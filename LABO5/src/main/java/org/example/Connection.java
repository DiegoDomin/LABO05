package org.example;

import alice.tuprolog.InvalidTermException;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import alice.tuprolog.Theory;
import alice.tuprolog.Var;

import java.io.InputStream;
import java.util.Set;

public class Connection {
    private static final Set<String> OPERACIONES = Set.of("d", "s", "sumar", "restar");
    private final Prolog engine = new Prolog();

    public Connection() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/calculo.pl")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró calculo.pl en los recursos.");
            }
            engine.setTheory(new Theory(in));
        }
    }

    public synchronized String consultar(String predicado, String expresion) throws Exception {
        if (!OPERACIONES.contains(predicado)) {
            throw new IllegalArgumentException("Operación desconocida.");
        }
        if (expresion == null || expresion.isBlank()) {
            throw new IllegalArgumentException("Escribí una expresión primero.");
        }
        if (expresion.length() > 1000) {
            throw new IllegalArgumentException("La expresión debe tener como máximo 1000 caracteres.");
        }
        Term entrada;
        try {
            entrada = engine.toTerm(expresion.trim());
        } catch (InvalidTermException ex) {
            throw new IllegalArgumentException("Revisá los paréntesis y usá * para multiplicar, por ejemplo 5*x^2.", ex);
        }
        validar(entrada, 0);
        if (predicado.equals("sumar") || predicado.equals("restar")) {
            String operador = predicado.equals("sumar") ? "+" : "-";
            if (!(entrada instanceof Struct estructura)
                    || estructura.getArity() != 2 || !estructura.getName().equals(operador)) {
                throw new IllegalArgumentException("Escribí la operación completa: "
                        + (predicado.equals("sumar") ? "x^2 + 5*x^2." : "7*x^3 - 3*x^3.")
                        + " Usá paréntesis para agrupar expresiones.");
            }
        }
        // La expresión se pasa como un término, sin concatenarla con código Prolog.
        Struct consulta = new Struct(predicado, new Struct("x"), entrada, new Var("R"));
        try {
            SolveInfo info = engine.solve(consulta);
            return info.isSuccess()
                    ? formatear(info.getTerm("R"), 0)
                    : "Sin solución para esa expresión";
        } finally {
            engine.solveEnd();
        }
    }

    // Presentación algebraica: seguir variables ligadas y conservar los paréntesis.
    // Los coeficientes y la simplificación se calculan exclusivamente en Prolog.
    private static String formatear(Term termino, int prioridadMinima) {
        Term valor = termino.getTerm();
        if (!(valor instanceof Struct estructura)) {
            String numero = valor.toString();
            return numero.startsWith("-") && prioridadMinima > 3 ? "(" + numero + ")" : numero;
        }
        String operador = estructura.getName();
        if (estructura.getArity() == 0) {
            return operador;
        }
        if (estructura.getArity() == 1) {
            if (operador.equals("log")) {
                return "log(" + formatear(estructura.getArg(0), 0) + ")";
            }
            String texto = operador + formatear(estructura.getArg(0), 3);
            return prioridadMinima > 3 ? "(" + texto + ")" : texto;
        }
        int prioridad = switch (operador) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            case "^" -> 4;
            default -> throw new IllegalStateException("Resultado con operador desconocido: " + operador);
        };
        boolean potencia = operador.equals("^");
        String texto = formatear(estructura.getArg(0), prioridad + (potencia ? 1 : 0))
                + operador + formatear(estructura.getArg(1), prioridad + (potencia ? 0 : 1));
        return prioridad < prioridadMinima ? "(" + texto + ")" : texto;
    }

    private static void validar(Term termino, int profundidad) {
        if (profundidad > 64) {
            throw new IllegalArgumentException("La expresión tiene demasiados niveles de paréntesis.");
        }
        if (termino instanceof alice.tuprolog.Number) {
            return;
        }
        if (termino instanceof Struct estructura) {
            String nombre = estructura.getName();
            int aridad = estructura.getArity();
            if (aridad == 0 && nombre.equals("x")) {
                return;
            }
            boolean unario = aridad == 1 && Set.of("+", "-", "log").contains(nombre);
            boolean binario = aridad == 2 && Set.of("+", "-", "*", "/", "^").contains(nombre);
            if (unario || binario) {
                for (int i = 0; i < aridad; i++) {
                    validar(estructura.getArg(i), profundidad + 1);
                }
                return;
            }
        }
        throw new IllegalArgumentException("Usá números, x minúscula, +, -, *, /, ^ y paréntesis; log(x) para derivar.");
    }
}

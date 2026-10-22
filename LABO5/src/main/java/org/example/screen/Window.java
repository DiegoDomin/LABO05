package org.example.screen;

import org.example.Connection;
import org.example.screen.components.Buttons;
import org.example.screen.components.PanelResult;
import org.example.screen.components.TextField;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Window extends JFrame {
    private final Connection motor;
    private final TextField txtEntrada = new TextField("Ej: x^2 + 5*x^2");
    private final PanelResult panelResultado = new PanelResult();
    private final JLabel lblEstado = new JLabel("Elegí un ejemplo o escribí tu expresión.");
    private final List<Buttons> operaciones = new ArrayList<>();
    private boolean consultando;

    public Window(Connection motor) {
        super("Cálculo simbólico en Prolog · Laboratorio 5");
        this.motor = motor;
        txtEntrada.setName("expresion");
        txtEntrada.getAccessibleContext().setAccessibleName("Expresión algebraica en x");
        txtEntrada.addActionListener(e -> ejecutar("d", "Derivada"));

        JPanel entrada = new JPanel();
        entrada.setLayout(new BoxLayout(entrada, BoxLayout.Y_AXIS));
        entrada.setOpaque(false);
        agregar(entrada, etiqueta("Expresión f(x)"), 8);
        agregar(entrada, txtEntrada, 10);
        agregar(entrada, etiqueta("Usá * para multiplicar y ^ para las potencias. Enter = derivar."), 14);
        agregar(entrada, crearEjemplos("Cálculo:", "x", "log(x)", "3*x^2", "2^x"), 8);
        agregar(entrada, crearEjemplos("Álgebra:", "x^2 + 5*x^2", "7*x^3 - 3*x^3", "4*x + 2*x", "8*x^2 - 5*x^2"), 18);
        agregar(entrada, crearBotones(), 8);
        agregar(entrada, etiqueta("Sumar: A + B. Restar: A - B. Agrupá polinomios con paréntesis."), 0);

        JPanel contenido = new JPanel(new BorderLayout(0, 20));
        contenido.setOpaque(false);
        contenido.add(entrada, BorderLayout.NORTH);
        contenido.add(panelResultado, BorderLayout.CENTER);
        contenido.add(crearPie(), BorderLayout.SOUTH);

        JPanel raiz = new JPanel(new BorderLayout(0, 24));
        raiz.setBackground(Theme.BG);
        raiz.setBorder(new EmptyBorder(24, 28, 24, 28));
        raiz.add(crearEncabezado(), BorderLayout.NORTH);
        raiz.add(contenido, BorderLayout.CENTER);
        setContentPane(raiz);
        setMinimumSize(new Dimension(800, 600));
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JPanel crearEncabezado() {
        JLabel titulo = new JLabel("Cálculo simbólico");
        titulo.setFont(Theme.sans(Font.BOLD, 26));
        titulo.setForeground(Theme.TEXT);
        JLabel subtitulo = new JLabel("Derivadas, integrales y operaciones con términos semejantes en Prolog");
        subtitulo.setFont(Theme.sans(Font.PLAIN, 13));
        subtitulo.setForeground(Theme.MUTED);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.add(titulo);
        panel.add(Box.createVerticalStrut(6));
        panel.add(subtitulo);
        return panel;
    }

    private JPanel crearEjemplos(String titulo, String... ejemplos) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);
        panel.add(etiqueta(titulo));
        for (String ejemplo : ejemplos) {
            Buttons chip = new Buttons(ejemplo, Theme.CARD, Theme.TEXT, 10, 7, 11);
            chip.addActionListener(e -> {
                txtEntrada.setText(ejemplo);
                txtEntrada.requestFocusInWindow();
            });
            panel.add(chip);
        }
        return panel;
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);
        panel.add(boton("Derivar", "d", "Derivada", Theme.BLUE));
        panel.add(boton("Integrar", "s", "Integral", Theme.GREEN));
        panel.add(boton("Sumar", "sumar", "Suma simplificada", Theme.AMBER));
        panel.add(boton("Restar", "restar", "Resta simplificada", Theme.RED));
        return panel;
    }

    private Buttons boton(String texto, String predicado, String titulo, Color color) {
        Buttons boton = new Buttons(texto, color, Theme.BG, 24, 11, 14);
        boton.addActionListener(e -> ejecutar(predicado, titulo));
        operaciones.add(boton);
        return boton;
    }

    private JPanel crearPie() {
        lblEstado.setName("estado");
        lblEstado.setFont(Theme.sans(Font.PLAIN, 12));
        lblEstado.setForeground(Theme.MUTED);
        Buttons copiar = new Buttons("Copiar", Theme.CARD, Theme.TEXT, 16, 7, 12);
        copiar.addActionListener(e -> copiarResultado());
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);
        panel.add(lblEstado, BorderLayout.CENTER);
        panel.add(copiar, BorderLayout.EAST);
        return panel;
    }

    private void ejecutar(String predicado, String titulo) {
        if (consultando) {
            return;
        }
        String entrada = txtEntrada.getText().trim();
        panelResultado.setTitulo(titulo);
        if (entrada.isEmpty()) {
            mostrar("", "Escribí una expresión primero.", Theme.AMBER);
            return;
        }
        consultando = true;
        operaciones.forEach(boton -> boton.setEnabled(false));
        mostrar("", "Resolviendo…", Theme.MUTED);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return motor.consultar(predicado, entrada);
            }

            @Override
            protected void done() {
                try {
                    String resultado = get();
                    if (resultado.startsWith("Sin solución")) {
                        String ayuda = predicado.equals("sumar") || predicado.equals("restar")
                                ? "Usá polinomios en x; no se admite aritmética sin variable."
                                : "No hay una regla para esa expresión.";
                        mostrar("", ayuda, Theme.AMBER);
                    } else {
                        mostrar(resultado + (predicado.equals("s") ? " + C" : ""),
                                "Listo · " + titulo, Theme.GREEN);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    mostrar("", "Se interrumpió la operación.", Theme.RED);
                } catch (ExecutionException ex) {
                    mostrar("", ex.getCause().getMessage(), Theme.RED);
                } finally {
                    consultando = false;
                    operaciones.forEach(boton -> boton.setEnabled(true));
                }
            }
        }.execute();
    }

    private void mostrar(String resultado, String estado, Color color) {
        panelResultado.setTexto(resultado);
        lblEstado.setText(estado);
        lblEstado.setForeground(color);
    }

    private void copiarResultado() {
        if (!panelResultado.getTexto().isEmpty()) {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(panelResultado.getTexto()), null);
            lblEstado.setText("Copiado al portapapeles");
            lblEstado.setForeground(Theme.BLUE);
        }
    }

    private static void agregar(JPanel panel, JComponent componente, int espacio) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(componente);
        if (espacio > 0) {
            panel.add(Box.createVerticalStrut(espacio));
        }
    }

    private static JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(Theme.sans(Font.PLAIN, 12));
        etiqueta.setForeground(Theme.MUTED);
        return etiqueta;
    }
}

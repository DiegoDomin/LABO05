package org.example;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ConnectionTest {
    private Connection motor;

    @Before
    public void crearMotor() throws Exception {
        motor = new Connection();
    }

    @Test
    public void ejemplosSolicitadosPorElDocente() throws Exception {
        assertEquals("6*x^2", motor.consultar("sumar", "x^2 + 5*x^2"));
        assertEquals("4*x^3", motor.consultar("restar", "7*x^3 - 3*x^3"));
        assertEquals("6*x", motor.consultar("sumar", "4*x + 2*x"));
        assertEquals("3*x^2", motor.consultar("restar", "8*x^2 - 5*x^2"));
    }

    @Test
    public void conservaGradosDiferentes() throws Exception {
        assertEquals("x^2+5*x", motor.consultar("sumar", "x^2 + 5*x"));
        assertEquals("7*x^3-3*x^2", motor.consultar("restar", "7*x^3 - 3*x^2"));
    }

    @Test
    public void restaTodosLosTerminosDelSegundoPolinomio() throws Exception {
        assertEquals("3*x^2-2*x+3", motor.consultar("restar", "(5*x^2 + x + 4) - (2*x^2 + 3*x + 1)"));
    }

    @Test
    public void combinaPolinomiosConParentesis() throws Exception {
        assertEquals("6*x^2+3*x+7", motor.consultar("sumar", "(x^2 + x + 2) + (5*x^2 + 2*x + 5)"));
    }

    @Test
    public void eliminaTerminosQueSeCancelan() throws Exception {
        assertEquals("0", motor.consultar("restar", "x^2 - x^2"));
        assertEquals("1", motor.consultar("restar", "(x + 2) - (x + 1)"));
    }

    @Test
    public void manejaResultadosNegativosYCoeficienteImplicito() throws Exception {
        assertEquals("-x", motor.consultar("restar", "x - 2*x"));
        assertEquals("-2*x^2", motor.consultar("restar", "3*x^2 - 5*x^2"));
        assertEquals("-6*x", motor.consultar("sumar", "-4*x + -2*x"));
        assertEquals("x", motor.consultar("sumar", "2*x + -x"));
    }

    @Test
    public void manejaRestasAnidadasYUnarios() throws Exception {
        assertEquals("0", motor.consultar("restar", "x - (2*x - x)"));
        assertEquals("6*x", motor.consultar("restar", "4*x - (-2*x)"));
        assertEquals("3*x", motor.consultar("sumar", "+x + 2*x"));
    }

    @Test
    public void aceptaCoeficienteALaDerechaYDistribucionPorConstante() throws Exception {
        assertEquals("6*x^2", motor.consultar("sumar", "x^2*5 + x^2"));
        assertEquals("6*x+6", motor.consultar("sumar", "2*(x + 3) + 4*x"));
    }

    @Test
    public void normalizaPotenciasCeroYUno() throws Exception {
        assertEquals("3*x", motor.consultar("sumar", "x^1 + 2*x"));
        assertEquals("3", motor.consultar("sumar", "x^0 + 2"));
    }

    @Test
    public void aceptaDecimalesYFraccionesComoCoeficientes() throws Exception {
        assertEquals("4.0*x", motor.consultar("sumar", "1.5*x + 2.5*x"));
        assertEquals("x", motor.consultar("sumar", "x/2 + x/2"));
    }

    @Test
    public void noResuelveAritmeticaSinVariable() throws Exception {
        assertTrue(motor.consultar("sumar", "1 + 2").startsWith("Sin solución"));
        assertTrue(motor.consultar("restar", "7 - 3").startsWith("Sin solución"));
    }

    @Test
    public void rechazaExpresionesQueNoSonPolinomios() throws Exception {
        assertTrue(motor.consultar("sumar", "log(x) + x").startsWith("Sin solución"));
        assertTrue(motor.consultar("sumar", "x^(-1) + x").startsWith("Sin solución"));
        assertTrue(motor.consultar("sumar", "x/0 + x").startsWith("Sin solución"));
        assertTrue(motor.consultar("sumar", "x^0.5 + x").startsWith("Sin solución"));
    }

    @Test
    public void conservaDerivadasDeLaGuia() throws Exception {
        assertEquals("0", motor.consultar("d", "5"));
        assertEquals("1", motor.consultar("d", "x"));
        assertEquals("0", motor.consultar("d", "x^0"));
        assertEquals("5", motor.consultar("d", "5*x"));
        assertEquals("3*x^2", motor.consultar("d", "x^3"));
        assertEquals("6*x", motor.consultar("d", "3*x^2"));
        assertEquals("1/x", motor.consultar("d", "log(x)"));
        assertEquals("log(2)*2^x", motor.consultar("d", "2^x"));
    }

    @Test
    public void conservaIntegralesYLinealidad() throws Exception {
        assertEquals("5*x", motor.consultar("s", "5"));
        assertEquals("0.5*x^2", motor.consultar("s", "x"));
        assertEquals("x^3", motor.consultar("s", "3*x^2"));
        assertEquals("x^3+x^2-x", motor.consultar("s", "3*x^2 + 2*x - 1"));
        assertEquals("log(x)", motor.consultar("s", "x^(-1)"));
    }

    @Test
    public void derivaSumasYRestas() throws Exception {
        assertEquals("6*x+2", motor.consultar("d", "3*x^2 + 2*x - 1"));
    }

    @Test
    public void exigeQueElBotonCorrespondaConLaOperacion() {
        assertThrows(IllegalArgumentException.class, () -> motor.consultar("sumar", "x - 2*x"));
        assertThrows(IllegalArgumentException.class, () -> motor.consultar("restar", "x + 2*x"));
    }

    @Test
    public void informaEntradasInvalidasSinDanarElMotor() throws Exception {
        for (String entrada : new String[]{"", "2x + x", "x +", "(x + x", "X + 2*X", "y + y"}) {
            assertThrows(entrada, IllegalArgumentException.class, () -> motor.consultar("sumar", entrada));
        }
        assertEquals("2*x", motor.consultar("sumar", "x + x"));
    }

    @Test
    public void noEjecutaCodigoPrologRecibidoEnLaEntrada() throws Exception {
        for (String entrada : new String[]{"halt", "x, halt", "x). halt.", "asserta(test)", "x + x. halt."}) {
            assertThrows(entrada, IllegalArgumentException.class, () -> motor.consultar("sumar", entrada));
        }
        assertThrows(IllegalArgumentException.class, () -> motor.consultar("halt", "x"));
        assertEquals("2*x", motor.consultar("sumar", "x + x"));
    }
}

package org.example.screen.components;

import org.example.screen.Theme;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Font;

public class PanelResult extends JPanel {
    private final JLabel lblTitulo = new JLabel("Resultado");
    private final JTextArea txtResultado = new JTextArea(5, 30);

    public PanelResult() {
        super(new BorderLayout(0, 8));
        setOpaque(false);
        lblTitulo.setFont(Theme.sans(Font.BOLD, 12));
        lblTitulo.setForeground(Theme.MUTED);
        txtResultado.setName("resultado");
        txtResultado.setEditable(false);
        txtResultado.setLineWrap(true);
        txtResultado.setWrapStyleWord(true);
        txtResultado.setFont(Theme.mono(Font.PLAIN, 24));
        txtResultado.setBackground(Theme.CARD);
        txtResultado.setForeground(Theme.TEXT);
        txtResultado.setCaretColor(Theme.TEXT);
        txtResultado.setBorder(new EmptyBorder(14, 16, 14, 16));
        JScrollPane scroll = new JScrollPane(txtResultado);
        scroll.setBorder(new LineBorder(Theme.BORDER, 1, true));
        scroll.getViewport().setBackground(Theme.CARD);
        add(lblTitulo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void setTitulo(String titulo) {
        lblTitulo.setText(titulo);
    }

    public void setTexto(String texto) {
        txtResultado.setText(texto);
        txtResultado.setCaretPosition(0);
    }

    public String getTexto() {
        return txtResultado.getText();
    }
}

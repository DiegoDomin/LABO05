package org.example.screen.components;

import org.example.screen.Theme;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Buttons extends JButton {
    private final Color colorBase;
    private boolean hover;

    public Buttons(String texto, Color colorBase, Color colorTexto,
                   int padH, int padV, int tamFuente) {
        super(texto);
        this.colorBase = colorBase;
        setFont(Theme.sans(Font.BOLD, tamFuente));
        setForeground(colorTexto);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(new EmptyBorder(padV, padH, padV, padH));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color color = colorBase;
        if (!isEnabled()) {
            color = Theme.BORDER;
        } else if (getModel().isPressed()) {
            color = colorBase.darker();
        } else if (hover) {
            color = colorBase.equals(Theme.CARD) ? Theme.BORDER : colorBase.brighter();
        }
        g2.setColor(color);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
        g2.dispose();
        super.paintComponent(g);
    }
}

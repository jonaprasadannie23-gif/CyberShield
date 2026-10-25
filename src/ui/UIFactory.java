package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Factory for creating consistently styled Swing components.
 * Centralises styling so all screens look uniform.
 */
public final class UIFactory {

    private UIFactory() {}

    // -----------------------------------------------------------------------
    // Labels
    // -----------------------------------------------------------------------

    public static JLabel titleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_TITLE);
        lbl.setForeground(Theme.TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel subtitleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_SUBTITLE);
        lbl.setForeground(Theme.TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel bodyLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BODY);
        lbl.setForeground(Theme.TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel mutedLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        return lbl;
    }

    public static JLabel accentLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BODY);
        lbl.setForeground(Theme.ACCENT_TEAL);
        return lbl;
    }

    // -----------------------------------------------------------------------
    // Buttons
    // -----------------------------------------------------------------------

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_LABEL);
        btn.setBackground(Theme.ACCENT_TEAL);
        btn.setForeground(Theme.BG_DARK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        styleButtonHover(btn, Theme.ACCENT_TEAL, new Color(28, 175, 130));
        return btn;
    }

    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_LABEL);
        btn.setBackground(Theme.BG_INPUT);
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        styleButtonHover(btn, Theme.BG_INPUT, Theme.ACCENT_HOVER);
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_LABEL);
        btn.setBackground(Theme.RISK_HIGH);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        styleButtonHover(btn, Theme.RISK_HIGH, new Color(200, 60, 55));
        return btn;
    }

    // -----------------------------------------------------------------------
    // Text fields
    // -----------------------------------------------------------------------

    public static JTextField textField(int columns) {
        JTextField tf = new JTextField(columns);
        styleInputComponent(tf);
        return tf;
    }

    public static JPasswordField passwordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        styleInputComponent(pf);
        return pf;
    }

    public static JTextArea textArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setFont(Theme.FONT_BODY);
        ta.setBackground(Theme.BG_INPUT);
        ta.setForeground(Theme.TEXT_PRIMARY);
        ta.setCaretColor(Theme.TEXT_PRIMARY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return ta;
    }

    // -----------------------------------------------------------------------
    // Combo box
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public static <T> JComboBox<T> comboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(Theme.FONT_BODY);
        cb.setBackground(Theme.BG_INPUT);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        return cb;
    }

    // -----------------------------------------------------------------------
    // Panels
    // -----------------------------------------------------------------------

    public static JPanel darkPanel() {
        JPanel p = new JPanel();
        p.setBackground(Theme.BG_PANEL);
        return p;
    }

    public static JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(Theme.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        return p;
    }

    /** A solid horizontal separator line in the border colour. */
    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setBackground(Theme.BORDER);
        return sep;
    }

    // -----------------------------------------------------------------------
    // Scroll pane
    // -----------------------------------------------------------------------

    public static JScrollPane scrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(Theme.BG_PANEL);
        sp.getViewport().setBackground(Theme.BG_PANEL);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        sp.getVerticalScrollBar().setBackground(Theme.BG_PANEL);
        return sp;
    }

    // -----------------------------------------------------------------------
    // Stat card (Dashboard)
    // -----------------------------------------------------------------------

    public static JPanel statCard(String value, String label, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Theme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLbl.setForeground(valueColor);

        JLabel nameLbl = new JLabel(label, SwingConstants.CENTER);
        nameLbl.setFont(Theme.FONT_SMALL);
        nameLbl.setForeground(Theme.TEXT_SECONDARY);

        card.add(valLbl,  BorderLayout.CENTER);
        card.add(nameLbl, BorderLayout.SOUTH);
        return card;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private static void styleInputComponent(JComponent c) {
        c.setFont(Theme.FONT_BODY);
        c.setBackground(Theme.BG_INPUT);
        c.setForeground(Theme.TEXT_PRIMARY);
        if (c instanceof JTextField) ((JTextField) c).setCaretColor(Theme.TEXT_PRIMARY);
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    private static void styleButtonHover(JButton btn, Color normal, Color hover) {
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(normal); }
        });
    }
}

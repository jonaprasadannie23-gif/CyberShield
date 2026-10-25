package ui;

import service.PasswordStrengthService;
import service.PasswordStrengthService.StrengthResult;
import service.PasswordStrengthService.StrengthLevel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Local password strength checker.
 *
 * The password is NEVER saved, logged, or included in any record.
 * The input is cleared when the user navigates away or clicks Clear.
 */
public class PasswordCheckerPanel extends JPanel {

    private final PasswordStrengthService strengthService;

    private JPasswordField passwordField;
    private JProgressBar strengthBar;
    private JLabel strengthLabel;
    private JPanel suggestionsPanel;
    private JLabel disclaimerLabel;
    private JCheckBox showPasswordBox;

    public PasswordCheckerPanel() {
        this.strengthService = AppContext.getInstance().getPasswordStrengthService();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_PANEL);
        setBorder(new EmptyBorder(28, 32, 28, 32));

        // Header
        JLabel header = UIFactory.titleLabel("🔑  Password Strength Checker");
        JLabel sub    = UIFactory.mutedLabel("Check how strong your password is. It is NEVER saved or transmitted.");
        JPanel hdr    = new JPanel(new BorderLayout(0, 4));
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(0, 0, 24, 0));
        hdr.add(header, BorderLayout.NORTH);
        hdr.add(sub,    BorderLayout.SOUTH);

        // Input card
        JPanel inputCard = new JPanel();
        inputCard.setLayout(new BoxLayout(inputCard, BoxLayout.Y_AXIS));
        inputCard.setBackground(Theme.BG_CARD);
        inputCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(22, 28, 22, 28)
        ));

        JLabel inputLbl = UIFactory.bodyLabel("Enter a password to test:");
        inputLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputCard.add(inputLbl);
        inputCard.add(Box.createVerticalStrut(8));

        // Password field + show toggle row
        passwordField = UIFactory.passwordField(30);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputCard.add(passwordField);
        inputCard.add(Box.createVerticalStrut(8));

        showPasswordBox = new JCheckBox("Show password");
        showPasswordBox.setFont(Theme.FONT_SMALL);
        showPasswordBox.setForeground(Theme.TEXT_SECONDARY);
        showPasswordBox.setBackground(Theme.BG_CARD);
        showPasswordBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPasswordBox.addActionListener(e -> togglePasswordVisibility());
        inputCard.add(showPasswordBox);
        inputCard.add(Box.createVerticalStrut(16));

        // Strength bar
        strengthBar = new JProgressBar(0, 100);
        strengthBar.setStringPainted(false);
        strengthBar.setForeground(Theme.TEXT_SECONDARY);
        strengthBar.setBackground(Theme.BG_DARK);
        strengthBar.setBorderPainted(false);
        strengthBar.setPreferredSize(new Dimension(0, 12));
        strengthBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        strengthBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputCard.add(strengthBar);
        inputCard.add(Box.createVerticalStrut(8));

        strengthLabel = new JLabel("Enter a password above");
        strengthLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        strengthLabel.setForeground(Theme.TEXT_SECONDARY);
        strengthLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputCard.add(strengthLabel);
        inputCard.add(Box.createVerticalStrut(16));

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton checkBtn = UIFactory.primaryButton("Check Strength");
        JButton clearBtn = UIFactory.secondaryButton("Clear");
        btnRow.add(checkBtn);
        btnRow.add(clearBtn);
        inputCard.add(btnRow);

        checkBtn.addActionListener(e -> evaluatePassword());
        clearBtn.addActionListener(e  -> clearAll());

        // Live evaluation on keystroke
        passwordField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { evaluatePassword(); }
        });

        // Suggestions panel
        suggestionsPanel = new JPanel();
        suggestionsPanel.setLayout(new BoxLayout(suggestionsPanel, BoxLayout.Y_AXIS));
        suggestionsPanel.setBackground(Theme.BG_PANEL);

        // Disclaimer
        disclaimerLabel = new JLabel(
            "<html><i>⚠ This is an educational strength estimate only. It does not determine whether "
          + "your password has been leaked or is truly secure.</i></html>");
        disclaimerLabel.setFont(Theme.FONT_SMALL);
        disclaimerLabel.setForeground(Theme.RISK_MEDIUM);
        disclaimerLabel.setBorder(new EmptyBorder(16, 0, 0, 0));

        // Assemble
        JPanel centre = new JPanel(new BorderLayout(0, 16));
        centre.setOpaque(false);
        centre.add(hdr,              BorderLayout.NORTH);
        centre.add(inputCard,        BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 0));
        bottom.setOpaque(false);
        bottom.add(suggestionsPanel, BorderLayout.CENTER);
        bottom.add(disclaimerLabel,  BorderLayout.SOUTH);

        add(centre, BorderLayout.NORTH);
        add(bottom, BorderLayout.CENTER);
    }

    private void evaluatePassword() {
        char[] chars = passwordField.getPassword();
        String password = new String(chars);
        java.util.Arrays.fill(chars, '\0');   // clear char array immediately

        StrengthResult result = strengthService.evaluate(password);
        password = null;  // hint to GC

        // Update bar
        strengthBar.setValue(result.score());
        strengthBar.setForeground(Color.decode(result.level().getColor()));

        // Update label
        strengthLabel.setText(result.level().getLabel());
        strengthLabel.setForeground(Color.decode(result.level().getColor()));

        // Update suggestions
        suggestionsPanel.removeAll();
        if (!result.suggestions().isEmpty()) {
            JLabel sugTitle = UIFactory.subtitleLabel("Suggestions:");
            sugTitle.setBorder(new EmptyBorder(12, 0, 6, 0));
            suggestionsPanel.add(sugTitle);
            for (String s : result.suggestions()) {
                JLabel sl = UIFactory.bodyLabel("  • " + s);
                suggestionsPanel.add(sl);
            }
        }
        suggestionsPanel.revalidate();
        suggestionsPanel.repaint();
    }

    private void clearAll() {
        passwordField.setText("");
        strengthBar.setValue(0);
        strengthBar.setForeground(Theme.TEXT_SECONDARY);
        strengthLabel.setText("Enter a password above");
        strengthLabel.setForeground(Theme.TEXT_SECONDARY);
        suggestionsPanel.removeAll();
        suggestionsPanel.revalidate();
        suggestionsPanel.repaint();
        showPasswordBox.setSelected(false);
        passwordField.setEchoChar('•');
    }

    private void togglePasswordVisibility() {
        if (showPasswordBox.isSelected()) {
            passwordField.setEchoChar((char) 0);
        } else {
            passwordField.setEchoChar('•');
        }
    }
}

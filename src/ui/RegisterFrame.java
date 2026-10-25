package ui;

import model.User;
import service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * New user registration screen.
 */
public class RegisterFrame extends JFrame {

    private final AuthenticationService authService;

    private JTextField fullNameField;
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JLabel errorLabel;

    public RegisterFrame(AuthenticationService authService) {
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        setTitle("CyberShield – Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 640);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_DARK);
        setContentPane(root);

        // Header
        JPanel header = new JPanel();
        header.setBackground(Theme.BG_DARK);
        header.setBorder(new EmptyBorder(32, 40, 10, 40));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("🛡", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        icon.setForeground(Theme.ACCENT_TEAL);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = UIFactory.titleLabel("Create Account");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = UIFactory.mutedLabel("Join CyberShield – Personal Cyber Safety");
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(icon);
        header.add(Box.createVerticalStrut(6));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        // Form card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.BG_PANEL);
        card.setBorder(new EmptyBorder(24, 36, 24, 36));

        fullNameField  = addField(card, "Full Name");
        usernameField  = addField(card, "Username  (3–20 chars, letters/digits/_)");
        emailField     = addField(card, "Email Address");
        passwordField  = addPasswordField(card, "Password  (min. 8 characters)");
        confirmField   = addPasswordField(card, "Confirm Password");

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.RISK_HIGH);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(12));

        JButton regBtn = UIFactory.primaryButton("Create Account");
        regBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        regBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(regBtn);
        card.add(Box.createVerticalStrut(12));

        // Back to login
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linkRow.setOpaque(false);
        linkRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel hasAcc = UIFactory.mutedLabel("Already have an account? ");
        JButton loginBtn = new JButton("Sign In");
        loginBtn.setFont(Theme.FONT_SMALL);
        loginBtn.setForeground(Theme.ACCENT_TEAL);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkRow.add(hasAcc);
        linkRow.add(loginBtn);
        card.add(linkRow);

        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(Theme.BG_DARK);
        centre.setBorder(new EmptyBorder(8, 40, 30, 40));
        centre.add(card);

        root.add(header, BorderLayout.NORTH);
        root.add(centre, BorderLayout.CENTER);

        regBtn.addActionListener(e -> doRegister());
        loginBtn.addActionListener(e -> backToLogin());
    }

    private JTextField addField(JPanel parent, String label) {
        JLabel lbl = UIFactory.bodyLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(4));
        JTextField tf = UIFactory.textField(20);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(tf);
        parent.add(Box.createVerticalStrut(12));
        return tf;
    }

    private JPasswordField addPasswordField(JPanel parent, String label) {
        JLabel lbl = UIFactory.bodyLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(4));
        JPasswordField pf = UIFactory.passwordField(20);
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        pf.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(pf);
        parent.add(Box.createVerticalStrut(12));
        return pf;
    }

    private void doRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm  = new String(confirmField.getPassword());

        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            passwordField.setText("");
            confirmField.setText("");
            return;
        }

        try {
            User user = authService.register(username, email, fullName, password);
            // Clear sensitive fields
            passwordField.setText("");
            confirmField.setText("");
            JOptionPane.showMessageDialog(this,
                "Account created successfully!\nWelcome, " + user.getFullName() + ".",
                "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
            backToLogin();
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
            passwordField.setText("");
            confirmField.setText("");
        }
    }

    private void backToLogin() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            // Rebuild auth service chain from main app context is handled by AppContext
            LoginFrame lf = AppContext.getInstance().createLoginFrame();
            lf.setVisible(true);
        });
    }
}

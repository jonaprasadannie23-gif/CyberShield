package ui;

import model.User;
import service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Login screen – the application entry point for returning users.
 */
public class LoginFrame extends JFrame {

    private final AuthenticationService authService;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public LoginFrame(AuthenticationService authService) {
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        setTitle("CyberShield – Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_DARK);
        setContentPane(root);

        // ---- Header ----
        JPanel header = new JPanel();
        header.setBackground(Theme.BG_DARK);
        header.setBorder(new EmptyBorder(40, 40, 10, 40));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("🛡", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        icon.setForeground(Theme.ACCENT_TEAL);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = UIFactory.titleLabel("CyberShield");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = UIFactory.mutedLabel("Personal Cyber Safety Management System");
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(icon);
        header.add(Box.createVerticalStrut(8));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        // ---- Form card ----
        JPanel card = UIFactory.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 36, 28, 36));
        card.setBackground(Theme.BG_PANEL);

        JLabel formTitle = UIFactory.subtitleLabel("Sign In");
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(formTitle);
        card.add(Box.createVerticalStrut(20));

        // Username
        JLabel userLbl = UIFactory.bodyLabel("Username");
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(userLbl);
        card.add(Box.createVerticalStrut(5));
        usernameField = UIFactory.textField(20);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(usernameField);
        card.add(Box.createVerticalStrut(14));

        // Password
        JLabel passLbl = UIFactory.bodyLabel("Password");
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(passLbl);
        card.add(Box.createVerticalStrut(5));
        passwordField = UIFactory.passwordField(20);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(passwordField);
        card.add(Box.createVerticalStrut(8));

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.RISK_HIGH);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(16));

        // Login button
        JButton loginBtn = UIFactory.primaryButton("Sign In");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(14));

        // Register link
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linkRow.setOpaque(false);
        linkRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel noAcc = UIFactory.mutedLabel("Don't have an account? ");
        JButton regBtn = new JButton("Register");
        regBtn.setFont(Theme.FONT_SMALL);
        regBtn.setForeground(Theme.ACCENT_TEAL);
        regBtn.setContentAreaFilled(false);
        regBtn.setBorderPainted(false);
        regBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkRow.add(noAcc);
        linkRow.add(regBtn);
        card.add(linkRow);

        // ---- Assemble ----
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(Theme.BG_DARK);
        centre.setBorder(new EmptyBorder(10, 40, 40, 40));
        centre.add(card);

        root.add(header, BorderLayout.NORTH);
        root.add(centre, BorderLayout.CENTER);

        // ---- Actions ----
        loginBtn.addActionListener(e -> doLogin());
        regBtn.addActionListener(e -> openRegister());

        // Allow Enter key to submit
        passwordField.addActionListener(e -> doLogin());
        usernameField.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            User user = authService.login(username, password);
            errorLabel.setText(" ");
            // Clear password from memory
            passwordField.setText("");
            openDashboard(user);
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
            passwordField.setText("");
        }
    }

    private void openDashboard(User user) {
        dispose();
        SwingUtilities.invokeLater(() -> {
            DashboardFrame dash = new DashboardFrame(user);
            dash.setVisible(true);
        });
    }

    private void openRegister() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            RegisterFrame reg = new RegisterFrame(authService);
            reg.setVisible(true);
        });
    }
}

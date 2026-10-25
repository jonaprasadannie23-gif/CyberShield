package ui;

import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Main application window with persistent sidebar navigation.
 * Each menu item swaps the content panel via CardLayout.
 */
public class DashboardFrame extends JFrame {

    private final User currentUser;
    private JPanel contentArea;
    private CardLayout cardLayout;

    // Panels (lazily created via getOrCreate pattern)
    private DashboardPanel dashboardPanel;
    private IncidentFormPanel incidentFormPanel;
    private IncidentHistoryPanel incidentHistoryPanel;
    private TaskPanel taskPanel;
    private PasswordCheckerPanel passwordCheckerPanel;
    private ReportsPanel reportsPanel;

    // Nav button references for active-state highlighting
    private JButton activeSidebarBtn;

    public DashboardFrame(User user) {
        this.currentUser = user;
        initUI();
    }

    private void initUI() {
        setTitle("CyberShield");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.BG_DARK);
        setContentPane(root);

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildContent(), BorderLayout.CENTER);

        showPanel("dashboard");
    }

    // -----------------------------------------------------------------------
    // Sidebar
    // -----------------------------------------------------------------------

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(Theme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Logo area
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(Theme.BG_SIDEBAR);
        logoPanel.setBorder(new EmptyBorder(20, 16, 20, 16));

        JLabel logoIcon = new JLabel("🛡 CyberShield");
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoIcon.setForeground(Theme.ACCENT_TEAL);
        logoPanel.add(logoIcon, BorderLayout.CENTER);
        sidebar.add(logoPanel);

        // Divider
        sidebar.add(sidebarDivider());

        // Nav items
        JButton btnDashboard  = navButton("  📊  Dashboard",   "dashboard");
        JButton btnReport     = navButton("  🚨  Report Incident", "report");
        JButton btnHistory    = navButton("  📋  Incident History", "history");
        JButton btnTasks      = navButton("  ✅  Safety Tasks",  "tasks");
        JButton btnPassword   = navButton("  🔑  Password Check", "password");
        JButton btnReports    = navButton("  📈  Reports",      "reports");

        sidebar.add(btnDashboard);
        sidebar.add(btnReport);
        sidebar.add(btnHistory);
        sidebar.add(btnTasks);
        sidebar.add(btnPassword);
        sidebar.add(btnReports);

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarDivider());

        // User info
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(Theme.BG_SIDEBAR);
        userPanel.setBorder(new EmptyBorder(12, 16, 8, 16));
        JLabel userLbl = new JLabel("👤 " + currentUser.getFullName());
        userLbl.setFont(Theme.FONT_SMALL);
        userLbl.setForeground(Theme.TEXT_SECONDARY);
        userPanel.add(userLbl, BorderLayout.CENTER);
        sidebar.add(userPanel);

        // Logout
        JButton logoutBtn = new JButton("  🚪  Logout");
        logoutBtn.setFont(Theme.FONT_BODY);
        logoutBtn.setForeground(Theme.RISK_HIGH);
        logoutBtn.setBackground(Theme.BG_SIDEBAR);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> confirmLogout());
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(8));

        // Activate dashboard by default
        setActiveNavButton(btnDashboard);

        return sidebar;
    }

    private JButton navButton(String text, String panelName) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_BODY);
        btn.setForeground(Theme.TEXT_SECONDARY);
        btn.setBackground(Theme.BG_SIDEBAR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            showPanel(panelName);
            setActiveNavButton(btn);
        });
        return btn;
    }

    private void setActiveNavButton(JButton btn) {
        if (activeSidebarBtn != null) {
            activeSidebarBtn.setBackground(Theme.BG_SIDEBAR);
            activeSidebarBtn.setForeground(Theme.TEXT_SECONDARY);
        }
        activeSidebarBtn = btn;
        btn.setBackground(Theme.ACCENT_HOVER);
        btn.setForeground(Theme.ACCENT_TEAL);
    }

    private JSeparator sidebarDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setBackground(Theme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    // -----------------------------------------------------------------------
    // Content area
    // -----------------------------------------------------------------------

    private JPanel buildContent() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(Theme.BG_PANEL);

        // Panels created once and reused
        dashboardPanel       = new DashboardPanel(currentUser, this);
        incidentFormPanel    = new IncidentFormPanel(currentUser, this);
        incidentHistoryPanel = new IncidentHistoryPanel(currentUser);
        taskPanel            = new TaskPanel(currentUser);
        passwordCheckerPanel = new PasswordCheckerPanel();
        reportsPanel         = new ReportsPanel(currentUser);

        contentArea.add(dashboardPanel,       "dashboard");
        contentArea.add(incidentFormPanel,    "report");
        contentArea.add(incidentHistoryPanel, "history");
        contentArea.add(taskPanel,            "tasks");
        contentArea.add(passwordCheckerPanel, "password");
        contentArea.add(reportsPanel,         "reports");

        return contentArea;
    }

    public void showPanel(String name) {
        // Refresh data-dependent panels on every navigation
        switch (name) {
            case "dashboard": dashboardPanel.refresh(); break;
            case "history":   incidentHistoryPanel.refresh(); break;
            case "tasks":     taskPanel.refresh(); break;
            case "reports":   reportsPanel.refresh(); break;
        }
        cardLayout.show(contentArea, name);
    }

    public void navigateTo(String panelName) {
        showPanel(panelName);
    }

    // -----------------------------------------------------------------------
    // Logout
    // -----------------------------------------------------------------------

    private void confirmLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame lf = AppContext.getInstance().createLoginFrame();
                lf.setVisible(true);
            });
        }
    }
}

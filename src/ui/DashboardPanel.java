package ui;

import model.*;
import service.ReportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dashboard panel – shows live statistics and quick actions.
 * All numbers are computed from stored data via ReportService.
 */
public class DashboardPanel extends JPanel {

    private final User currentUser;
    private final DashboardFrame parentFrame;
    private final ReportService reportService;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    // Stat card labels (updated on refresh)
    private JLabel totalIncLbl;
    private JLabel highRiskLbl;
    private JLabel pendingTasksLbl;
    private JLabel completedTasksLbl;

    private JPanel recentPanel;

    public DashboardPanel(User user, DashboardFrame parent) {
        this.currentUser   = user;
        this.parentFrame   = parent;
        this.reportService = AppContext.getInstance().getReportService();
        initUI();
        refresh();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_PANEL);
        setBorder(new EmptyBorder(28, 32, 28, 32));

        // ---- Top header ----
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel greeting = UIFactory.titleLabel("Welcome, " + currentUser.getFullName() + " 👋");
        JLabel subGreet = UIFactory.mutedLabel("Here's your personal cyber safety overview");
        JPanel greetPanel = new JPanel(new BorderLayout(0, 4));
        greetPanel.setOpaque(false);
        greetPanel.add(greeting, BorderLayout.NORTH);
        greetPanel.add(subGreet, BorderLayout.SOUTH);
        topBar.add(greetPanel, BorderLayout.WEST);

        // ---- Stat cards ----
        totalIncLbl      = new JLabel("0");
        highRiskLbl      = new JLabel("0");
        pendingTasksLbl  = new JLabel("0");
        completedTasksLbl = new JLabel("0");

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.setBorder(new EmptyBorder(20, 0, 20, 0));

        stats.add(buildStatCard(totalIncLbl,      "Total Incidents",   Theme.ACCENT_BLUE));
        stats.add(buildStatCard(highRiskLbl,       "High Risk",         Theme.RISK_HIGH));
        stats.add(buildStatCard(pendingTasksLbl,   "Pending Tasks",     Theme.RISK_MEDIUM));
        stats.add(buildStatCard(completedTasksLbl, "Completed Tasks",   Theme.RISK_LOW));

        // ---- Quick actions ----
        JLabel actTitle = UIFactory.subtitleLabel("Quick Actions");
        actTitle.setBorder(new EmptyBorder(4, 0, 10, 0));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);

        JButton btnReport = UIFactory.primaryButton("🚨  Report Incident");
        JButton btnPwdChk = UIFactory.secondaryButton("🔑  Check Password");
        JButton btnTasks  = UIFactory.secondaryButton("✅  View Tasks");

        actions.add(btnReport);
        actions.add(btnPwdChk);
        actions.add(btnTasks);

        btnReport.addActionListener(e -> parentFrame.navigateTo("report"));
        btnPwdChk.addActionListener(e -> parentFrame.navigateTo("password"));
        btnTasks.addActionListener(e -> parentFrame.navigateTo("tasks"));

        // ---- Recent incidents ----
        JLabel recentTitle = UIFactory.subtitleLabel("Recent Incidents");
        recentTitle.setBorder(new EmptyBorder(16, 0, 10, 0));
        recentPanel = new JPanel();
        recentPanel.setLayout(new BoxLayout(recentPanel, BoxLayout.Y_AXIS));
        recentPanel.setBackground(Theme.BG_PANEL);

        JScrollPane recentScroll = UIFactory.scrollPane(recentPanel);
        recentScroll.setBorder(BorderFactory.createEmptyBorder());

        // ---- Assemble ----
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(topBar,    BorderLayout.NORTH);
        topSection.add(stats,     BorderLayout.CENTER);

        JPanel midSection = new JPanel(new BorderLayout());
        midSection.setOpaque(false);
        midSection.add(actTitle,  BorderLayout.NORTH);
        midSection.add(actions,   BorderLayout.CENTER);

        JPanel botSection = new JPanel(new BorderLayout());
        botSection.setOpaque(false);
        botSection.add(recentTitle,  BorderLayout.NORTH);
        botSection.add(recentScroll, BorderLayout.CENTER);

        JPanel centre = new JPanel(new BorderLayout(0, 0));
        centre.setOpaque(false);
        centre.add(topSection, BorderLayout.NORTH);
        centre.add(midSection, BorderLayout.CENTER);
        centre.add(botSection, BorderLayout.SOUTH);

        add(centre, BorderLayout.CENTER);
    }

    private JPanel buildStatCard(JLabel valueLabel, String name, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Theme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel nameLbl = new JLabel(name, SwingConstants.CENTER);
        nameLbl.setFont(Theme.FONT_SMALL);
        nameLbl.setForeground(Theme.TEXT_SECONDARY);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(nameLbl,    BorderLayout.SOUTH);
        return card;
    }

    /** Called every time this panel is shown, to keep stats live. */
    public void refresh() {
        String uid = currentUser.getUserId();
        totalIncLbl.setText(String.valueOf(reportService.getTotalIncidents(uid)));
        highRiskLbl.setText(String.valueOf(reportService.getCountByRiskLevel(uid).get(RiskLevel.HIGH)));
        pendingTasksLbl.setText(String.valueOf(reportService.getPendingTasks(uid)));
        completedTasksLbl.setText(String.valueOf(reportService.getCompletedTasks(uid)));

        // Rebuild recent incidents list
        recentPanel.removeAll();
        List<Incident> recent = reportService.getRecentIncidents(uid, 5);
        if (recent.isEmpty()) {
            JLabel empty = UIFactory.mutedLabel("  No incidents reported yet. Stay vigilant!");
            empty.setBorder(new EmptyBorder(12, 0, 12, 0));
            recentPanel.add(empty);
        } else {
            for (Incident inc : recent) {
                recentPanel.add(buildRecentRow(inc));
                recentPanel.add(UIFactory.separator());
            }
        }
        recentPanel.revalidate();
        recentPanel.repaint();
    }

    private JPanel buildRecentRow(Incident inc) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Theme.BG_CARD);
        row.setBorder(new EmptyBorder(10, 14, 10, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        // Risk badge
        JLabel badge = new JLabel(inc.getRiskLevel() != null ? inc.getRiskLevel().getDisplayName() : "–");
        badge.setFont(Theme.FONT_SMALL);
        badge.setOpaque(true);
        badge.setBackground(inc.getRiskLevel() != null
                ? Theme.riskColor(inc.getRiskLevel().getDisplayName()) : Theme.TEXT_SECONDARY);
        badge.setForeground(Theme.BG_DARK);
        badge.setBorder(new EmptyBorder(2, 8, 2, 8));
        badge.setPreferredSize(new Dimension(70, 22));
        badge.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel titleLbl = UIFactory.bodyLabel(inc.getTitle());
        JLabel meta     = UIFactory.mutedLabel(inc.getType().getDisplayName()
                + "  •  " + inc.getReportedAt().format(DT_FMT));

        JPanel left = new JPanel(new BorderLayout(0, 2));
        left.setOpaque(false);
        left.add(titleLbl, BorderLayout.NORTH);
        left.add(meta,     BorderLayout.SOUTH);

        row.add(badge, BorderLayout.WEST);
        row.add(left,  BorderLayout.CENTER);
        return row;
    }
}

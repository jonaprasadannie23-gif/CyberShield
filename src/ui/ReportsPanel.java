package ui;

import model.*;
import service.ReportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.Map;

/**
 * Reports and Statistics screen.
 * All values are computed from live data via ReportService.
 * Uses native Swing components – no external chart libraries.
 */
public class ReportsPanel extends JPanel {

    private final model.User currentUser;
    private final ReportService reportService;

    // Live-updated components
    private JLabel totalIncLbl, resolvedLbl, unresolvedLbl;
    private JLabel totalTasksLbl, completedTasksLbl, pendingTasksLbl;
    private JProgressBar taskProgressBar;
    private DefaultTableModel riskTableModel;
    private DefaultTableModel typeTableModel;
    private JPanel riskBarsPanel;
    private JPanel typeBarsPanel;

    public ReportsPanel(model.User user) {
        this.currentUser   = user;
        this.reportService = AppContext.getInstance().getReportService();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_PANEL);
        setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel header = UIFactory.titleLabel("📈  Reports & Statistics");
        JLabel sub    = UIFactory.mutedLabel("All figures calculated from your actual stored data.");
        JPanel hdr    = new JPanel(new BorderLayout(0, 4));
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(0, 0, 20, 0));
        hdr.add(header, BorderLayout.NORTH);
        hdr.add(sub,    BorderLayout.SOUTH);

        // Summary row
        JPanel summaryRow = buildSummaryRow();

        // Middle section: Risk table + Type table
        JPanel tables = new JPanel(new GridLayout(1, 2, 20, 0));
        tables.setOpaque(false);
        tables.setBorder(new EmptyBorder(16, 0, 16, 0));
        tables.add(buildRiskSection());
        tables.add(buildTypeSection());

        // Task stats
        JPanel taskStats = buildTaskSection();

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setOpaque(false);
        content.add(summaryRow, BorderLayout.NORTH);
        content.add(tables,     BorderLayout.CENTER);
        content.add(taskStats,  BorderLayout.SOUTH);

        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setOpaque(false);
        outer.add(hdr,     BorderLayout.NORTH);
        outer.add(content, BorderLayout.CENTER);

        JScrollPane scroll = UIFactory.scrollPane(outer);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildSummaryRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        totalIncLbl   = new JLabel("0");
        resolvedLbl   = new JLabel("0");
        unresolvedLbl = new JLabel("0");

        row.add(buildStatCard(totalIncLbl,   "Total Incidents",  Theme.ACCENT_BLUE));
        row.add(buildStatCard(resolvedLbl,    "Resolved",         Theme.RISK_LOW));
        row.add(buildStatCard(unresolvedLbl,  "Unresolved",       Theme.RISK_MEDIUM));
        return row;
    }

    private JPanel buildRiskSection() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Theme.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = UIFactory.subtitleLabel("Incidents by Risk Level");
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        // Table
        riskTableModel = new DefaultTableModel(new String[]{"Risk Level", "Count"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable riskTable = buildSmallTable(riskTableModel);

        // Bar chart panel
        riskBarsPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        riskBarsPanel.setBackground(Theme.BG_CARD);

        p.add(title,       BorderLayout.NORTH);
        p.add(UIFactory.scrollPane(riskTable), BorderLayout.CENTER);
        p.add(riskBarsPanel, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildTypeSection() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Theme.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = UIFactory.subtitleLabel("Incidents by Category");
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        typeTableModel = new DefaultTableModel(new String[]{"Category", "Count"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable typeTable = buildSmallTable(typeTableModel);

        typeBarsPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        typeBarsPanel.setBackground(Theme.BG_CARD);

        p.add(title,       BorderLayout.NORTH);
        p.add(UIFactory.scrollPane(typeTable), BorderLayout.CENTER);
        p.add(typeBarsPanel, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildTaskSection() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Theme.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = UIFactory.subtitleLabel("Safety Task Progress");
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        totalTasksLbl     = new JLabel("0");
        completedTasksLbl = new JLabel("0");
        pendingTasksLbl   = new JLabel("0");

        JPanel taskCards = new JPanel(new GridLayout(1, 3, 12, 0));
        taskCards.setOpaque(false);
        taskCards.add(buildStatCard(totalTasksLbl,     "Total Tasks",    Theme.ACCENT_BLUE));
        taskCards.add(buildStatCard(completedTasksLbl, "Completed",      Theme.RISK_LOW));
        taskCards.add(buildStatCard(pendingTasksLbl,   "Pending",        Theme.RISK_MEDIUM));

        taskProgressBar = new JProgressBar(0, 100);
        taskProgressBar.setStringPainted(true);
        taskProgressBar.setForeground(Theme.ACCENT_TEAL);
        taskProgressBar.setBackground(Theme.BG_DARK);
        taskProgressBar.setBorderPainted(false);
        taskProgressBar.setPreferredSize(new Dimension(0, 18));

        p.add(title,           BorderLayout.NORTH);
        p.add(taskCards,       BorderLayout.CENTER);
        p.add(taskProgressBar, BorderLayout.SOUTH);
        return p;
    }

    public void refresh() {
        String uid = currentUser.getUserId();

        // Summary
        int total    = reportService.getTotalIncidents(uid);
        long resolved = reportService.getResolvedCount(uid);
        long unresolved = reportService.getUnresolvedCount(uid);

        totalIncLbl.setText(String.valueOf(total));
        resolvedLbl.setText(String.valueOf(resolved));
        unresolvedLbl.setText(String.valueOf(unresolved));

        // Risk table + bars
        riskTableModel.setRowCount(0);
        riskBarsPanel.removeAll();
        Map<RiskLevel, Long> riskMap = reportService.getCountByRiskLevel(uid);
        long maxRisk = riskMap.values().stream().mapToLong(Long::longValue).max().orElse(1);
        for (Map.Entry<RiskLevel, Long> e : riskMap.entrySet()) {
            riskTableModel.addRow(new Object[]{e.getKey().getDisplayName(), e.getValue()});
            riskBarsPanel.add(buildBarRow(e.getKey().getDisplayName(),
                    e.getValue(), maxRisk, Theme.riskColor(e.getKey().getDisplayName())));
        }

        // Type table + bars
        typeTableModel.setRowCount(0);
        typeBarsPanel.removeAll();
        Map<IncidentType, Long> typeMap = reportService.getCountByType(uid);
        long maxType = typeMap.values().stream().mapToLong(Long::longValue).max().orElse(1);
        for (Map.Entry<IncidentType, Long> e : typeMap.entrySet()) {
            typeTableModel.addRow(new Object[]{e.getKey().getDisplayName(), e.getValue()});
            typeBarsPanel.add(buildBarRow(e.getKey().getDisplayName(),
                    e.getValue(), maxType, Theme.ACCENT_BLUE));
        }

        // Task stats
        int ttasks = reportService.getTotalTasks(uid);
        int tdone  = reportService.getCompletedTasks(uid);
        int tpend  = reportService.getPendingTasks(uid);
        int pct    = ttasks == 0 ? 0 : (int)(tdone * 100.0 / ttasks);

        totalTasksLbl.setText(String.valueOf(ttasks));
        completedTasksLbl.setText(String.valueOf(tdone));
        pendingTasksLbl.setText(String.valueOf(tpend));
        taskProgressBar.setValue(pct);
        taskProgressBar.setString(pct + "% complete");

        riskBarsPanel.revalidate();
        riskBarsPanel.repaint();
        typeBarsPanel.revalidate();
        typeBarsPanel.repaint();
    }

    // ---- Helpers ----

    private JPanel buildBarRow(String label, long value, long max, Color color) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(120, 16));

        JProgressBar bar = new JProgressBar(0, (int) Math.max(max, 1));
        bar.setValue((int) value);
        bar.setStringPainted(true);
        bar.setString(String.valueOf(value));
        bar.setForeground(color);
        bar.setBackground(Theme.BG_DARK);
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(0, 16));

        row.add(lbl, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        return row;
    }

    private JPanel buildStatCard(JLabel valueLbl, String name, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Theme.BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLbl.setForeground(color);
        valueLbl.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel nameLbl = new JLabel(name, SwingConstants.CENTER);
        nameLbl.setFont(Theme.FONT_SMALL);
        nameLbl.setForeground(Theme.TEXT_SECONDARY);
        card.add(valueLbl, BorderLayout.CENTER);
        card.add(nameLbl,  BorderLayout.SOUTH);
        return card;
    }

    private JTable buildSmallTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setBackground(Theme.BG_CARD);
        t.setForeground(Theme.TEXT_PRIMARY);
        t.setFont(Theme.FONT_BODY);
        t.setRowHeight(26);
        t.setGridColor(Theme.BORDER);
        t.setShowVerticalLines(false);
        t.getTableHeader().setBackground(Theme.BG_SIDEBAR);
        t.getTableHeader().setForeground(Theme.TEXT_SECONDARY);
        t.getTableHeader().setFont(Theme.FONT_LABEL);
        return t;
    }
}

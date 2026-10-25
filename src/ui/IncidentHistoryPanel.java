package ui;

import model.*;
import service.IncidentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Full incident management screen with search, filter, update, and delete.
 */
public class IncidentHistoryPanel extends JPanel {

    private final User currentUser;
    private final IncidentService incidentService;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private JTextField searchField;
    private JComboBox<String> typeFilter;
    private JComboBox<String> riskFilter;
    private JComboBox<String> statusFilter;
    private JTable table;
    private DefaultTableModel tableModel;

    private static final String[] COLUMNS =
            {"ID", "Title", "Type", "Source", "Date", "Risk", "Status"};

    public IncidentHistoryPanel(User user) {
        this.currentUser     = user;
        this.incidentService = AppContext.getInstance().getIncidentService();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_PANEL);
        setBorder(new EmptyBorder(28, 32, 28, 32));

        // Header
        JLabel header = UIFactory.titleLabel("📋  Incident History");
        JLabel sub    = UIFactory.mutedLabel("View, search, filter and manage your reported incidents.");
        JPanel hdr    = new JPanel(new BorderLayout(0, 4));
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(0, 0, 18, 0));
        hdr.add(header, BorderLayout.NORTH);
        hdr.add(sub,    BorderLayout.SOUTH);

        // Filter bar
        JPanel filterBar = buildFilterBar();

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();
        JScrollPane scroll = UIFactory.scrollPane(table);

        // Action buttons
        JPanel actionBar = buildActionBar();

        // Assemble
        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(hdr,       BorderLayout.NORTH);
        top.add(filterBar, BorderLayout.SOUTH);

        add(top,       BorderLayout.NORTH);
        add(scroll,    BorderLayout.CENTER);
        add(actionBar, BorderLayout.SOUTH);
    }

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        bar.setBackground(Theme.BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        searchField = UIFactory.textField(18);
        searchField.putClientProperty("JTextField.placeholderText", "Search by title or description…");
        bar.add(UIFactory.bodyLabel("Search:"));
        bar.add(searchField);

        typeFilter = buildFilterCombo("All Types",
                java.util.Arrays.stream(IncidentType.values())
                        .map(IncidentType::getDisplayName).toArray(String[]::new));
        riskFilter = buildFilterCombo("All Risks",
                java.util.Arrays.stream(RiskLevel.values())
                        .map(RiskLevel::getDisplayName).toArray(String[]::new));
        statusFilter = buildFilterCombo("All Statuses",
                java.util.Arrays.stream(IncidentStatus.values())
                        .map(IncidentStatus::getDisplayName).toArray(String[]::new));

        bar.add(UIFactory.bodyLabel("Type:"));   bar.add(typeFilter);
        bar.add(UIFactory.bodyLabel("Risk:"));   bar.add(riskFilter);
        bar.add(UIFactory.bodyLabel("Status:")); bar.add(statusFilter);

        JButton applyBtn = UIFactory.secondaryButton("Apply");
        JButton clearBtn = UIFactory.secondaryButton("Clear");
        bar.add(applyBtn);
        bar.add(clearBtn);

        applyBtn.addActionListener(e -> refresh());
        clearBtn.addActionListener(e -> { searchField.setText(""); typeFilter.setSelectedIndex(0);
                                          riskFilter.setSelectedIndex(0); statusFilter.setSelectedIndex(0);
                                          refresh(); });
        return bar;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(Theme.BG_PANEL);

        JButton viewBtn   = UIFactory.secondaryButton("View Details");
        JButton editBtn   = UIFactory.secondaryButton("Edit Status");
        JButton deleteBtn = UIFactory.dangerButton("Delete");

        bar.add(viewBtn);
        bar.add(editBtn);
        bar.add(deleteBtn);

        viewBtn.addActionListener(e   -> viewSelected());
        editBtn.addActionListener(e   -> editSelectedStatus());
        deleteBtn.addActionListener(e -> deleteSelected());
        return bar;
    }

    public void refresh() {
        tableModel.setRowCount(0);

        String keyword       = searchField.getText().trim();
        String typeStr       = (String) typeFilter.getSelectedItem();
        String riskStr       = (String) riskFilter.getSelectedItem();
        String statusStr     = (String) statusFilter.getSelectedItem();

        List<Incident> incidents;

        if (!keyword.isEmpty()) {
            incidents = incidentService.searchIncidents(currentUser.getUserId(), keyword);
        } else {
            incidents = incidentService.getIncidentsByUser(currentUser.getUserId());
        }

        // Apply filters
        if (!"All Types".equals(typeStr)) {
            IncidentType ft = findEnum(IncidentType.values(), typeStr);
            if (ft != null) incidents = incidents.stream().filter(i -> i.getType() == ft).collect(Collectors.toList());
        }
        if (!"All Risks".equals(riskStr)) {
            RiskLevel fr = findEnum(RiskLevel.values(), riskStr);
            if (fr != null) incidents = incidents.stream().filter(i -> i.getRiskLevel() == fr).collect(Collectors.toList());
        }
        if (!"All Statuses".equals(statusStr)) {
            IncidentStatus fs = findEnum(IncidentStatus.values(), statusStr);
            if (fs != null) incidents = incidents.stream().filter(i -> i.getStatus() == fs).collect(Collectors.toList());
        }

        for (Incident inc : incidents) {
            tableModel.addRow(new Object[]{
                inc.getIncidentId(),
                inc.getTitle(),
                inc.getType().getDisplayName(),
                inc.getSource().getDisplayName(),
                inc.getIncidentDate() != null ? inc.getIncidentDate().format(FMT) : "",
                inc.getRiskLevel() != null ? inc.getRiskLevel().getDisplayName() : "–",
                inc.getStatus().getDisplayName()
            });
        }
    }

    private void viewSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo("Select an incident row first."); return; }
        String id = (String) tableModel.getValueAt(row, 0);
        incidentService.getIncidentById(id).ifPresent(this::showDetailDialog);
    }

    private void showDetailDialog(Incident inc) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.BG_CARD);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        addDetail(p, "ID",          inc.getIncidentId());
        addDetail(p, "Title",       inc.getTitle());
        addDetail(p, "Type",        inc.getType().getDisplayName());
        addDetail(p, "Source",      inc.getSource().getDisplayName());
        addDetail(p, "Date",        inc.getIncidentDate() != null ? inc.getIncidentDate().format(FMT) : "–");
        addDetail(p, "Risk Level",  inc.getRiskLevel() != null ? inc.getRiskLevel().getDisplayName() : "–");
        addDetail(p, "Status",      inc.getStatus().getDisplayName());
        addDetail(p, "Description", inc.getDescription());

        if (inc.getSuspiciousUrl() != null && !inc.getSuspiciousUrl().isBlank()) {
            addDetail(p, "Suspicious URL", inc.getSuspiciousUrl() + "  (text record only – not a link)");
        }

        // Assessment reasons
        if (!inc.getAssessmentReasons().isEmpty()) {
            p.add(Box.createVerticalStrut(8));
            p.add(labelLine("Assessment Reasons:"));
            inc.getAssessmentReasons().forEach(r -> p.add(bodyLine("  • " + r)));
        }

        // Recommendations
        if (!inc.getRecommendations().isEmpty()) {
            p.add(Box.createVerticalStrut(8));
            p.add(labelLine("Recommended Actions:"));
            inc.getRecommendations().forEach(r -> p.add(bodyLine("  ✓ " + r)));
        }

        // Type-specific detail
        p.add(Box.createVerticalStrut(8));
        p.add(labelLine("Type-Specific Detail:"));
        p.add(bodyLine(inc.getTypeSpecificDetails()));

        JScrollPane sp = new JScrollPane(p);
        sp.setPreferredSize(new Dimension(550, 400));
        sp.getViewport().setBackground(Theme.BG_CARD);

        JOptionPane.showMessageDialog(this, sp,
                "Incident Details – " + inc.getTitle(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void editSelectedStatus() {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo("Select an incident row first."); return; }
        String id = (String) tableModel.getValueAt(row, 0);

        IncidentStatus[] statuses = IncidentStatus.values();
        String[] labels = java.util.Arrays.stream(statuses)
                .map(IncidentStatus::getDisplayName).toArray(String[]::new);

        String chosen = (String) JOptionPane.showInputDialog(this,
                "Select new status for this incident:",
                "Update Status", JOptionPane.PLAIN_MESSAGE,
                null, labels, labels[0]);
        if (chosen == null) return;

        IncidentStatus ns = findEnum(statuses, chosen);
        if (ns == null) return;

        try {
            incidentService.updateStatus(id, currentUser.getUserId(), ns);
            refresh();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo("Select an incident row first."); return; }
        String id    = (String) tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete incident:\n\"" + title + "\"?\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            incidentService.deleteIncident(id, currentUser.getUserId());
            refresh();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    // ---- Helpers ----

    private void styleTable() {
        table.setBackground(Theme.BG_CARD);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(30);
        table.setGridColor(Theme.BORDER);
        table.setSelectionBackground(Theme.ACCENT_HOVER);
        table.setSelectionForeground(Theme.ACCENT_TEAL);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader th = table.getTableHeader();
        th.setBackground(Theme.BG_SIDEBAR);
        th.setForeground(Theme.TEXT_SECONDARY);
        th.setFont(Theme.FONT_LABEL);
        th.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        // Colour renderer for Risk column (index 5, but shifted after hide → visible 4)
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(sel ? Theme.ACCENT_HOVER : Theme.BG_CARD);
                setForeground(Theme.riskColor(v != null ? v.toString() : ""));
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                return this;
            }
        });

        // Status colour renderer
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(sel ? Theme.ACCENT_HOVER : Theme.BG_CARD);
                setForeground(Theme.statusColor(v != null ? v.toString() : ""));
                return this;
            }
        });
    }

    private JComboBox<String> buildFilterCombo(String allLabel, String[] values) {
        String[] items = new String[values.length + 1];
        items[0] = allLabel;
        System.arraycopy(values, 0, items, 1, values.length);
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(Theme.FONT_SMALL);
        cb.setBackground(Theme.BG_INPUT);
        cb.setForeground(Theme.TEXT_PRIMARY);
        return cb;
    }

    private <T extends Enum<T>> T findEnum(T[] values, String displayName) {
        for (T v : values) {
            if (v.toString().equalsIgnoreCase(displayName)) return v;
        }
        return null;
    }

    private void addDetail(JPanel p, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        row.setOpaque(false);
        JLabel l = new JLabel(label + ":");
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(Theme.TEXT_SECONDARY);
        JLabel v = new JLabel(value != null ? value : "–");
        v.setFont(Theme.FONT_BODY);
        v.setForeground(Theme.TEXT_PRIMARY);
        row.add(l); row.add(v);
        p.add(row);
    }

    private JLabel labelLine(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }

    private JLabel bodyLine(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BODY);
        l.setForeground(Theme.TEXT_PRIMARY);
        return l;
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}

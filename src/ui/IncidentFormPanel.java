package ui;

import model.*;
import service.IncidentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Panel for reporting new cyber incidents.
 * After successful submission it shows the risk assessment result inline.
 */
public class IncidentFormPanel extends JPanel {

    private final User currentUser;
    private final DashboardFrame parentFrame;
    private final IncidentService incidentService;

    private JTextField titleField;
    private JComboBox<IncidentType> typeCombo;
    private JTextArea descArea;
    private JComboBox<IncidentSource> sourceCombo;
    private JTextField dateField;
    private JTextField urlField;
    private JPanel resultPanel;
    private JLabel errorLabel;

    public IncidentFormPanel(User user, DashboardFrame parent) {
        this.currentUser     = user;
        this.parentFrame     = parent;
        this.incidentService = AppContext.getInstance().getIncidentService();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_PANEL);
        setBorder(new EmptyBorder(28, 32, 28, 32));

        // ---- Header ----
        JLabel header = UIFactory.titleLabel("🚨  Report a Cyber Incident");
        JLabel sub    = UIFactory.mutedLabel("Fill in the details below. All fields marked * are required.");
        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);
        headerPanel.add(header, BorderLayout.NORTH);
        headerPanel.add(sub,    BorderLayout.SOUTH);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        // ---- Form ----
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_CARD);
        form.setBorder(new EmptyBorder(22, 28, 22, 28));

        GridBagConstraints lc = labelConstraints();
        GridBagConstraints fc = fieldConstraints();
        int row = 0;

        // Title
        form.add(requiredLabel("Incident Title"), lc(lc, row));
        titleField = UIFactory.textField(30);
        form.add(titleField, fc(fc, row++));

        // Type
        form.add(requiredLabel("Incident Type"), lc(lc, row));
        typeCombo = UIFactory.comboBox(IncidentType.values());
        form.add(typeCombo, fc(fc, row++));

        // Description
        form.add(requiredLabel("Description"), lc(lc, row));
        descArea = UIFactory.textArea(4, 30);
        JScrollPane descScroll = UIFactory.scrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(0, 90));
        form.add(descScroll, fc(fc, row++));

        // Source
        form.add(requiredLabel("Source"), lc(lc, row));
        sourceCombo = UIFactory.comboBox(IncidentSource.values());
        form.add(sourceCombo, fc(fc, row++));

        // Date
        form.add(requiredLabel("Date of Incident  (YYYY-MM-DD)"), lc(lc, row));
        dateField = UIFactory.textField(15);
        dateField.setText(LocalDate.now().toString());
        form.add(dateField, fc(fc, row++));

        // URL (optional)
        form.add(UIFactory.bodyLabel("Suspicious URL  (optional)"), lc(lc, row));
        urlField = UIFactory.textField(30);
        urlField.setToolTipText("Record the URL as text only. It will NOT be opened.");
        form.add(urlField, fc(fc, row++));

        // URL disclaimer
        JLabel urlNote = UIFactory.mutedLabel("⚠ The URL is stored as text only and will never be opened or resolved.");
        urlNote.setForeground(Theme.RISK_MEDIUM);
        GridBagConstraints nc = new GridBagConstraints();
        nc.gridx = 1; nc.gridy = row++; nc.fill = GridBagConstraints.HORIZONTAL;
        nc.insets = new Insets(0, 0, 8, 0);
        form.add(urlNote, nc);

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.RISK_HIGH);
        GridBagConstraints ec = new GridBagConstraints();
        ec.gridx = 0; ec.gridy = row++; ec.gridwidth = 2; ec.fill = GridBagConstraints.HORIZONTAL;
        form.add(errorLabel, ec);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        JButton submitBtn = UIFactory.primaryButton("Submit Report");
        JButton clearBtn  = UIFactory.secondaryButton("Clear Form");
        btnRow.add(submitBtn);
        btnRow.add(clearBtn);

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = 0; bc.gridy = row; bc.gridwidth = 2; bc.anchor = GridBagConstraints.WEST;
        bc.insets = new Insets(8, 0, 0, 0);
        form.add(btnRow, bc);

        submitBtn.addActionListener(e -> doSubmit());
        clearBtn.addActionListener(e  -> clearForm());

        // ---- Result panel (shown after submission) ----
        resultPanel = new JPanel(new BorderLayout());
        resultPanel.setOpaque(false);
        resultPanel.setVisible(false);

        // ---- Scroll wrapper ----
        JPanel inner = new JPanel(new BorderLayout(0, 16));
        inner.setOpaque(false);
        inner.add(headerPanel, BorderLayout.NORTH);
        inner.add(form,        BorderLayout.CENTER);
        inner.add(resultPanel, BorderLayout.SOUTH);

        JScrollPane scroll = UIFactory.scrollPane(inner);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
    }

    private void doSubmit() {
        String title       = titleField.getText().trim();
        IncidentType type  = (IncidentType) typeCombo.getSelectedItem();
        String desc        = descArea.getText().trim();
        IncidentSource src = (IncidentSource) sourceCombo.getSelectedItem();
        String dateStr     = dateField.getText().trim();
        String url         = urlField.getText().trim();

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            errorLabel.setText("Invalid date format. Use YYYY-MM-DD (e.g. 2026-10-12).");
            return;
        }

        try {
            Incident incident = incidentService.createIncident(
                    currentUser.getUserId(), title, type, desc, src, date, url);
            errorLabel.setText(" ");
            showAssessmentResult(incident);
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
        }
    }

    private void showAssessmentResult(Incident incident) {
        resultPanel.removeAll();

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(16, 22, 16, 22)
        ));

        // Success header
        JLabel ok = UIFactory.accentLabel("✓  Incident reported successfully!");
        ok.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ok.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Risk badge
        RiskLevel level = incident.getRiskLevel();
        Color riskColor = level != null ? Theme.riskColor(level.getDisplayName()) : Theme.TEXT_SECONDARY;
        JLabel riskLbl  = new JLabel("Risk Level: " + (level != null ? level.getDisplayName() : "Pending"));
        riskLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        riskLbl.setForeground(riskColor);
        riskLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Type-specific detail (Polymorphism demonstrated here)
        JLabel typeDetail = UIFactory.mutedLabel(incident.getTypeSpecificDetails());
        typeDetail.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Reasons
        JLabel reasonsHdr = UIFactory.subtitleLabel("Assessment Reasons:");
        reasonsHdr.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(ok);
        card.add(Box.createVerticalStrut(10));
        card.add(riskLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(typeDetail);
        card.add(Box.createVerticalStrut(12));
        card.add(reasonsHdr);
        for (String reason : incident.getAssessmentReasons()) {
            JLabel r = UIFactory.bodyLabel("  • " + reason);
            r.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(r);
        }

        // Recommendations
        card.add(Box.createVerticalStrut(12));
        JLabel recHdr = UIFactory.subtitleLabel("Recommended Actions:");
        recHdr.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(recHdr);
        for (String rec : incident.getRecommendations()) {
            JLabel r = new JLabel("  ✓ " + rec);
            r.setFont(Theme.FONT_BODY);
            r.setForeground(Theme.ACCENT_TEAL);
            r.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(r);
        }

        // Disclaimer
        card.add(Box.createVerticalStrut(12));
        JLabel disc = UIFactory.mutedLabel(
            "⚠ This assessment is based on the information provided. "
          + "CyberShield cannot definitively identify all threats.");
        disc.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(disc);

        // Button row
        card.add(Box.createVerticalStrut(14));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton viewHistBtn = UIFactory.secondaryButton("View Incident History");
        JButton newIncBtn   = UIFactory.primaryButton("Report Another Incident");
        btnRow.add(newIncBtn);
        btnRow.add(viewHistBtn);
        card.add(btnRow);

        newIncBtn.addActionListener(e -> { clearForm(); resultPanel.setVisible(false); });
        viewHistBtn.addActionListener(e -> parentFrame.navigateTo("history"));

        resultPanel.add(card, BorderLayout.CENTER);
        resultPanel.setVisible(true);
        resultPanel.revalidate();
        resultPanel.repaint();
    }

    private void clearForm() {
        titleField.setText("");
        typeCombo.setSelectedIndex(0);
        descArea.setText("");
        sourceCombo.setSelectedIndex(0);
        dateField.setText(LocalDate.now().toString());
        urlField.setText("");
        errorLabel.setText(" ");
        resultPanel.setVisible(false);
    }

    // ---- GridBag helpers ----

    private GridBagConstraints labelConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(0, 0, 14, 16);
        c.gridx  = 0;
        return c;
    }

    private GridBagConstraints fieldConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill   = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(0, 0, 14, 0);
        c.gridx  = 1;
        return c;
    }

    private GridBagConstraints lc(GridBagConstraints base, int row) {
        base.gridy = row;
        return base;
    }

    private GridBagConstraints fc(GridBagConstraints base, int row) {
        base.gridy = row;
        return base;
    }

    private JLabel requiredLabel(String text) {
        JLabel lbl = UIFactory.bodyLabel(text + " *");
        lbl.setForeground(Theme.TEXT_PRIMARY);
        return lbl;
    }
}

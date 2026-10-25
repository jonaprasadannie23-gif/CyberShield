package ui;

import model.SecurityTask;
import model.User;
import service.TaskService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Safety Task Manager panel – add, complete, edit, delete preventive tasks.
 */
public class TaskPanel extends JPanel {

    private final User currentUser;
    private final TaskService taskService;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private JPanel taskListPanel;
    private JLabel progressLabel;
    private JProgressBar progressBar;

    public TaskPanel(User user) {
        this.currentUser = user;
        this.taskService = AppContext.getInstance().getTaskService();
        initUI();
        refresh();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_PANEL);
        setBorder(new EmptyBorder(28, 32, 28, 32));

        // Header
        JLabel header = UIFactory.titleLabel("✅  Safety Task Manager");
        JLabel sub    = UIFactory.mutedLabel("Track and complete your personal security action items.");
        JPanel hdr    = new JPanel(new BorderLayout(0, 4));
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(0, 0, 16, 0));
        hdr.add(header, BorderLayout.NORTH);
        hdr.add(sub,    BorderLayout.SOUTH);

        // Progress bar
        JPanel progressPanel = new JPanel(new BorderLayout(10, 0));
        progressPanel.setBackground(Theme.BG_CARD);
        progressPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        progressLabel = UIFactory.bodyLabel("Task Progress: 0 / 0");
        progressBar   = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(Theme.ACCENT_TEAL);
        progressBar.setBackground(Theme.BG_DARK);
        progressBar.setBorderPainted(false);
        progressPanel.add(progressLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar,   BorderLayout.CENTER);

        // Add task row
        JPanel addRow = buildAddRow();

        // Task list
        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        taskListPanel.setBackground(Theme.BG_PANEL);
        JScrollPane scroll = UIFactory.scrollPane(taskListPanel);

        // Assemble top section
        JPanel topSection = new JPanel(new BorderLayout(0, 12));
        topSection.setOpaque(false);
        topSection.add(hdr,           BorderLayout.NORTH);
        topSection.add(progressPanel, BorderLayout.CENTER);
        topSection.add(addRow,        BorderLayout.SOUTH);

        add(topSection, BorderLayout.NORTH);
        add(scroll,     BorderLayout.CENTER);
    }

    private JPanel buildAddRow() {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(Theme.BG_CARD);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JTextField titleField = UIFactory.textField(25);
        titleField.putClientProperty("JTextField.placeholderText", "Enter new task title…");
        JTextField descField  = UIFactory.textField(30);
        descField.putClientProperty("JTextField.placeholderText", "Optional description…");
        JButton addBtn = UIFactory.primaryButton("Add Task");

        JPanel fields = new JPanel(new GridLayout(1, 2, 8, 0));
        fields.setOpaque(false);
        fields.add(titleField);
        fields.add(descField);

        row.add(UIFactory.bodyLabel("New Task:"), BorderLayout.WEST);
        row.add(fields,  BorderLayout.CENTER);
        row.add(addBtn,  BorderLayout.EAST);

        addBtn.addActionListener(e -> {
            String t = titleField.getText().trim();
            String d = descField.getText().trim();
            try {
                taskService.createTask(currentUser.getUserId(), t, d);
                titleField.setText("");
                descField.setText("");
                refresh();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        return row;
    }

    public void refresh() {
        taskListPanel.removeAll();
        List<SecurityTask> tasks = taskService.getTasksByUser(currentUser.getUserId());

        int total   = tasks.size();
        int done    = (int) tasks.stream().filter(SecurityTask::isCompleted).count();
        int percent = total == 0 ? 0 : (done * 100 / total);

        progressLabel.setText("Task Progress: " + done + " / " + total + " completed");
        progressBar.setValue(percent);
        progressBar.setString(percent + "%");

        if (tasks.isEmpty()) {
            JLabel empty = UIFactory.mutedLabel("  No tasks yet. Add a task above to get started.");
            empty.setBorder(new EmptyBorder(16, 4, 16, 4));
            taskListPanel.add(empty);
        } else {
            for (SecurityTask task : tasks) {
                taskListPanel.add(buildTaskRow(task));
                taskListPanel.add(UIFactory.separator());
            }
        }
        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    private JPanel buildTaskRow(SecurityTask task) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(task.isCompleted() ? Theme.BG_DARK : Theme.BG_CARD);
        row.setBorder(new EmptyBorder(10, 14, 10, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // Status indicator
        JLabel statusIcon = new JLabel(task.isCompleted() ? "✅" : "⬜");
        statusIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        statusIcon.setBorder(new EmptyBorder(0, 0, 0, 10));

        // Title and description
        JLabel titleLbl = new JLabel(task.getTitle());
        titleLbl.setFont(Theme.FONT_LABEL);
        titleLbl.setForeground(task.isCompleted() ? Theme.TEXT_SECONDARY : Theme.TEXT_PRIMARY);

        String metaText = task.getDescription().isBlank() ? "" : task.getDescription();
        if (task.isCompleted() && task.getCompletedAt() != null) {
            metaText += (metaText.isEmpty() ? "" : "  •  ")
                      + "Completed: " + task.getCompletedAt().format(FMT);
        }
        JLabel metaLbl = UIFactory.mutedLabel(metaText);

        JPanel info = new JPanel(new BorderLayout(0, 2));
        info.setOpaque(false);
        info.add(titleLbl, BorderLayout.NORTH);
        info.add(metaLbl,  BorderLayout.SOUTH);

        // Action buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setOpaque(false);

        if (!task.isCompleted()) {
            JButton completeBtn = UIFactory.primaryButton("Complete");
            completeBtn.setPreferredSize(new Dimension(90, 30));
            completeBtn.addActionListener(e -> {
                taskService.markCompleted(task.getTaskId(), currentUser.getUserId());
                refresh();
            });
            btnPanel.add(completeBtn);
        } else {
            JButton undoBtn = UIFactory.secondaryButton("Undo");
            undoBtn.setPreferredSize(new Dimension(70, 30));
            undoBtn.addActionListener(e -> {
                taskService.markPending(task.getTaskId(), currentUser.getUserId());
                refresh();
            });
            btnPanel.add(undoBtn);
        }

        JButton editBtn = UIFactory.secondaryButton("Edit");
        editBtn.setPreferredSize(new Dimension(60, 30));
        editBtn.addActionListener(e -> editTask(task));
        btnPanel.add(editBtn);

        JButton delBtn = UIFactory.dangerButton("Delete");
        delBtn.setPreferredSize(new Dimension(70, 30));
        delBtn.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this,
                    "Delete task \"" + task.getTitle() + "\"?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (c == JOptionPane.YES_OPTION) {
                taskService.deleteTask(task.getTaskId(), currentUser.getUserId());
                refresh();
            }
        });
        btnPanel.add(delBtn);

        row.add(statusIcon, BorderLayout.WEST);
        row.add(info,       BorderLayout.CENTER);
        row.add(btnPanel,   BorderLayout.EAST);
        return row;
    }

    private void editTask(SecurityTask task) {
        JTextField t = UIFactory.textField(25);
        t.setText(task.getTitle());
        JTextField d = UIFactory.textField(25);
        d.setText(task.getDescription());

        JPanel p = new JPanel(new GridLayout(4, 1, 4, 4));
        p.setBackground(Theme.BG_CARD);
        p.add(UIFactory.bodyLabel("Title:"));
        p.add(t);
        p.add(UIFactory.bodyLabel("Description:"));
        p.add(d);

        int result = JOptionPane.showConfirmDialog(this, p, "Edit Task",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                taskService.updateTask(task.getTaskId(), currentUser.getUserId(),
                        t.getText().trim(), d.getText().trim());
                refresh();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

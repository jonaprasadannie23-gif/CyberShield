package model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a preventive security task for the Safety Task Manager.
 *
 * OOP – Encapsulation: all fields private; validated setters prevent invalid state.
 */
public class SecurityTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private String taskId;
    private String userId;
    private String title;
    private String description;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String relatedIncidentId;   // optional – links task to an incident

    // --- Constructors ---

    public SecurityTask(String taskId, String userId, String title, String description) {
        this.taskId      = taskId;
        this.userId      = userId;
        this.title       = title;
        this.description = description;
        this.completed   = false;
        this.createdAt   = LocalDateTime.now();
    }

    public SecurityTask(String taskId, String userId, String title,
                        String description, String relatedIncidentId) {
        this(taskId, userId, title, description);
        this.relatedIncidentId = relatedIncidentId;
    }

    public SecurityTask() {}

    // --- Business methods ---

    public void markCompleted() {
        this.completed   = true;
        this.completedAt = LocalDateTime.now();
    }

    public void markPending() {
        this.completed   = false;
        this.completedAt = null;
    }

    // --- Getters ---

    public String getTaskId()             { return taskId; }
    public String getUserId()             { return userId; }
    public String getTitle()              { return title; }
    public String getDescription()        { return description; }
    public boolean isCompleted()          { return completed; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public String getRelatedIncidentId()  { return relatedIncidentId; }

    // --- Setters with validation ---

    public void setTitle(String title) {
        if (title != null && !title.isBlank()) this.title = title;
    }

    public void setDescription(String description) {
        if (description != null) this.description = description;
    }

    public void setRelatedIncidentId(String id) {
        this.relatedIncidentId = id;
    }

    @Override
    public String toString() {
        return "SecurityTask{id='" + taskId + "', title='" + title +
               "', completed=" + completed + "}";
    }
}

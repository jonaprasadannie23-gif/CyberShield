package com.cybershield.model;

import java.time.LocalDateTime;

/**
 * Preventive security task. OOP – Encapsulation via private fields and validated setters.
 */
public class SecurityTask {

    private String taskId;
    private String userId;
    private String title;
    private String description;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String relatedIncidentId;

    public SecurityTask() {}

    public SecurityTask(String taskId, String userId, String title, String description) {
        this.taskId = taskId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.completed = false;
        this.createdAt = LocalDateTime.now();
    }

    public void markCompleted() {
        this.completed = true;
        this.completedAt = LocalDateTime.now();
    }

    public void markPending() {
        this.completed = false;
        this.completedAt = null;
    }

    public String getTaskId() { return taskId; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return completed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public String getRelatedIncidentId() { return relatedIncidentId; }

    public void setTaskId(String taskId) { this.taskId = taskId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTitle(String title) {
        if (title != null && !title.isBlank()) this.title = title;
    }
    public void setDescription(String description) {
        if (description != null) this.description = description;
    }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public void setRelatedIncidentId(String relatedIncidentId) {
        this.relatedIncidentId = relatedIncidentId;
    }
}

package com.cybershield.web.dto;

public class TaskRequest {
    private String title;
    private String description;
    private String relatedIncidentId;
    private Boolean completed;

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getRelatedIncidentId() { return relatedIncidentId; }
    public Boolean getCompleted() { return completed; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setRelatedIncidentId(String relatedIncidentId) { this.relatedIncidentId = relatedIncidentId; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
}

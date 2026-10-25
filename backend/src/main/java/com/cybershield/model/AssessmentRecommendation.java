package com.cybershield.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A prioritized recommendation derived from the security assessment.
 * Can be directly converted into a preventive Safety Task.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssessmentRecommendation {

    private String recommendationId;
    private String practiceKey;
    private String title;
    private String priority; // HIGH, MEDIUM, LOW
    private String description;
    private String taskTitle;
    private String taskDescription;

    public AssessmentRecommendation() {}

    public AssessmentRecommendation(String recommendationId,
                                    String practiceKey,
                                    String title,
                                    String priority,
                                    String description,
                                    String taskTitle,
                                    String taskDescription) {
        this.recommendationId = recommendationId;
        this.practiceKey = practiceKey;
        this.title = title;
        this.priority = priority;
        this.description = description;
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
    }

    public String getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(String recommendationId) {
        this.recommendationId = recommendationId;
    }

    public String getPracticeKey() {
        return practiceKey;
    }

    public void setPracticeKey(String practiceKey) {
        this.practiceKey = practiceKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }
}

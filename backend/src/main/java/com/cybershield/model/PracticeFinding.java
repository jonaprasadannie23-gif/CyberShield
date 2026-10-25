package com.cybershield.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Detailed evaluation of an individual security practice.
 * Encapsulates the user's response, points earned, risk status, finding explanation,
 * and associated safety task recommendation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PracticeFinding {

    private String practiceKey;
    private String title;
    private String answerKey;
    private String answerLabel;
    private int score;
    private int maxScore;
    private String status; // SECURE, MODERATE, VULNERABLE, NEEDS_ATTENTION
    private String explanation;
    private String recommendation;
    private String taskTitle;
    private String taskDescription;

    public PracticeFinding() {}

    public PracticeFinding(String practiceKey,
                           String title,
                           String answerKey,
                           String answerLabel,
                           int score,
                           int maxScore,
                           String status,
                           String explanation,
                           String recommendation,
                           String taskTitle,
                           String taskDescription) {
        this.practiceKey = practiceKey;
        this.title = title;
        this.answerKey = answerKey;
        this.answerLabel = answerLabel;
        this.score = score;
        this.maxScore = maxScore;
        this.status = status;
        this.explanation = explanation;
        this.recommendation = recommendation;
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
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

    public String getAnswerKey() {
        return answerKey;
    }

    public void setAnswerKey(String answerKey) {
        this.answerKey = answerKey;
    }

    public String getAnswerLabel() {
        return answerLabel;
    }

    public void setAnswerLabel(String answerLabel) {
        this.answerLabel = answerLabel;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
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

package com.cybershield.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain entity representing a completed Personal Security Assessment.
 * OOP – Encapsulation: maintains complete audit record of user answers,
 * server-evaluated scores, practice findings, and generated recommendations.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityAssessment {

    private String assessmentId;
    private String userId;
    private int score;
    private AssessmentCategory category;
    private String categoryLabel;
    private String riskSummary;
    private String scoreInterpretation;
    private String disclaimer;
    private Map<String, String> answers = new HashMap<>();
    private List<PracticeFinding> findings = new ArrayList<>();
    private List<AssessmentRecommendation> recommendations = new ArrayList<>();
    private LocalDateTime createdAt;

    public SecurityAssessment() {}

    public SecurityAssessment(String assessmentId,
                              String userId,
                              int score,
                              AssessmentCategory category,
                              String categoryLabel,
                              String riskSummary,
                              String scoreInterpretation,
                              String disclaimer,
                              Map<String, String> answers,
                              List<PracticeFinding> findings,
                              List<AssessmentRecommendation> recommendations,
                              LocalDateTime createdAt) {
        this.assessmentId = assessmentId;
        this.userId = userId;
        this.score = score;
        this.category = category;
        this.categoryLabel = categoryLabel;
        this.riskSummary = riskSummary;
        this.scoreInterpretation = scoreInterpretation;
        this.disclaimer = disclaimer;
        if (answers != null) {
            this.answers = new HashMap<>(answers);
        }
        if (findings != null) {
            this.findings = new ArrayList<>(findings);
        }
        if (recommendations != null) {
            this.recommendations = new ArrayList<>(recommendations);
        }
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public String getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(String assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public AssessmentCategory getCategory() {
        return category;
    }

    public void setCategory(AssessmentCategory category) {
        this.category = category;
    }

    public String getCategoryLabel() {
        return categoryLabel;
    }

    public void setCategoryLabel(String categoryLabel) {
        this.categoryLabel = categoryLabel;
    }

    public String getRiskSummary() {
        return riskSummary;
    }

    public void setRiskSummary(String riskSummary) {
        this.riskSummary = riskSummary;
    }

    public String getScoreInterpretation() {
        return scoreInterpretation;
    }

    public void setScoreInterpretation(String scoreInterpretation) {
        this.scoreInterpretation = scoreInterpretation;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public Map<String, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, String> answers) {
        this.answers = answers != null ? new HashMap<>(answers) : new HashMap<>();
    }

    public List<PracticeFinding> getFindings() {
        return findings;
    }

    public void setFindings(List<PracticeFinding> findings) {
        this.findings = findings != null ? new ArrayList<>(findings) : new ArrayList<>();
    }

    public List<AssessmentRecommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<AssessmentRecommendation> recommendations) {
        this.recommendations = recommendations != null ? new ArrayList<>(recommendations) : new ArrayList<>();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

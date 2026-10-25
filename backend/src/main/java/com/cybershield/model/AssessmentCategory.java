package com.cybershield.model;

/**
 * Score categories and thresholds for Personal Security Assessments.
 * Defined in one centralized location to guarantee consistency across the system.
 */
public enum AssessmentCategory {
    EXCELLENT(
            85, 100,
            "Excellent Security Posture",
            "Strong Security Posture. Your proactive security habits provide robust defense against common cyber threats. Continue maintaining these practices."
    ),
    GOOD(
            70, 84,
            "Good Security Posture",
            "Moderate Security Posture. You have established solid foundational habits, but a few key gaps leave room for targeted risks. Addressing these areas will significantly strengthen your safety."
    ),
    FAIR(
            50, 69,
            "Fair Security Posture",
            "Basic Security Posture. Several critical practices require attention. Your accounts and devices may be exposed to common attack vectors such as credential stuffing or unpatched flaws."
    ),
    POOR(
            0, 49,
            "At-Risk Security Posture",
            "At-Risk Security Posture. Urgent action is recommended. Multiple fundamental security practices are missing or uncertain, leaving your personal information and accounts at high risk."
    );

    private final int minScore;
    private final int maxScore;
    private final String label;
    private final String summary;

    AssessmentCategory(int minScore, int maxScore, String label, String summary) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.label = label;
        this.summary = summary;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public String getLabel() {
        return label;
    }

    public String getSummary() {
        return summary;
    }

    /**
     * Maps an integer score (0-100) to its corresponding category.
     * Boundary values: 0-49 POOR, 50-69 FAIR, 70-84 GOOD, 85-100 EXCELLENT.
     */
    public static AssessmentCategory fromScore(int score) {
        if (score >= 85) {
            return EXCELLENT;
        } else if (score >= 70) {
            return GOOD;
        } else if (score >= 50) {
            return FAIR;
        } else {
            return POOR;
        }
    }
}

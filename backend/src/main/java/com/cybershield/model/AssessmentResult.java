package com.cybershield.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable value object returned by the risk assessor.
 * OOP – Encapsulation: fields are final after construction.
 */
public class AssessmentResult {

    private final RiskLevel riskLevel;
    private final List<String> reasons;
    private final List<String> recommendations;
    private final String disclaimer;

    public AssessmentResult(RiskLevel riskLevel,
                            List<String> reasons,
                            List<String> recommendations) {
        this.riskLevel = riskLevel;
        this.reasons = new ArrayList<>(reasons);
        this.recommendations = new ArrayList<>(recommendations);
        this.disclaimer = "Note: This assessment is based on the information you provided. "
                + "CyberShield cannot definitively identify all scams or threats. "
                + "When in doubt, contact official support channels.";
    }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public List<String> getReasons() { return new ArrayList<>(reasons); }
    public List<String> getRecommendations() { return new ArrayList<>(recommendations); }
    public String getDisclaimer() { return disclaimer; }
}

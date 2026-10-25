package model;

import java.util.List;
import java.util.ArrayList;

/**
 * Value object returned by the RiskAssessmentService.
 * Contains the computed risk level, all reasons that contributed to it,
 * and the recommended preventive actions.
 *
 * OOP – Encapsulation: immutable after construction; fields exposed via getters only.
 */
public class AssessmentResult {

    private final RiskLevel riskLevel;
    private final List<String> reasons;
    private final List<String> recommendations;
    private final String disclaimer;

    public AssessmentResult(RiskLevel riskLevel,
                            List<String> reasons,
                            List<String> recommendations) {
        this.riskLevel       = riskLevel;
        this.reasons         = new ArrayList<>(reasons);
        this.recommendations = new ArrayList<>(recommendations);
        this.disclaimer      = "Note: This assessment is based on the information you provided. "
                             + "CyberShield cannot definitively identify all scams or threats. "
                             + "When in doubt, contact official support channels.";
    }

    public RiskLevel getRiskLevel()          { return riskLevel; }
    public List<String> getReasons()         { return new ArrayList<>(reasons); }
    public List<String> getRecommendations() { return new ArrayList<>(recommendations); }
    public String getDisclaimer()            { return disclaimer; }

    @Override
    public String toString() {
        return "AssessmentResult{level=" + riskLevel + ", reasons=" + reasons.size() + "}";
    }
}

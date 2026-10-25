package service;

import model.AssessmentResult;
import model.Incident;

/**
 * Abstraction interface for risk assessment engines.
 *
 * OOP – Abstraction: defines the contract for any rule-based assessor.
 * The concrete implementation is RiskAssessmentService.  Future implementations
 * (e.g., ML-based) can be swapped in without changing callers.
 */
public interface RiskAssessor {

    /**
     * Analyses the supplied incident and returns a risk assessment containing:
     * - computed RiskLevel
     * - reasons that influenced the score
     * - recommended preventive actions
     *
     * @param incident  the incident to assess (any subtype via polymorphism)
     * @return          the assessment result
     */
    AssessmentResult assess(Incident incident);
}

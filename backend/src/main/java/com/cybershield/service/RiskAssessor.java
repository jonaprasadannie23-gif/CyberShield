package com.cybershield.service;

import com.cybershield.model.AssessmentResult;
import com.cybershield.model.Incident;

/**
 * OOP – Abstraction: assessment contract implemented by RiskAssessmentService.
 */
public interface RiskAssessor {

    AssessmentResult assess(Incident incident);
}

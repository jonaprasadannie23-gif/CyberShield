package com.cybershield.web;

import com.cybershield.exception.NotFoundException;
import com.cybershield.model.SecurityAssessment;
import com.cybershield.service.PersonalSecurityAssessmentService;
import com.cybershield.web.dto.AssessmentRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for Personal Security Assessment questionnaire, scoring, and history.
 * Enforces session authentication and strict user data isolation.
 */
@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final PersonalSecurityAssessmentService assessmentService;

    public AssessmentController(PersonalSecurityAssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    /**
     * Submits security practice answers, runs rule-based backend scoring engine,
     * persists the assessment, and returns the result.
     */
    @PostMapping
    public ResponseEntity<SecurityAssessment> createAssessment(@RequestBody AssessmentRequest request,
                                                              HttpSession session) {
        String userId = SessionUser.requireUserId(session);
        SecurityAssessment result = assessmentService.evaluateAndSave(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Retrieves the list of past assessments for the currently authenticated user.
     */
    @GetMapping
    public List<SecurityAssessment> listAssessments(HttpSession session) {
        String userId = SessionUser.requireUserId(session);
        return assessmentService.getUserAssessments(userId);
    }

    /**
     * Retrieves the most recent assessment for the currently authenticated user.
     */
    @GetMapping("/latest")
    public ResponseEntity<SecurityAssessment> getLatestAssessment(HttpSession session) {
        String userId = SessionUser.requireUserId(session);
        SecurityAssessment latest = assessmentService.getLatestAssessment(userId);
        if (latest == null) {
            throw new NotFoundException("No assessments recorded yet.");
        }
        return ResponseEntity.ok(latest);
    }

    /**
     * Retrieves a single assessment by ID, strictly enforcing user ownership.
     */
    @GetMapping("/{id}")
    public SecurityAssessment getAssessmentById(@PathVariable String id, HttpSession session) {
        String userId = SessionUser.requireUserId(session);
        return assessmentService.getAssessment(id, userId);
    }
}

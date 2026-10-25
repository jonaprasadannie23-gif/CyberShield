package com.cybershield.service;

import com.cybershield.exception.ForbiddenException;
import com.cybershield.exception.NotFoundException;
import com.cybershield.model.AssessmentCategory;
import com.cybershield.model.PracticeFinding;
import com.cybershield.model.SecurityAssessment;
import com.cybershield.repository.AssessmentRepository;
import com.cybershield.storage.JsonFileStorage;
import com.cybershield.web.dto.AssessmentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersonalSecurityAssessmentServiceTest {

    private AssessmentRepository repository;
    private PersonalSecurityAssessmentService service;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        JsonFileStorage storage = new JsonFileStorage(tempDir.toString(), mapper);
        repository = new AssessmentRepository(storage);
        service = new PersonalSecurityAssessmentService(repository);
    }

    private AssessmentRequest createPerfectRequest() {
        return new AssessmentRequest(
                "ALWAYS",             // 15
                "NEVER",              // 15
                "MANAGER_OR_STRONG",  // 15
                "AUTOMATIC",          // 10
                "ALWAYS_VERIFY",      // 15
                "UP_TO_DATE_SECURE",  // 10
                "ALWAYS_PROTECTED",   // 10
                "VERY_CONFIDENT"      // 10
        );
    }

    private AssessmentRequest createWorstRequest() {
        return new AssessmentRequest(
                "NEVER",              // 0
                "FREQUENTLY",         // 0
                "SIMPLE_PATTERNS",    // 0
                "DELAYED",            // 0
                "RARELY_VERIFY",      // 0
                "OUTDATED_OR_WEAK",   // 0
                "NO_PRECAUTIONS",     // 0
                "NOT_CONFIDENT"       // 0
        );
    }

    private AssessmentRequest createAllNotSureRequest() {
        return new AssessmentRequest(
                "NOT_SURE", // 4
                "NOT_SURE", // 2
                "NOT_SURE", // 4
                "NOT_SURE", // 3
                "NOT_SURE", // 3
                "NOT_SURE", // 2
                "NOT_SURE", // 2
                "NOT_SURE"  // 2
        );
    }

    // ── 1. Perfect & Worst Scores (Boundary testing) ───────────────────────────

    @Test
    void testPerfectScoreGives100AndExcellentCategory() {
        AssessmentRequest req = createPerfectRequest();
        SecurityAssessment assessment = service.evaluateAndSave("user-1", req);

        assertEquals(100, assessment.getScore());
        assertEquals(AssessmentCategory.EXCELLENT, assessment.getCategory());
        assertTrue(assessment.getFindings().stream().allMatch(f -> "SECURE".equals(f.getStatus())));
        assertTrue(assessment.getRecommendations().isEmpty(), "No recommendations needed for 100% secure practices");
        assertNotNull(assessment.getDisclaimer());
        assertTrue(assessment.getDisclaimer().contains("educational indicator"));
    }

    @Test
    void testWorstScoreGivesZeroAndPoorCategory() {
        AssessmentRequest req = createWorstRequest();
        SecurityAssessment assessment = service.evaluateAndSave("user-1", req);

        assertEquals(0, assessment.getScore());
        assertEquals(AssessmentCategory.POOR, assessment.getCategory());
        assertTrue(assessment.getFindings().stream().allMatch(f -> "VULNERABLE".equals(f.getStatus())));
        assertEquals(8, assessment.getRecommendations().size());
        assertTrue(assessment.getRecommendations().stream().allMatch(r -> "HIGH".equals(r.getPriority())));
    }

    // ── 2. "Not Sure" handling ─────────────────────────────────────────────────

    @Test
    void testNotSureAnswersAreExplicitlyScoredAndNotTreatedAsSecure() {
        AssessmentRequest req = createAllNotSureRequest();
        SecurityAssessment assessment = service.evaluateAndSave("user-1", req);

        // Sum = 4 + 2 + 4 + 3 + 3 + 2 + 2 + 2 = 22
        assertEquals(22, assessment.getScore());
        assertEquals(AssessmentCategory.POOR, assessment.getCategory());

        // Every finding should be NEEDS_ATTENTION
        for (PracticeFinding finding : assessment.getFindings()) {
            assertEquals("NEEDS_ATTENTION", finding.getStatus());
            assertNotNull(finding.getRecommendation());
            assertNotNull(finding.getTaskTitle());
        }

        // Recommendations should be generated for all 8 areas with MEDIUM priority
        assertEquals(8, assessment.getRecommendations().size());
        assertTrue(assessment.getRecommendations().stream().allMatch(r -> "MEDIUM".equals(r.getPriority())));
    }

    // ── 3. Category Threshold Boundaries ───────────────────────────────────────

    @Test
    void testCategoryThresholds() {
        assertEquals(AssessmentCategory.EXCELLENT, AssessmentCategory.fromScore(100));
        assertEquals(AssessmentCategory.EXCELLENT, AssessmentCategory.fromScore(85));
        assertEquals(AssessmentCategory.GOOD, AssessmentCategory.fromScore(84));
        assertEquals(AssessmentCategory.GOOD, AssessmentCategory.fromScore(70));
        assertEquals(AssessmentCategory.FAIR, AssessmentCategory.fromScore(69));
        assertEquals(AssessmentCategory.FAIR, AssessmentCategory.fromScore(50));
        assertEquals(AssessmentCategory.POOR, AssessmentCategory.fromScore(49));
        assertEquals(AssessmentCategory.POOR, AssessmentCategory.fromScore(0));
    }

    // ── 4. Practice-by-practice rules ──────────────────────────────────────────

    @Test
    void testTwoFactorAuthScoring() {
        // ALWAYS = 15
        AssessmentRequest r1 = createPerfectRequest();
        r1.setTwoFactorAuth("ALWAYS");
        assertEquals(15, service.evaluateAndSave("u", r1).getFindings().get(0).getScore());

        // SOMETIMES = 8
        AssessmentRequest r2 = createPerfectRequest();
        r2.setTwoFactorAuth("SOMETIMES");
        assertEquals(8, service.evaluateAndSave("u", r2).getFindings().get(0).getScore());

        // NEVER = 0
        AssessmentRequest r3 = createPerfectRequest();
        r3.setTwoFactorAuth("NEVER");
        assertEquals(0, service.evaluateAndSave("u", r3).getFindings().get(0).getScore());

        // NOT_SURE = 4
        AssessmentRequest r4 = createPerfectRequest();
        r4.setTwoFactorAuth("NOT_SURE");
        assertEquals(4, service.evaluateAndSave("u", r4).getFindings().get(0).getScore());
    }

    @Test
    void testPasswordReuseScoring() {
        // NEVER = 15
        AssessmentRequest r1 = createPerfectRequest();
        r1.setPasswordReuse("NEVER");
        assertEquals(15, service.evaluateAndSave("u", r1).getFindings().get(1).getScore());

        // RARELY = 10
        AssessmentRequest r2 = createPerfectRequest();
        r2.setPasswordReuse("RARELY");
        assertEquals(10, service.evaluateAndSave("u", r2).getFindings().get(1).getScore());

        // SOMETIMES = 4
        AssessmentRequest r3 = createPerfectRequest();
        r3.setPasswordReuse("SOMETIMES");
        assertEquals(4, service.evaluateAndSave("u", r3).getFindings().get(1).getScore());

        // FREQUENTLY = 0
        AssessmentRequest r4 = createPerfectRequest();
        r4.setPasswordReuse("FREQUENTLY");
        assertEquals(0, service.evaluateAndSave("u", r4).getFindings().get(1).getScore());

        // NOT_SURE = 2
        AssessmentRequest r5 = createPerfectRequest();
        r5.setPasswordReuse("NOT_SURE");
        assertEquals(2, service.evaluateAndSave("u", r5).getFindings().get(1).getScore());
    }

    @Test
    void testStrongPasswordsScoring() {
        // MANAGER_OR_STRONG = 15
        AssessmentRequest r1 = createPerfectRequest();
        r1.setStrongPasswords("MANAGER_OR_STRONG");
        assertEquals(15, service.evaluateAndSave("u", r1).getFindings().get(2).getScore());

        // MEMORIZED_COMPLEX = 10
        AssessmentRequest r2 = createPerfectRequest();
        r2.setStrongPasswords("MEMORIZED_COMPLEX");
        assertEquals(10, service.evaluateAndSave("u", r2).getFindings().get(2).getScore());

        // SIMPLE_PATTERNS = 0
        AssessmentRequest r3 = createPerfectRequest();
        r3.setStrongPasswords("SIMPLE_PATTERNS");
        assertEquals(0, service.evaluateAndSave("u", r3).getFindings().get(2).getScore());

        // NOT_SURE = 4
        AssessmentRequest r4 = createPerfectRequest();
        r4.setStrongPasswords("NOT_SURE");
        assertEquals(4, service.evaluateAndSave("u", r4).getFindings().get(2).getScore());
    }

    @Test
    void testSystemUpdatesScoring() {
        // AUTOMATIC = 10
        AssessmentRequest r1 = createPerfectRequest();
        r1.setSystemUpdates("AUTOMATIC");
        assertEquals(10, service.evaluateAndSave("u", r1).getFindings().get(3).getScore());

        // REGULAR_MANUAL = 7
        AssessmentRequest r2 = createPerfectRequest();
        r2.setSystemUpdates("REGULAR_MANUAL");
        assertEquals(7, service.evaluateAndSave("u", r2).getFindings().get(3).getScore());

        // DELAYED = 0
        AssessmentRequest r3 = createPerfectRequest();
        r3.setSystemUpdates("DELAYED");
        assertEquals(0, service.evaluateAndSave("u", r3).getFindings().get(3).getScore());

        // NOT_SURE = 3
        AssessmentRequest r4 = createPerfectRequest();
        r4.setSystemUpdates("NOT_SURE");
        assertEquals(3, service.evaluateAndSave("u", r4).getFindings().get(3).getScore());
    }

    @Test
    void testLinkVerificationScoring() {
        // ALWAYS_VERIFY = 15
        AssessmentRequest r1 = createPerfectRequest();
        r1.setLinkVerification("ALWAYS_VERIFY");
        assertEquals(15, service.evaluateAndSave("u", r1).getFindings().get(4).getScore());

        // SOMETIMES_VERIFY = 6
        AssessmentRequest r2 = createPerfectRequest();
        r2.setLinkVerification("SOMETIMES_VERIFY");
        assertEquals(6, service.evaluateAndSave("u", r2).getFindings().get(4).getScore());

        // RARELY_VERIFY = 0
        AssessmentRequest r3 = createPerfectRequest();
        r3.setLinkVerification("RARELY_VERIFY");
        assertEquals(0, service.evaluateAndSave("u", r3).getFindings().get(4).getScore());

        // NOT_SURE = 3
        AssessmentRequest r4 = createPerfectRequest();
        r4.setLinkVerification("NOT_SURE");
        assertEquals(3, service.evaluateAndSave("u", r4).getFindings().get(4).getScore());
    }

    @Test
    void testRecoveryMethodsScoring() {
        // UP_TO_DATE_SECURE = 10
        AssessmentRequest r1 = createPerfectRequest();
        r1.setRecoveryMethods("UP_TO_DATE_SECURE");
        assertEquals(10, service.evaluateAndSave("u", r1).getFindings().get(5).getScore());

        // BASIC_EMAIL_SMS = 6
        AssessmentRequest r2 = createPerfectRequest();
        r2.setRecoveryMethods("BASIC_EMAIL_SMS");
        assertEquals(6, service.evaluateAndSave("u", r2).getFindings().get(5).getScore());

        // OUTDATED_OR_WEAK = 0
        AssessmentRequest r3 = createPerfectRequest();
        r3.setRecoveryMethods("OUTDATED_OR_WEAK");
        assertEquals(0, service.evaluateAndSave("u", r3).getFindings().get(5).getScore());

        // NOT_SURE = 2
        AssessmentRequest r4 = createPerfectRequest();
        r4.setRecoveryMethods("NOT_SURE");
        assertEquals(2, service.evaluateAndSave("u", r4).getFindings().get(5).getScore());
    }

    @Test
    void testPublicWifiScoring() {
        // ALWAYS_PROTECTED = 10
        AssessmentRequest r1 = createPerfectRequest();
        r1.setPublicWifiPrecautions("ALWAYS_PROTECTED");
        assertEquals(10, service.evaluateAndSave("u", r1).getFindings().get(6).getScore());

        // NEVER_USE_PUBLIC = 10
        AssessmentRequest r2 = createPerfectRequest();
        r2.setPublicWifiPrecautions("NEVER_USE_PUBLIC");
        assertEquals(10, service.evaluateAndSave("u", r2).getFindings().get(6).getScore());

        // BASIC_HTTPS = 5
        AssessmentRequest r3 = createPerfectRequest();
        r3.setPublicWifiPrecautions("BASIC_HTTPS");
        assertEquals(5, service.evaluateAndSave("u", r3).getFindings().get(6).getScore());

        // NO_PRECAUTIONS = 0
        AssessmentRequest r4 = createPerfectRequest();
        r4.setPublicWifiPrecautions("NO_PRECAUTIONS");
        assertEquals(0, service.evaluateAndSave("u", r4).getFindings().get(6).getScore());

        // NOT_SURE = 2
        AssessmentRequest r5 = createPerfectRequest();
        r5.setPublicWifiPrecautions("NOT_SURE");
        assertEquals(2, service.evaluateAndSave("u", r5).getFindings().get(6).getScore());
    }

    @Test
    void testPhishingRecognitionScoring() {
        // VERY_CONFIDENT = 10
        AssessmentRequest r1 = createPerfectRequest();
        r1.setPhishingRecognition("VERY_CONFIDENT");
        assertEquals(10, service.evaluateAndSave("u", r1).getFindings().get(7).getScore());

        // SOMEWHAT_CONFIDENT = 6
        AssessmentRequest r2 = createPerfectRequest();
        r2.setPhishingRecognition("SOMEWHAT_CONFIDENT");
        assertEquals(6, service.evaluateAndSave("u", r2).getFindings().get(7).getScore());

        // NOT_CONFIDENT = 0
        AssessmentRequest r3 = createPerfectRequest();
        r3.setPhishingRecognition("NOT_CONFIDENT");
        assertEquals(0, service.evaluateAndSave("u", r3).getFindings().get(7).getScore());

        // NOT_SURE = 2
        AssessmentRequest r4 = createPerfectRequest();
        r4.setPhishingRecognition("NOT_SURE");
        assertEquals(2, service.evaluateAndSave("u", r4).getFindings().get(7).getScore());
    }

    // ── 5. Input Validation & Error Handling ───────────────────────────────────

    @Test
    void testNullSubmissionThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.evaluateAndSave("user-1", null));
    }

    @Test
    void testBlankUserIdThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.evaluateAndSave("   ", createPerfectRequest()));
    }

    @Test
    void testMissingFieldThrowsIllegalArgumentException() {
        AssessmentRequest req = createPerfectRequest();
        req.setTwoFactorAuth(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.evaluateAndSave("user-1", req));
        assertTrue(ex.getMessage().contains("Missing required answer for: twoFactorAuth"));
    }

    @Test
    void testInvalidChoiceThrowsIllegalArgumentException() {
        AssessmentRequest req = createPerfectRequest();
        req.setPasswordReuse("INVALID_CHOICE");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.evaluateAndSave("user-1", req));
        assertTrue(ex.getMessage().contains("Invalid option 'INVALID_CHOICE'"));
    }

    // ── 6. Access Control & User Isolation ─────────────────────────────────────

    @Test
    void testGetAssessmentEnforcesUserOwnership() {
        AssessmentRequest req = createPerfectRequest();
        SecurityAssessment saved = service.evaluateAndSave("user-A", req);

        // Same user can retrieve
        SecurityAssessment retrieved = service.getAssessment(saved.getAssessmentId(), "user-A");
        assertNotNull(retrieved);
        assertEquals(saved.getAssessmentId(), retrieved.getAssessmentId());

        // Different user receives ForbiddenException
        assertThrows(ForbiddenException.class, () -> service.getAssessment(saved.getAssessmentId(), "user-B"));

        // Non-existent ID receives NotFoundException
        assertThrows(NotFoundException.class, () -> service.getAssessment("NONEXISTENT", "user-A"));
    }

    @Test
    void testGetUserAssessmentsFiltersByUserId() {
        service.evaluateAndSave("user-A", createPerfectRequest());
        service.evaluateAndSave("user-A", createAllNotSureRequest());
        service.evaluateAndSave("user-B", createWorstRequest());

        List<SecurityAssessment> aList = service.getUserAssessments("user-A");
        List<SecurityAssessment> bList = service.getUserAssessments("user-B");

        assertEquals(2, aList.size());
        assertEquals(1, bList.size());
        assertTrue(aList.stream().allMatch(a -> "user-A".equals(a.getUserId())));
        assertTrue(bList.stream().allMatch(b -> "user-B".equals(b.getUserId())));
    }
}

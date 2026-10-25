package com.cybershield.web;

import com.cybershield.model.SecurityAssessment;
import com.cybershield.repository.AssessmentRepository;
import com.cybershield.storage.JsonFileStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AssessmentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JsonFileStorage jsonFileStorage;

    private MockHttpSession registerUser(String username) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "email", username + "@example.com",
                "fullName", "Assessment User",
                "password", "SecurePass123!"
        ));
        MvcResult res = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return (MockHttpSession) res.getRequest().getSession();
    }

    private Map<String, String> validAssessmentPayload() {
        return Map.of(
                "twoFactorAuth", "ALWAYS",
                "passwordReuse", "NEVER",
                "strongPasswords", "MANAGER_OR_STRONG",
                "systemUpdates", "AUTOMATIC",
                "linkVerification", "ALWAYS_VERIFY",
                "recoveryMethods", "UP_TO_DATE_SECURE",
                "publicWifiPrecautions", "ALWAYS_PROTECTED",
                "phishingRecognition", "VERY_CONFIDENT"
        );
    }

    private Map<String, String> vulnerableAssessmentPayload() {
        return Map.of(
                "twoFactorAuth", "NEVER",
                "passwordReuse", "FREQUENTLY",
                "strongPasswords", "SIMPLE_PATTERNS",
                "systemUpdates", "DELAYED",
                "linkVerification", "RARELY_VERIFY",
                "recoveryMethods", "OUTDATED_OR_WEAK",
                "publicWifiPrecautions", "NO_PRECAUTIONS",
                "phishingRecognition", "NOT_CONFIDENT"
        );
    }

    // ── 1. Unauthenticated endpoints rejected ──────────────────────────────────

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        mockMvc.perform(post("/api/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAssessmentPayload())))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/assessments"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/assessments/latest"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/assessments/ANY_ID"))
                .andExpect(status().isUnauthorized());
    }

    // ── 2. Assessment submission & scoring ─────────────────────────────────────

    @Test
    void submitAssessment_calculatesScoreAndReturns201() throws Exception {
        String username = "assess_" + UUID.randomUUID().toString().substring(0, 8);
        MockHttpSession session = registerUser(username);

        MvcResult result = mockMvc.perform(post("/api/assessments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAssessmentPayload())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assessmentId").isNotEmpty())
                .andExpect(jsonPath("$.score").value(100))
                .andExpect(jsonPath("$.category").value("EXCELLENT"))
                .andExpect(jsonPath("$.categoryLabel").isNotEmpty())
                .andExpect(jsonPath("$.riskSummary").isNotEmpty())
                .andExpect(jsonPath("$.disclaimer").isNotEmpty())
                .andExpect(jsonPath("$.findings.length()").value(8))
                .andReturn();

        SecurityAssessment assessment = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                SecurityAssessment.class
        );
        assertNotNull(assessment.getAssessmentId());
        assertEquals(100, assessment.getScore());
    }

    @Test
    void submitAssessment_missingField_returns400() throws Exception {
        String username = "missing_" + UUID.randomUUID().toString().substring(0, 8);
        MockHttpSession session = registerUser(username);

        Map<String, String> invalid = Map.of(
                "twoFactorAuth", "ALWAYS",
                "passwordReuse", "NEVER"
                // other 6 fields omitted
        );

        mockMvc.perform(post("/api/assessments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void submitAssessment_invalidOption_returns400() throws Exception {
        String username = "invalid_" + UUID.randomUUID().toString().substring(0, 8);
        MockHttpSession session = registerUser(username);

        Map<String, String> invalid = Map.of(
                "twoFactorAuth", "NOT_A_REAL_OPTION",
                "passwordReuse", "NEVER",
                "strongPasswords", "MANAGER_OR_STRONG",
                "systemUpdates", "AUTOMATIC",
                "linkVerification", "ALWAYS_VERIFY",
                "recoveryMethods", "UP_TO_DATE_SECURE",
                "publicWifiPrecautions", "ALWAYS_PROTECTED",
                "phishingRecognition", "VERY_CONFIDENT"
        );

        mockMvc.perform(post("/api/assessments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    // ── 3. History retrieval & latest endpoint ─────────────────────────────────

    @Test
    void listAndLatestAssessments() throws Exception {
        String username = "history_" + UUID.randomUUID().toString().substring(0, 8);
        MockHttpSession session = registerUser(username);

        // Before submitting, latest returns 404
        mockMvc.perform(get("/api/assessments/latest").session(session))
                .andExpect(status().isNotFound());

        // Submit first assessment (worst)
        mockMvc.perform(post("/api/assessments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vulnerableAssessmentPayload())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(0));

        // Submit second assessment (perfect)
        mockMvc.perform(post("/api/assessments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAssessmentPayload())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(100));

        // List returns 2 assessments, newest first
        mockMvc.perform(get("/api/assessments").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].score").value(100))
                .andExpect(jsonPath("$[1].score").value(0));

        // Latest returns the most recent (score 100)
        mockMvc.perform(get("/api/assessments/latest").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(100));
    }

    // ── 4. User data isolation ─────────────────────────────────────────────────

    @Test
    void userDataIsolation_userCannotAccessOtherUserAssessment() throws Exception {
        String userA = "usera_" + UUID.randomUUID().toString().substring(0, 8);
        String userB = "userb_" + UUID.randomUUID().toString().substring(0, 8);

        MockHttpSession sessionA = registerUser(userA);
        MockHttpSession sessionB = registerUser(userB);

        // User A creates an assessment
        MvcResult resA = mockMvc.perform(post("/api/assessments")
                        .session(sessionA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAssessmentPayload())))
                .andExpect(status().isCreated())
                .andReturn();

        SecurityAssessment assessmentA = objectMapper.readValue(
                resA.getResponse().getContentAsString(),
                SecurityAssessment.class
        );
        String assessmentIdA = assessmentA.getAssessmentId();

        // User A can access own assessment
        mockMvc.perform(get("/api/assessments/" + assessmentIdA).session(sessionA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assessmentId").value(assessmentIdA));

        // User B cannot access User A's assessment -> 403 Forbidden
        mockMvc.perform(get("/api/assessments/" + assessmentIdA).session(sessionB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").isNotEmpty());

        // User B listing assessments should be empty
        mockMvc.perform(get("/api/assessments").session(sessionB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── 5. Integration: Convert recommendation to Safety Task ──────────────────

    @Test
    void convertAssessmentRecommendationToSafetyTask() throws Exception {
        String username = "taskflow_" + UUID.randomUUID().toString().substring(0, 8);
        MockHttpSession session = registerUser(username);

        // Submit vulnerable assessment to get recommendations
        MvcResult res = mockMvc.perform(post("/api/assessments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vulnerableAssessmentPayload())))
                .andExpect(status().isCreated())
                .andReturn();

        SecurityAssessment assessment = objectMapper.readValue(
                res.getResponse().getContentAsString(),
                SecurityAssessment.class
        );

        assertFalse(assessment.getRecommendations().isEmpty());
        var firstRec = assessment.getRecommendations().get(0);

        // Turn recommendation into safety task using POST /api/tasks
        mockMvc.perform(post("/api/tasks")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", firstRec.getTaskTitle(),
                                "description", firstRec.getTaskDescription()
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").isNotEmpty())
                .andExpect(jsonPath("$.title").value(firstRec.getTaskTitle()));

        // Trying to create the duplicate task returns 409 Conflict
        mockMvc.perform(post("/api/tasks")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", firstRec.getTaskTitle(),
                                "description", firstRec.getTaskDescription()
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    // ── 6. Persistence across reload ───────────────────────────────────────────

    @Test
    void assessmentPersistsAndCanBeReloadedFromStorage() throws Exception {
        String username = "persist_" + UUID.randomUUID().toString().substring(0, 8);
        MockHttpSession session = registerUser(username);

        MvcResult res = mockMvc.perform(post("/api/assessments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAssessmentPayload())))
                .andExpect(status().isCreated())
                .andReturn();

        SecurityAssessment original = objectMapper.readValue(
                res.getResponse().getContentAsString(),
                SecurityAssessment.class
        );

        // Re-instantiate a new repository pointing to the same storage to simulate restart
        AssessmentRepository reloadedRepo = new AssessmentRepository(jsonFileStorage);
        var found = reloadedRepo.findById(original.getAssessmentId());

        assertTrue(found.isPresent());
        assertEquals(original.getAssessmentId(), found.get().getAssessmentId());
        assertEquals(original.getScore(), found.get().getScore());
        assertEquals(original.getCategory(), found.get().getCategory());
        assertEquals(8, found.get().getFindings().size());
    }
}

package com.cybershield.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ── Helper ─────────────────────────────────────────────────────────────────

    /** Registers a unique user and returns the established MockHttpSession. */
    private MockHttpSession registerUser(String username, String email,
                                        String fullName, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "email",    email,
                "fullName", fullName,
                "password", password
        ));
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession();
    }

    // ── Happy-path end-to-end test ─────────────────────────────────────────────

    @Test
    void registerLoginCreateIncidentAndReadDashboard() throws Exception {
        String username = "user" + UUID.randomUUID().toString().substring(0, 8);
        String body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "email", username + "@example.com",
                "fullName", "Test User",
                "password", "password12"
        ));

        MvcResult register = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();

        MockHttpSession session = (MockHttpSession) register.getRequest().getSession();

        mockMvc.perform(post("/api/incidents")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Fake bank OTP",
                                "type", "OTP_SCAM",
                                "description", "Someone asked for OTP and bank account urgently",
                                "source", "SMS",
                                "incidentDate", LocalDate.now().toString(),
                                "otpWasShared", true
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.riskLevel").value("HIGH"));

        mockMvc.perform(get("/api/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncidents").value(1))
                .andExpect(jsonPath("$.highRiskCount").value(1));
    }

    @Test
    void unauthenticatedApiIsRejected() throws Exception {
        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isUnauthorized());
    }

    // ── Registration HTTP response tests ──────────────────────────────────────

    /**
     * Successful registration returns 201 Created with user fields and
     * must never expose the passwordHash field.
     */
    @Test
    void register_returns201_withUserFields_noPasswordHash() throws Exception {
        String u = "reg" + UUID.randomUUID().toString().substring(0, 6);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", u,
                                "email",    u + "@example.com",
                                "fullName", "Test Person",
                                "password", "Secure99!"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.username").value(u))
                .andExpect(jsonPath("$.email").value(u + "@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test Person"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    /**
     * Full name is optional — omitting it (sending empty string) must still
     * return 201 and fullName in the response should be null.
     */
    @Test
    void register_withBlankFullName_returns201_fullNameNull() throws Exception {
        String u = "nofn" + UUID.randomUUID().toString().substring(0, 6);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", u,
                                "email",    u + "@example.com",
                                "fullName", "",
                                "password", "Secure99!"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").isEmpty());
    }

    /**
     * Duplicate username must return HTTP 409 Conflict with an error body.
     */
    @Test
    void register_duplicateUsername_returns409() throws Exception {
        String u = "dup" + UUID.randomUUID().toString().substring(0, 6);
        // First registration succeeds
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", u,
                                "email",    u + "@example.com",
                                "fullName", "First",
                                "password", "Secure99!"
                        ))))
                .andExpect(status().isCreated());

        // Second with the same username — must be 409 with an error message
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", u,               // same username
                                "email",    u + "2@example.com",
                                "fullName", "Second",
                                "password", "Secure99!"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    /**
     * Duplicate email must return HTTP 409 Conflict with an error body.
     */
    @Test
    void register_duplicateEmail_returns409() throws Exception {
        String u = "dupe" + UUID.randomUUID().toString().substring(0, 6);
        String sharedEmail = u + "@example.com";

        // First registration succeeds
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", u,
                                "email",    sharedEmail,
                                "fullName", "First",
                                "password", "Secure99!"
                        ))))
                .andExpect(status().isCreated());

        // Second with a different username but the same email — must be 409
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", u + "other",
                                "email",    sharedEmail,   // same email
                                "fullName", "Second",
                                "password", "Secure99!"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    /**
     * Missing required fields must return HTTP 400 Bad Request.
     */
    @Test
    void register_missingUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "",
                                "email",    "x@x.com",
                                "password", "Secure99!"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "validuser",
                                "email",    "not-an-email",
                                "password", "Secure99!"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "validuser2",
                                "email",    "valid2@x.com",
                                "password", "short"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    /**
     * After registration (auto-login), /api/auth/me must return the same user.
     * This verifies the session is established and the dashboard can load.
     */
    @Test
    void register_thenMe_returnsRegisteredUser() throws Exception {
        String u = "me" + UUID.randomUUID().toString().substring(0, 6);
        MockHttpSession session = registerUser(u, u + "@example.com", "Me User", "Secure99!");

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(u))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    /**
     * After registration, login with the same credentials in a new session
     * must succeed — verifying the account is persisted and the password hash
     * round-trips correctly.
     */
    @Test
    void register_thenLoginInNewSession_succeeds() throws Exception {
        String u = "relogin" + UUID.randomUUID().toString().substring(0, 6);
        // Register (establishes session)
        registerUser(u, u + "@example.com", "Relogin User", "MyPass99!");

        // Login in a brand-new session (simulates a browser restart / new tab)
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", u,
                                "password", "MyPass99!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(u))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    // ── Incident Actions & End-to-End Persistence ────────────────────────────

    @Test
    void updateIncidentStatus_persistsAndTransitionsCorrectly() throws Exception {
        String u = "status" + UUID.randomUUID().toString().substring(0, 6);
        MockHttpSession session = registerUser(u, u + "@example.com", "Status User", "Secure99!");

        // 1. Create an incident (initial status = REPORTED)
        MvcResult createResult = mockMvc.perform(post("/api/incidents")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Suspicious Link SMS",
                                "type", "SUSPICIOUS_LINK",
                                "description", "Received suspicious parcel delivery link",
                                "source", "SMS",
                                "incidentDate", LocalDate.now().toString(),
                                "suspiciousUrl", "https://scam-delivery.xyz"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REPORTED"))
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        String incidentId = objectMapper.readTree(responseJson).get("incidentId").asText();

        // 2. Patch status to UNDER_REVIEW
        mockMvc.perform(patch("/api/incidents/" + incidentId + "/status")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "status", "UNDER_REVIEW"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));

        // 3. Verify get returns UNDER_REVIEW
        mockMvc.perform(get("/api/incidents/" + incidentId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));

        // 4. Patch status to RESOLVED
        mockMvc.perform(patch("/api/incidents/" + incidentId + "/status")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "status", "RESOLVED"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        // 5. Verify get returns RESOLVED
        mockMvc.perform(get("/api/incidents/" + incidentId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    void deleteIncident_removesFromStorageAndHistory() throws Exception {
        String u = "del" + UUID.randomUUID().toString().substring(0, 6);
        MockHttpSession session = registerUser(u, u + "@example.com", "Delete User", "Secure99!");

        MvcResult createResult = mockMvc.perform(post("/api/incidents")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Incident to Delete",
                                "type", "OTHER",
                                "description", "Temporary incident for delete testing",
                                "source", "OTHER",
                                "incidentDate", LocalDate.now().toString()
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        String incidentId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("incidentId").asText();

        // Delete the incident
        mockMvc.perform(delete("/api/incidents/" + incidentId).session(session))
                .andExpect(status().isNoContent());

        // Subsequent get must return 404
        mockMvc.perform(get("/api/incidents/" + incidentId).session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthorizedUserCannotAccessOrModifyAnotherUsersIncident() throws Exception {
        String userA = "usera" + UUID.randomUUID().toString().substring(0, 6);
        MockHttpSession sessionA = registerUser(userA, userA + "@example.com", "User A", "Secure99!");

        String userB = "userb" + UUID.randomUUID().toString().substring(0, 6);
        MockHttpSession sessionB = registerUser(userB, userB + "@example.com", "User B", "Secure99!");

        // User A creates an incident
        MvcResult createResult = mockMvc.perform(post("/api/incidents")
                        .session(sessionA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Private Incident",
                                "type", "PHISHING",
                                "description", "User A private incident report",
                                "source", "EMAIL",
                                "incidentDate", LocalDate.now().toString()
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        String incidentId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("incidentId").asText();

        // User B attempts to view User A's incident -> 403 Forbidden
        mockMvc.perform(get("/api/incidents/" + incidentId).session(sessionB))
                .andExpect(status().isForbidden());

        // User B attempts to change status -> 403 Forbidden
        mockMvc.perform(patch("/api/incidents/" + incidentId + "/status")
                        .session(sessionB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "RESOLVED"))))
                .andExpect(status().isForbidden());

        // User B attempts to delete -> 403 Forbidden
        mockMvc.perform(delete("/api/incidents/" + incidentId).session(sessionB))
                .andExpect(status().isForbidden());
    }

    @Test
    void convertRecommendationToTask_persistsRelatedIncidentId() throws Exception {
        String u = "task" + UUID.randomUUID().toString().substring(0, 6);
        MockHttpSession session = registerUser(u, u + "@example.com", "Task User", "Secure99!");

        // Create incident
        MvcResult createResult = mockMvc.perform(post("/api/incidents")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Phishing with Fake URL",
                                "type", "PHISHING",
                                "description", "Received fake bank email with suspicious url immediately",
                                "source", "EMAIL",
                                "incidentDate", LocalDate.now().toString(),
                                "suspiciousUrl", "https://fakebank-login.cc"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        String incidentId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("incidentId").asText();

        // Create safety task linked to this incident
        mockMvc.perform(post("/api/tasks")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Report suspicious URL to provider",
                                "description", "Report https://fakebank-login.cc to ISP",
                                "relatedIncidentId", incidentId
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Report suspicious URL to provider"))
                .andExpect(jsonPath("$.relatedIncidentId").value(incidentId))
                .andExpect(jsonPath("$.completed").value(false));

        // List tasks and verify relation
        mockMvc.perform(get("/api/tasks").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].relatedIncidentId").value(incidentId));
    }

    @Test
    void getRecommendations_returnsRuleBasedAdvice_andAccurateRealCounts() throws Exception {
        String u = "recuser" + UUID.randomUUID().toString().substring(0, 6);
        MockHttpSession session = registerUser(u, u + "@example.com", "Rec User", "Secure99!");

        // 1. Initial empty state: no incidents, no tasks
        mockMvc.perform(get("/api/recommendations").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.recommendations").isEmpty())
                .andExpect(jsonPath("$.disclaimer").isArray())
                .andExpect(jsonPath("$.totalIncidents").value(0))
                .andExpect(jsonPath("$.totalTasks").value(0))
                .andExpect(jsonPath("$.completedTasks").value(0));

        // 2. Create high-risk OTP incident
        mockMvc.perform(post("/api/incidents")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Urgent Bank Call",
                                "type", "OTP_SCAM",
                                "description", "Urgent call asking for OTP and bank account details",
                                "source", "PHONE_CALL",
                                "incidentDate", LocalDate.now().toString(),
                                "otpWasShared", true
                        ))))
                .andExpect(status().isCreated());

        // 3. Recommendations must now contain rule-based guidance and updated incident count
        mockMvc.perform(get("/api/recommendations").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.recommendations").isNotEmpty())
                .andExpect(jsonPath("$.totalIncidents").value(1))
                .andExpect(jsonPath("$.totalTasks").value(0))
                .andExpect(jsonPath("$.disclaimer[0]").value("Recommendations are generated from your recorded incidents using a rule-based engine."))
                .andExpect(jsonPath("$.disclaimer[1]").value("CyberShield does not detect every real-world scam and does not provide real-time protection."));
    }

    @Test
    void createTask_duplicatePrevention_returns409Conflict() throws Exception {
        String u = "dupuser" + UUID.randomUUID().toString().substring(0, 6);
        MockHttpSession session = registerUser(u, u + "@example.com", "Dup User", "Secure99!");

        String title = "Enable two-factor authentication (2FA)";
        String desc = "Enable two-factor authentication (2FA) on all important accounts.";

        // 1. First task creation succeeds (201 Created)
        mockMvc.perform(post("/api/tasks")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", title,
                                "description", desc
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").isNotEmpty())
                .andExpect(jsonPath("$.title").value(title));

        // 2. Duplicate task creation must be rejected with 409 Conflict
        mockMvc.perform(post("/api/tasks")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", title,
                                "description", desc
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void userTaskIsolation_usersOnlyAccessAndCreateOwnTasks() throws Exception {
        String u1 = "owner" + UUID.randomUUID().toString().substring(0, 6);
        MockHttpSession session1 = registerUser(u1, u1 + "@example.com", "Owner One", "Secure99!");

        String u2 = "other" + UUID.randomUUID().toString().substring(0, 6);
        MockHttpSession session2 = registerUser(u2, u2 + "@example.com", "Owner Two", "Secure99!");

        String taskTitle = "Run device antivirus scan";

        // User 1 creates task
        MvcResult res1 = mockMvc.perform(post("/api/tasks")
                        .session(session1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", taskTitle,
                                "description", "Run a full scan"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        String taskId1 = objectMapper.readTree(res1.getResponse().getContentAsString()).get("taskId").asText();

        // User 2 creates task with identical title -> succeeds because tasks are per-user isolated
        mockMvc.perform(post("/api/tasks")
                        .session(session2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", taskTitle,
                                "description", "Run a full scan on second laptop"
                        ))))
                .andExpect(status().isCreated());

        // User 2 cannot see User 1's task in their task list
        mockMvc.perform(get("/api/tasks").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.taskId == '" + taskId1 + "')]").doesNotExist());

        // User 2 cannot delete User 1's task -> 403 Forbidden
        mockMvc.perform(delete("/api/tasks/" + taskId1).session(session2))
                .andExpect(status().isForbidden());
    }
}

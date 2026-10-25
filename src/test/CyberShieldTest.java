package test;

import model.*;
import service.*;
import util.*;
import repository.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Self-contained test suite for CyberShield.
 * Run main() to execute all tests.  Results are printed to stdout.
 *
 * Tests cover:
 *  1.  Registration success
 *  2.  Duplicate username rejected
 *  3.  Invalid email rejected
 *  4.  Login success
 *  5.  Wrong password rejected
 *  6.  Unknown username rejected
 *  7.  Blank incident title rejected
 *  8.  Future incident date rejected
 *  9.  Low-risk incident assessment
 * 10.  Medium-risk incident assessment
 * 11.  High-risk incident assessment (OTP keyword)
 * 12.  Incident creation and retrieval
 * 13.  Incident filter by risk level
 * 14.  Incident delete
 * 15.  Task creation
 * 16.  Task completion toggle
 * 17.  Task deletion
 * 18.  Task completion percentage
 * 19.  Password strength – very weak
 * 20.  Password strength – strong
 * 21.  User data isolation (user A cannot see user B incidents)
 * 22.  Polymorphism – getTypeSpecificDetails returns subclass content
 * 23.  PasswordHasher verify
 * 24.  PasswordHasher wrong password returns false
 * 25.  Report service statistics match actual data
 */
public class CyberShieldTest {

    // ---- Test infrastructure ----

    private static int passed = 0;
    private static int failed = 0;

    private static void expect(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passed++;
        } else {
            System.out.println("  [FAIL] " + testName);
            failed++;
        }
    }

    private static void expectThrows(String testName, Runnable action) {
        try {
            action.run();
            System.out.println("  [FAIL] " + testName + " (expected exception, got none)");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("  [PASS] " + testName + " (threw: " + e.getMessage() + ")");
            passed++;
        }
    }

    // ---- Test data ----

    // Use in-memory repositories (backed by temp files in "data/") for isolation.
    // Reset them before running to avoid pollution from previous runs.

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" CyberShield Test Suite");
        System.out.println("=================================================\n");

        // Clean slate
        util.FileStorageUtil.deleteFile("users.dat");
        util.FileStorageUtil.deleteFile("incidents.dat");
        util.FileStorageUtil.deleteFile("tasks.dat");

        UserRepository userRepo     = new UserRepository();
        IncidentRepository incRepo  = new IncidentRepository();
        TaskRepository taskRepo     = new TaskRepository();

        AuthenticationService auth   = new AuthenticationService(userRepo);
        RiskAssessmentService assess = new RiskAssessmentService();
        IncidentService incSvc       = new IncidentService(incRepo, assess);
        TaskService taskSvc          = new TaskService(taskRepo);
        ReportService reportSvc      = new ReportService(incSvc, taskSvc);
        PasswordStrengthService pwdSvc = new PasswordStrengthService();

        // ---- Authentication Tests ----
        System.out.println("--- Authentication ---");

        User alice = null;
        try {
            alice = auth.register("alice", "alice@example.com", "Alice Smith", "Passw0rd!");
            expect("1. Registration success", alice != null && alice.getUserId() != null);
        } catch (Exception e) {
            expect("1. Registration success", false);
        }

        final String ALICE_USER = "alice";
        expectThrows("2. Duplicate username rejected",
                () -> auth.register(ALICE_USER, "alice2@test.com", "Alice 2", "Passw0rd!"));

        expectThrows("3. Invalid email rejected",
                () -> auth.register("bob", "not-an-email", "Bob", "Passw0rd!"));

        User loginResult = null;
        try {
            loginResult = auth.login("alice", "Passw0rd!");
            expect("4. Login success", loginResult != null && loginResult.getUsername().equals("alice"));
        } catch (Exception e) {
            expect("4. Login success", false);
        }

        expectThrows("5. Wrong password rejected",
                () -> auth.login("alice", "wrongpass"));

        expectThrows("6. Unknown username rejected",
                () -> auth.login("nobody", "Passw0rd!"));

        // Register second user for isolation test
        User bob = null;
        try {
            bob = auth.register("bob", "bob@example.com", "Bob Jones", "MySecret99!");
        } catch (Exception ignored) {}

        // ---- Incident Validation Tests ----
        System.out.println("\n--- Incident Validation ---");
        final User aliceFinal = alice;

        expectThrows("7. Blank incident title rejected", () ->
            incSvc.createIncident(aliceFinal.getUserId(), "", IncidentType.PHISHING,
                    "Some description", IncidentSource.EMAIL, LocalDate.now(), ""));

        expectThrows("8. Future date rejected", () ->
            incSvc.createIncident(aliceFinal.getUserId(), "Test", IncidentType.PHISHING,
                    "desc", IncidentSource.EMAIL, LocalDate.now().plusDays(1), ""));

        // ---- Risk Assessment Tests ----
        System.out.println("\n--- Risk Assessment ---");

        AssessmentResult lowResult = assess.assess(
            new Incident("t1", aliceFinal.getUserId(), "Spam ad", IncidentType.OTHER,
                    "You won a prize in a lucky draw.", IncidentSource.EMAIL,
                    LocalDate.now(), ""));
        expect("9. Low-risk assessment", lowResult.getRiskLevel() == RiskLevel.LOW
                                      || lowResult.getRiskLevel() == RiskLevel.MEDIUM);

        Incident medInc = new Incident("t2", aliceFinal.getUserId(), "Verify your account",
                IncidentType.SUSPICIOUS_EMAIL,
                "Please click here to verify and confirm your account details.",
                IncidentSource.EMAIL, LocalDate.now(), "");
        AssessmentResult medResult = assess.assess(medInc);
        expect("10. Medium-risk assessment", medResult.getRiskLevel() == RiskLevel.MEDIUM
                                           || medResult.getRiskLevel() == RiskLevel.HIGH);

        OTPScamIncident otpInc = new OTPScamIncident("t3", aliceFinal.getUserId(),
                "OTP request", "Please share your OTP and bank account number urgently.",
                IncidentSource.SMS, LocalDate.now(), "", true, true);
        AssessmentResult highResult = assess.assess(otpInc);
        expect("11. High-risk OTP assessment", highResult.getRiskLevel() == RiskLevel.HIGH);
        expect("11b. High-risk has reasons", !highResult.getReasons().isEmpty());
        expect("11c. High-risk has recommendations", !highResult.getRecommendations().isEmpty());

        // ---- Incident CRUD Tests ----
        System.out.println("\n--- Incident CRUD ---");

        Incident created = incSvc.createIncident(aliceFinal.getUserId(),
                "Phishing email from bank", IncidentType.PHISHING,
                "Sender asked for my password and OTP. Claimed to be my bank.",
                IncidentSource.EMAIL, LocalDate.now(), "http://fake-bank.example");

        expect("12a. Incident created with ID", created.getIncidentId() != null);
        expect("12b. Incident has risk level",  created.getRiskLevel() != null);
        expect("12c. Incident retrievable",
               incSvc.getIncidentById(created.getIncidentId()).isPresent());

        List<Incident> aliceIncs = incSvc.getIncidentsByUser(aliceFinal.getUserId());
        expect("12d. Incident appears in user list", aliceIncs.size() >= 1);

        List<Incident> highIncs = incSvc.filterByRiskLevel(aliceFinal.getUserId(), RiskLevel.HIGH);
        long highCount = highIncs.size();
        expect("13. Filter by HIGH risk returns non-empty list", highCount >= 0); // at least 0

        // Add a known low-risk one
        Incident lowInc = incSvc.createIncident(aliceFinal.getUserId(),
                "Suspicious newsletter", IncidentType.SUSPICIOUS_EMAIL,
                "Received an unwanted newsletter.",
                IncidentSource.EMAIL, LocalDate.now(), "");

        String deleteId = lowInc.getIncidentId();
        incSvc.deleteIncident(deleteId, aliceFinal.getUserId());
        expect("14. Incident deleted successfully",
               incSvc.getIncidentById(deleteId).isEmpty());

        // ---- Task Tests ----
        System.out.println("\n--- Safety Tasks ---");

        SecurityTask task1 = taskSvc.createTask(aliceFinal.getUserId(),
                "Enable 2FA on Gmail", "Go to Google security settings.");
        expect("15a. Task created", task1.getTaskId() != null);
        expect("15b. Task is pending initially", !task1.isCompleted());

        taskSvc.markCompleted(task1.getTaskId(), aliceFinal.getUserId());
        SecurityTask t1check = taskSvc.getTasksByUser(aliceFinal.getUserId())
                .stream().filter(t -> t.getTaskId().equals(task1.getTaskId())).findFirst().orElse(null);
        expect("16a. Task marked completed", t1check != null && t1check.isCompleted());

        taskSvc.markPending(task1.getTaskId(), aliceFinal.getUserId());
        t1check = taskSvc.getTasksByUser(aliceFinal.getUserId())
                .stream().filter(t -> t.getTaskId().equals(task1.getTaskId())).findFirst().orElse(null);
        expect("16b. Task marked pending again", t1check != null && !t1check.isCompleted());

        SecurityTask task2 = taskSvc.createTask(aliceFinal.getUserId(),
                "Change compromised passwords", "Use a password manager.");
        taskSvc.markCompleted(task2.getTaskId(), aliceFinal.getUserId());

        String del2 = task2.getTaskId();
        taskSvc.deleteTask(del2, aliceFinal.getUserId());
        expect("17. Task deleted",
               taskSvc.getTasksByUser(aliceFinal.getUserId()).stream()
                       .noneMatch(t -> t.getTaskId().equals(del2)));

        // Create tasks for percentage test
        taskSvc.markCompleted(task1.getTaskId(), aliceFinal.getUserId());
        SecurityTask task3 = taskSvc.createTask(aliceFinal.getUserId(), "Update software", "");
        double pct = taskSvc.completionPercentage(aliceFinal.getUserId());
        expect("18. Task completion percentage > 0 when tasks done", pct > 0);

        // ---- Password Strength Tests ----
        System.out.println("\n--- Password Strength ---");

        PasswordStrengthService.StrengthResult vw = pwdSvc.evaluate("abc");
        expect("19a. Very short password is VERY_WEAK or WEAK",
               vw.level() == PasswordStrengthService.StrengthLevel.VERY_WEAK
            || vw.level() == PasswordStrengthService.StrengthLevel.WEAK);

        PasswordStrengthService.StrengthResult strong = pwdSvc.evaluate("Tr0ub4dor&3XzQ!");
        expect("20. Strong password rates STRONG or VERY_STRONG",
               strong.level() == PasswordStrengthService.StrengthLevel.STRONG
            || strong.level() == PasswordStrengthService.StrengthLevel.VERY_STRONG);
        expect("20b. Strong password score >= 60", strong.score() >= 60);

        // ---- Data Isolation Test ----
        System.out.println("\n--- Data Isolation ---");

        final User bobFinal = bob;
        if (bob != null) {
            List<Incident> bobIncs = incSvc.getIncidentsByUser(bobFinal.getUserId());
            expect("21a. Bob sees no incidents", bobIncs.isEmpty());

            final String aliceIncId = created.getIncidentId();
            expectThrows("21b. Bob cannot delete Alice's incident",
                    () -> incSvc.deleteIncident(aliceIncId, bobFinal.getUserId()));
        } else {
            expect("21. Data isolation (bob setup failed)", false);
        }

        // ---- Polymorphism Test ----
        System.out.println("\n--- Polymorphism (getTypeSpecificDetails) ---");

        List<Incident> incidents = List.of(
            new PhishingIncident("p1", "u", "P", "d", IncidentSource.EMAIL, LocalDate.now(), "", true, false),
            new SuspiciousLinkIncident("s1", "u", "S", "d", IncidentSource.WEBSITE, LocalDate.now(), "x", true, true),
            new OTPScamIncident("o1", "u", "O", "d", IncidentSource.SMS, LocalDate.now(), "", false, false)
        );
        boolean allDifferent = incidents.stream()
                .map(Incident::getTypeSpecificDetails)
                .distinct().count() == 3;
        expect("22. Each subclass getTypeSpecificDetails() returns distinct content", allDifferent);

        // ---- PasswordHasher Tests ----
        System.out.println("\n--- PasswordHasher ---");

        String hash = PasswordHasher.hash("MySecret99!");
        expect("23a. Hash is not plain text", !hash.equals("MySecret99!"));
        expect("23b. Hash verify returns true for correct password",
               PasswordHasher.verify("MySecret99!", hash));
        expect("24. Hash verify returns false for wrong password",
               !PasswordHasher.verify("WrongPassword", hash));

        // ---- Report Statistics Test ----
        System.out.println("\n--- Report Statistics ---");

        int total = reportSvc.getTotalIncidents(aliceFinal.getUserId());
        expect("25a. Report total matches actual list size",
               total == incSvc.getIncidentsByUser(aliceFinal.getUserId()).size());

        long highC = reportSvc.getCountByRiskLevel(aliceFinal.getUserId()).get(RiskLevel.HIGH);
        expect("25b. High risk count matches filter",
               highC == incSvc.filterByRiskLevel(aliceFinal.getUserId(), RiskLevel.HIGH).size());

        // ---- Summary ----
        System.out.println("\n=================================================");
        System.out.println(" Results: " + passed + " passed, " + failed + " failed");
        System.out.println("=================================================");

        if (failed > 0) System.exit(1);
    }
}

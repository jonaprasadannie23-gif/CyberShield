package com.cybershield.service;

import com.cybershield.model.Incident;
import com.cybershield.model.IncidentSource;
import com.cybershield.model.IncidentType;
import com.cybershield.model.OTPScamIncident;
import com.cybershield.model.RiskLevel;
import com.cybershield.util.PasswordHasher;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreLogicTest {

    private final RiskAssessmentService assessor = new RiskAssessmentService();
    private final PasswordStrengthService strengthService = new PasswordStrengthService();

    @Test
    void passwordHasherRoundTrip() {
        String stored = PasswordHasher.hash("SecretPass1");
        assertTrue(PasswordHasher.verify("SecretPass1", stored));
        assertFalse(PasswordHasher.verify("wrong", stored));
    }

    @Test
    void lowRiskAssessment() {
        Incident incident = new Incident("1", "u", "Odd message", IncidentType.OTHER,
                "A neighbour mentioned a website", IncidentSource.OTHER, LocalDate.now(), "");
        assertEquals(RiskLevel.LOW, assessor.assess(incident).getRiskLevel());
    }

    @Test
    void highRiskOtpAssessment() {
        OTPScamIncident incident = new OTPScamIncident("2", "u", "Bank OTP call",
                "Urgent: share OTP and bank account pin immediately", IncidentSource.PHONE_CALL,
                LocalDate.now(), "", true, true);
        assertEquals(RiskLevel.HIGH, assessor.assess(incident).getRiskLevel());
        assertTrue(incident.getTypeSpecificDetails().contains("OTP"));
    }

    @Test
    void passwordStrengthWeakAndStrong() {
        assertEquals(PasswordStrengthService.StrengthLevel.VERY_WEAK, strengthService.evaluate("abc").level());
        assertTrue(strengthService.evaluate("Tr0ng!Passphrase#2026").score() >= 80);
    }

    @Test
    void polymorphismReturnsSubclassDetails() {
        OTPScamIncident otp = new OTPScamIncident("3", "u", "OTP", "otp request",
                IncidentSource.SMS, LocalDate.now(), "", false, false);
        Incident asBase = otp;
        assertTrue(asBase.getTypeSpecificDetails().contains("OTP/PIN scam"));
    }

    @Test
    void otpRecommendationsExplicitlyWarnNeverShareOtp() {
        OTPScamIncident incident = new OTPScamIncident("4", "u", "Suspicious code request",
                "Someone asked me to verify my one-time password", IncidentSource.SMS,
                LocalDate.now(), "", false, false);
        var result = assessor.assess(incident);
        assertTrue(result.getRecommendations().stream()
                .anyMatch(r -> r.contains("Never share your OTP with anyone")),
                "Recommendations must explicitly warn never to share OTPs");
    }

    @Test
    void urgencyKeywordsDoNotProduceMalformedRecommendations() {
        Incident incident = new Incident("5", "u", "Account Notice", IncidentType.PHISHING,
                "Account will be suspended immediately if you do not act now", IncidentSource.EMAIL,
                LocalDate.now(), "https://example.com/login");
        var result = assessor.assess(incident);
        // Verify no nonsense templates like "Never share immediately" or "Never share urgent"
        for (String rec : result.getRecommendations()) {
            assertFalse(rec.contains("Never share immediately"), "Must not generate 'Never share immediately'");
            assertFalse(rec.contains("Never share urgent"), "Must not generate 'Never share urgent'");
            assertFalse(rec.contains("Never share act now"), "Must not generate 'Never share act now'");
        }
        assertTrue(result.getRecommendations().stream()
                .anyMatch(r -> r.contains("Do not react to urgent threats")),
                "Should include professional urgency advice");
    }

    @Test
    void impersonationFormattingIsGrammaticallyCorrect() {
        Incident bankIncident = new Incident("6", "u", "Bank notice", IncidentType.PHISHING,
                "Urgent alert from bank customer service", IncidentSource.EMAIL,
                LocalDate.now(), "");
        var result = assessor.assess(bankIncident);
        assertTrue(result.getRecommendations().stream()
                .anyMatch(r -> r.contains("contacting the bank directly")),
                "Should say 'contacting the bank directly' instead of 'contacting bank directly'");

        Incident googleIncident = new Incident("7", "u", "Google alert", IncidentType.PHISHING,
                "Suspicious login from google service", IncidentSource.EMAIL,
                LocalDate.now(), "");
        var googleResult = assessor.assess(googleIncident);
        assertTrue(googleResult.getRecommendations().stream()
                .anyMatch(r -> r.contains("contacting Google directly")),
                "Should say 'contacting Google directly' with proper capitalization");
    }

    @Test
    void recommendationsContainNoDuplicates() {
        OTPScamIncident incident = new OTPScamIncident("8", "u", "OTP and bank urgent",
                "Urgent: share OTP and bank account immediately", IncidentSource.SMS,
                LocalDate.now(), "https://fake.link", true, true);
        var result = assessor.assess(incident);
        long uniqueCount = result.getRecommendations().stream().distinct().count();
        assertEquals(uniqueCount, result.getRecommendations().size(),
                "Recommendations list must not contain duplicate items");
    }
}

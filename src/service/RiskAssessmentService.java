package service;

import model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule-based risk assessment engine.
 *
 * OOP – Abstraction: implements the RiskAssessor interface.
 * OOP – Polymorphism: calls incident.getTypeSpecificDetails() which dispatches
 *        to the correct subclass at runtime.
 *
 * Rules are evaluated against the description text and incident metadata.
 * Each matching rule adds a reason and may raise the risk score.
 * The final level is LOW / MEDIUM / HIGH based on cumulative score.
 *
 * IMPORTANT DISCLAIMER: This is a keyword/rule-based educational system.
 * It cannot definitively identify every cyber threat.
 */
public class RiskAssessmentService implements RiskAssessor {

    // --- Rule keywords ---
    private static final String[] HIGH_RISK_KEYWORDS = {
        "otp", "one-time password", "one time password",
        "pin", "password", "passcode",
        "bank account", "account number", "credit card", "debit card",
        "cvv", "expiry", "routing number",
        "urgent", "immediately", "act now", "limited time",
        "your account will be", "suspended", "blocked", "terminated",
        "verify your identity", "confirm your identity",
        "aadhaar", "social security", "tax id", "national id"
    };

    private static final String[] MEDIUM_RISK_KEYWORDS = {
        "click here", "click the link", "follow this link",
        "verify", "validate", "confirm",
        "prize", "winner", "congratulations", "reward", "gift",
        "free offer", "limited offer",
        "login", "sign in", "update your",
        "invoice", "payment due", "overdue"
    };

    private static final String[] IMPERSONATION_KEYWORDS = {
        "google", "microsoft", "amazon", "apple", "facebook", "instagram",
        "whatsapp", "paypal", "ebay", "bank", "irs", "income tax",
        "police", "fbi", "customs", "government", "official"
    };

    @Override
    public AssessmentResult assess(Incident incident) {
        List<String> reasons         = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        int score = 0;

        String descLower = incident.getDescription() == null
                           ? "" : incident.getDescription().toLowerCase();
        String titleLower = incident.getTitle() == null
                            ? "" : incident.getTitle().toLowerCase();
        String urlLower   = incident.getSuspiciousUrl() == null
                            ? "" : incident.getSuspiciousUrl().toLowerCase();
        String combined   = descLower + " " + titleLower;

        // --- Rule 1: Incident type base score ---
        score += baseScoreForType(incident.getType(), reasons);

        // --- Rule 2: High-risk keyword scan ---
        for (String kw : HIGH_RISK_KEYWORDS) {
            if (combined.contains(kw)) {
                score += 3;
                reasons.add("Description mentions sensitive information keyword: \"" + kw + "\"");
                recommendations.add("Never share " + kw + " with anyone under any circumstances.");
                break; // count category once
            }
        }

        // --- Rule 3: Medium-risk keyword scan ---
        int mediumHits = 0;
        for (String kw : MEDIUM_RISK_KEYWORDS) {
            if (combined.contains(kw)) {
                mediumHits++;
            }
        }
        if (mediumHits > 0) {
            score += Math.min(mediumHits * 1, 4);
            reasons.add("Description contains " + mediumHits + " social-engineering indicator(s) "
                       + "(urgency, reward claims, or action prompts).");
            recommendations.add("Be sceptical of unsolicited messages asking you to click links or verify accounts.");
        }

        // --- Rule 4: Impersonation keyword scan ---
        for (String kw : IMPERSONATION_KEYWORDS) {
            if (combined.contains(kw)) {
                score += 2;
                reasons.add("Message may impersonate a trusted brand or authority: \"" + kw + "\".");
                recommendations.add("Always verify requests by contacting " + kw + " directly through their official website or customer-service number.");
                break;
            }
        }

        // --- Rule 5: Suspicious URL present ---
        if (urlLower != null && !urlLower.isBlank()) {
            score += 2;
            reasons.add("A suspicious URL was recorded with this incident.");
            recommendations.add("Do NOT open, copy, or visit the suspicious URL.");
            recommendations.add("Report the URL to your internet service provider or a cybercrime helpline.");
        }

        // --- Rule 6: Source-based adjustments ---
        if (incident.getSource() == IncidentSource.PHONE_CALL) {
            score += 1;
            reasons.add("Phone call is a common vector for vishing (voice phishing) attacks.");
            recommendations.add("Hang up and call back using the number on the official website.");
        } else if (incident.getSource() == IncidentSource.SMS) {
            score += 1;
            reasons.add("SMS is a common smishing (SMS phishing) channel.");
            recommendations.add("Block the sender number and report it as spam.");
        }

        // --- Rule 7: Subclass-specific details (Polymorphism) ---
        String typeDetails = incident.getTypeSpecificDetails();
        // OTPScamIncident / SuspiciousLinkIncident set critical flags
        if (incident instanceof OTPScamIncident) {
            OTPScamIncident otp = (OTPScamIncident) incident;
            if (otp.isOtpWasShared() || otp.isFinancialInfoShared()) {
                score += 5;
                reasons.add("Critical: sensitive information has already been disclosed to the attacker.");
                recommendations.add("Contact your bank or service provider IMMEDIATELY to secure your account.");
                recommendations.add("File a report with your local cybercrime authority.");
            }
        } else if (incident instanceof SuspiciousLinkIncident) {
            SuspiciousLinkIncident sli = (SuspiciousLinkIncident) incident;
            if (sli.isLinkWasClicked()) {
                score += 3;
                reasons.add("The suspicious link was already clicked.");
                recommendations.add("Run a full antivirus/malware scan on your device.");
                recommendations.add("Change passwords for any accounts accessed after clicking the link.");
            }
        } else if (incident instanceof PhishingIncident) {
            PhishingIncident pi = (PhishingIncident) incident;
            if (pi.isContainedAttachment()) {
                score += 2;
                reasons.add("Message contained a suspicious attachment.");
                recommendations.add("Do NOT open the attachment. Delete the message immediately.");
            }
        }

        // Add general recommendations
        addGeneralRecommendations(incident, recommendations);

        // --- Classify ---
        RiskLevel level;
        if (score >= 8) {
            level = RiskLevel.HIGH;
            if (reasons.isEmpty()) reasons.add("Multiple high-risk indicators detected.");
        } else if (score >= 4) {
            level = RiskLevel.MEDIUM;
            if (reasons.isEmpty()) reasons.add("Moderate risk indicators detected.");
        } else {
            level = RiskLevel.LOW;
            if (reasons.isEmpty()) reasons.add("No strong risk indicators found in the provided description.");
        }

        // Deduplicate recommendations
        List<String> uniqueRec = deduplicateList(recommendations);

        return new AssessmentResult(level, reasons, uniqueRec);
    }

    // --- Helpers ---

    private int baseScoreForType(IncidentType type, List<String> reasons) {
        switch (type) {
            case OTP_SCAM:
                reasons.add("OTP scams are inherently high-severity incidents targeting financial accounts.");
                return 4;
            case PHISHING:
                reasons.add("Phishing is a prevalent credential-theft technique.");
                return 3;
            case SUSPICIOUS_LINK:
                reasons.add("Suspicious links may lead to malware or credential-harvesting sites.");
                return 3;
            case IMPERSONATION:
                reasons.add("Impersonation attacks often escalate to identity theft or financial fraud.");
                return 3;
            case SUSPICIOUS_EMAIL:
                reasons.add("Suspicious emails frequently carry phishing content or malicious attachments.");
                return 2;
            default:
                reasons.add("Incident type is unspecified; manual review is recommended.");
                return 1;
        }
    }

    private void addGeneralRecommendations(Incident incident, List<String> recommendations) {
        recommendations.add("Enable two-factor authentication (2FA) on all important accounts.");
        recommendations.add("Keep your device's operating system and apps up to date.");
        recommendations.add("Do not reuse passwords across multiple services.");

        switch (incident.getType()) {
            case PHISHING:
            case SUSPICIOUS_EMAIL:
                recommendations.add("Check the sender's actual email domain carefully for typosquatting.");
                break;
            case OTP_SCAM:
                recommendations.add("Legitimate banks and services never ask for OTPs via phone or message.");
                break;
            case IMPERSONATION:
                recommendations.add("Verify the identity of callers/senders through official channels before acting.");
                break;
            case SUSPICIOUS_LINK:
                recommendations.add("Hover over (but don't click) links to preview the destination URL.");
                break;
            default:
                break;
        }
    }

    private List<String> deduplicateList(List<String> list) {
        List<String> result = new ArrayList<>();
        for (String item : list) {
            if (!result.contains(item)) result.add(item);
        }
        return result;
    }
}

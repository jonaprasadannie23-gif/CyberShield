package com.cybershield.service;

import com.cybershield.model.AssessmentResult;
import com.cybershield.model.Incident;
import com.cybershield.model.IncidentSource;
import com.cybershield.model.IncidentType;
import com.cybershield.model.OTPScamIncident;
import com.cybershield.model.PhishingIncident;
import com.cybershield.model.RiskLevel;
import com.cybershield.model.SuspiciousLinkIncident;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule-based educational risk engine adapted from the original Swing project.
 * OOP – Abstraction: implements RiskAssessor.
 * OOP – Polymorphism: calls getTypeSpecificDetails() and inspects subclasses.
 */
@Service
public class RiskAssessmentService implements RiskAssessor {

    private static final String[] OTP_KEYWORDS = {
            "otp", "one-time password", "one time password"
    };

    private static final String[] CREDENTIAL_KEYWORDS = {
            "pin", "password", "passcode"
    };

    private static final String[] FINANCIAL_KEYWORDS = {
            "bank account", "account number", "credit card", "debit card",
            "cvv", "expiry", "routing number"
    };

    private static final String[] IDENTITY_KEYWORDS = {
            "aadhaar", "social security", "tax id", "national id"
    };

    private static final String[] URGENCY_KEYWORDS = {
            "urgent", "immediately", "act now", "limited time",
            "your account will be", "suspended", "blocked", "terminated",
            "verify your identity", "confirm your identity"
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
        List<String> reasons = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        int score = 0;

        String descLower = incident.getDescription() == null ? "" : incident.getDescription().toLowerCase();
        String titleLower = incident.getTitle() == null ? "" : incident.getTitle().toLowerCase();
        String urlLower = incident.getSuspiciousUrl() == null ? "" : incident.getSuspiciousUrl().toLowerCase();
        String combined = descLower + " " + titleLower;

        score += baseScoreForType(incident.getType(), reasons);

        for (String kw : OTP_KEYWORDS) {
            if (combined.contains(kw)) {
                score += 3;
                reasons.add("Description mentions one-time password (OTP) verification: \"" + kw + "\".");
                recommendations.add("Never share your OTP with anyone under any circumstances.");
                break;
            }
        }

        for (String kw : CREDENTIAL_KEYWORDS) {
            if (combined.contains(kw)) {
                score += 3;
                reasons.add("Description mentions authentication credential keyword: \"" + kw + "\".");
                recommendations.add("Never share your passwords, PINs, or security passcodes with anyone.");
                break;
            }
        }

        for (String kw : FINANCIAL_KEYWORDS) {
            if (combined.contains(kw)) {
                score += 3;
                reasons.add("Description mentions sensitive financial data: \"" + kw + "\".");
                recommendations.add("Never disclose banking credentials, card numbers, or CVVs over unverified channels.");
                break;
            }
        }

        for (String kw : IDENTITY_KEYWORDS) {
            if (combined.contains(kw)) {
                score += 3;
                reasons.add("Description mentions government or national identity information: \"" + kw + "\".");
                recommendations.add("Protect government-issued IDs and personal identity numbers from unverified requests.");
                break;
            }
        }

        for (String kw : URGENCY_KEYWORDS) {
            if (combined.contains(kw)) {
                score += 3;
                reasons.add("Description contains urgency or account-threat language: \"" + kw + "\".");
                recommendations.add("Do not react to urgent threats or artificial time pressure; verify your account status directly through official channels.");
                break;
            }
        }

        int mediumHits = 0;
        for (String kw : MEDIUM_RISK_KEYWORDS) {
            if (combined.contains(kw)) {
                mediumHits++;
            }
        }
        if (mediumHits > 0) {
            score += Math.min(mediumHits, 4);
            reasons.add("Description contains " + mediumHits + " social-engineering indicator(s) "
                    + "(urgency, reward claims, or action prompts).");
            recommendations.add("Be skeptical of unsolicited messages asking you to click links or verify accounts.");
        }

        for (String kw : IMPERSONATION_KEYWORDS) {
            if (combined.contains(kw)) {
                score += 2;
                reasons.add("Message may impersonate a trusted brand or authority: \"" + formatEntityDisplayName(kw) + "\".");
                recommendations.add("Always verify requests by contacting " + formatEntityTarget(kw)
                        + " directly through their official website or customer-service number.");
                break;
            }
        }

        if (urlLower != null && !urlLower.isBlank()) {
            score += 2;
            reasons.add("A suspicious URL was recorded with this incident.");
            recommendations.add("Do NOT open, copy, or visit the suspicious URL.");
            recommendations.add("Report the URL to your internet service provider or a cybercrime helpline.");
        }

        if (incident.getSource() == IncidentSource.PHONE_CALL) {
            score += 1;
            reasons.add("Phone call is a common vector for vishing (voice phishing) attacks.");
            recommendations.add("Hang up and call back using the number on the official website.");
        } else if (incident.getSource() == IncidentSource.SMS) {
            score += 1;
            reasons.add("SMS is a common smishing (SMS phishing) channel.");
            recommendations.add("Block the sender number and report it as spam.");
        }

        incident.getTypeSpecificDetails();

        if (incident instanceof OTPScamIncident otp) {
            recommendations.add("Never share your OTP with anyone under any circumstances.");
            if (otp.isOtpWasShared() || otp.isFinancialInfoShared()) {
                score += 5;
                reasons.add("Critical: sensitive information has already been disclosed to the attacker.");
                recommendations.add("Contact your bank or service provider IMMEDIATELY to secure your account.");
                recommendations.add("File a report with your local cybercrime authority.");
            }
        } else if (incident instanceof SuspiciousLinkIncident sli) {
            if (sli.isLinkWasClicked()) {
                score += 3;
                reasons.add("The suspicious link was already clicked.");
                recommendations.add("Run a full antivirus/malware scan on your device.");
                recommendations.add("Change passwords for any accounts accessed after clicking the link.");
            }
        } else if (incident instanceof PhishingIncident pi) {
            if (pi.isContainedAttachment()) {
                score += 2;
                reasons.add("Message contained a suspicious attachment.");
                recommendations.add("Do NOT open the attachment. Delete the message immediately.");
            }
        }

        addGeneralRecommendations(incident, recommendations);

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

        return new AssessmentResult(level, reasons, deduplicateList(recommendations));
    }

    private int baseScoreForType(IncidentType type, List<String> reasons) {
        if (type == null) {
            reasons.add("Incident type is unspecified; manual review is recommended.");
            return 1;
        }
        return switch (type) {
            case OTP_SCAM -> {
                reasons.add("OTP scams are inherently high-severity incidents targeting financial accounts.");
                yield 4;
            }
            case PHISHING -> {
                reasons.add("Phishing is a prevalent credential-theft technique.");
                yield 3;
            }
            case SUSPICIOUS_LINK -> {
                reasons.add("Suspicious links may lead to malware or credential-harvesting sites.");
                yield 3;
            }
            case IMPERSONATION -> {
                reasons.add("Impersonation attacks often escalate to identity theft or financial fraud.");
                yield 3;
            }
            case SUSPICIOUS_EMAIL -> {
                reasons.add("Suspicious emails frequently carry phishing content or malicious attachments.");
                yield 2;
            }
            default -> {
                reasons.add("Incident type is unspecified; manual review is recommended.");
                yield 1;
            }
        };
    }

    private void addGeneralRecommendations(Incident incident, List<String> recommendations) {
        recommendations.add("Enable two-factor authentication (2FA) on all important accounts.");
        recommendations.add("Keep your device's operating system and apps up to date.");
        recommendations.add("Do not reuse passwords across multiple services.");

        if (incident.getType() == null) return;
        switch (incident.getType()) {
            case PHISHING, SUSPICIOUS_EMAIL ->
                    recommendations.add("Check the sender's actual email domain carefully for typosquatting.");
            case OTP_SCAM ->
                    recommendations.add("Never share your OTP with anyone. Legitimate banks and services will never ask for your one-time password.");
            case IMPERSONATION ->
                    recommendations.add("Verify the identity of callers/senders through official channels before acting.");
            case SUSPICIOUS_LINK ->
                    recommendations.add("Hover over (but don't click) links to preview the destination URL.");
            default -> { }
        }
    }

    private String formatEntityDisplayName(String kw) {
        return switch (kw) {
            case "bank" -> "Bank";
            case "police" -> "Police";
            case "government" -> "Government Agency";
            case "customs" -> "Customs";
            case "irs" -> "IRS";
            case "income tax" -> "Income Tax Department";
            case "fbi" -> "FBI";
            case "official" -> "Official Authority";
            case "whatsapp" -> "WhatsApp";
            case "paypal" -> "PayPal";
            case "ebay" -> "eBay";
            default -> Character.toUpperCase(kw.charAt(0)) + kw.substring(1);
        };
    }

    private String formatEntityTarget(String kw) {
        return switch (kw) {
            case "bank" -> "the bank";
            case "police" -> "the police";
            case "government" -> "the official government department";
            case "customs" -> "customs authorities";
            case "irs" -> "the IRS";
            case "income tax" -> "the tax authority";
            case "fbi" -> "law enforcement";
            case "official" -> "the official organization";
            case "whatsapp" -> "WhatsApp";
            case "paypal" -> "PayPal";
            case "ebay" -> "eBay";
            default -> Character.toUpperCase(kw.charAt(0)) + kw.substring(1);
        };
    }

    private List<String> deduplicateList(List<String> list) {
        List<String> result = new ArrayList<>();
        for (String item : list) {
            if (!result.contains(item)) result.add(item);
        }
        return result;
    }
}

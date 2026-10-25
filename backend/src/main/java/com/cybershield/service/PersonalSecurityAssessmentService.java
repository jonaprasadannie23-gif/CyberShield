package com.cybershield.service;

import com.cybershield.exception.ForbiddenException;
import com.cybershield.exception.NotFoundException;
import com.cybershield.model.AssessmentCategory;
import com.cybershield.model.AssessmentRecommendation;
import com.cybershield.model.PracticeFinding;
import com.cybershield.model.SecurityAssessment;
import com.cybershield.repository.AssessmentRepository;
import com.cybershield.util.IDGenerator;
import com.cybershield.util.InputValidator;
import com.cybershield.web.dto.AssessmentRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dedicated backend scoring engine for Personal Security Assessments.
 * OOP – Encapsulation: scoring weights, rule evaluations, input validation,
 * and deterministic calculations are kept strictly server-side. Client-supplied
 * scores are never accepted.
 */
@Service
public class PersonalSecurityAssessmentService {

    public static final String DISCLAIMER =
            "Educational Assessment Notice: This assessment score is a rule-based educational indicator "
                    + "calculated solely from your self-reported practices. It is not an empirical guarantee of security, "
                    + "an official safety certification, or a predictive guarantee of immunity against cyber attacks. "
                    + "Practicing strong security hygiene reduces your risk exposure but cannot eliminate all threats.";

    public static final String SCORE_SCALE_INTERPRETATION =
            "Score Scale Interpretation:\n"
                    + "• 85–100 (Excellent): Indicates rigorous adherence to recommended safety habits across most critical areas.\n"
                    + "• 70–84 (Good): Indicates solid foundational safety habits with a few specific areas for improvement.\n"
                    + "• 50–69 (Fair): Indicates partial adoption of essential safety practices with significant exposure in key areas.\n"
                    + "• 0–49 (At Risk): Indicates substantial gaps in basic hygiene that leave accounts and data vulnerable to common attacks.";

    private final AssessmentRepository assessmentRepository;

    public PersonalSecurityAssessmentService(AssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
    }

    /**
     * Validates answers, executes deterministic scoring engine, and persists the assessment.
     */
    public SecurityAssessment evaluateAndSave(String userId, AssessmentRequest request) {
        if (!InputValidator.isNotBlank(userId)) {
            throw new IllegalArgumentException("User ID is required.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Assessment submission cannot be null.");
        }

        validateSubmission(request);

        Map<String, String> answersMap = new LinkedHashMap<>();
        answersMap.put("twoFactorAuth", request.getTwoFactorAuth().trim());
        answersMap.put("passwordReuse", request.getPasswordReuse().trim());
        answersMap.put("strongPasswords", request.getStrongPasswords().trim());
        answersMap.put("systemUpdates", request.getSystemUpdates().trim());
        answersMap.put("linkVerification", request.getLinkVerification().trim());
        answersMap.put("recoveryMethods", request.getRecoveryMethods().trim());
        answersMap.put("publicWifiPrecautions", request.getPublicWifiPrecautions().trim());
        answersMap.put("phishingRecognition", request.getPhishingRecognition().trim());

        List<PracticeFinding> findings = new ArrayList<>();
        List<AssessmentRecommendation> recommendations = new ArrayList<>();

        int totalScore = 0;

        // 1. Two-Factor Authentication (max 15 pts)
        PracticeFinding f1 = evaluateTwoFactor(request.getTwoFactorAuth().trim());
        findings.add(f1);
        totalScore += f1.getScore();
        addRecommendationIfApplicable(f1, recommendations);

        // 2. Password Reuse (max 15 pts)
        PracticeFinding f2 = evaluatePasswordReuse(request.getPasswordReuse().trim());
        findings.add(f2);
        totalScore += f2.getScore();
        addRecommendationIfApplicable(f2, recommendations);

        // 3. Strong Passwords (max 15 pts)
        PracticeFinding f3 = evaluateStrongPasswords(request.getStrongPasswords().trim());
        findings.add(f3);
        totalScore += f3.getScore();
        addRecommendationIfApplicable(f3, recommendations);

        // 4. System Updates (max 10 pts)
        PracticeFinding f4 = evaluateSystemUpdates(request.getSystemUpdates().trim());
        findings.add(f4);
        totalScore += f4.getScore();
        addRecommendationIfApplicable(f4, recommendations);

        // 5. Link Verification (max 15 pts)
        PracticeFinding f5 = evaluateLinkVerification(request.getLinkVerification().trim());
        findings.add(f5);
        totalScore += f5.getScore();
        addRecommendationIfApplicable(f5, recommendations);

        // 6. Recovery Methods (max 10 pts)
        PracticeFinding f6 = evaluateRecoveryMethods(request.getRecoveryMethods().trim());
        findings.add(f6);
        totalScore += f6.getScore();
        addRecommendationIfApplicable(f6, recommendations);

        // 7. Public Wi-Fi (max 10 pts)
        PracticeFinding f7 = evaluatePublicWifi(request.getPublicWifiPrecautions().trim());
        findings.add(f7);
        totalScore += f7.getScore();
        addRecommendationIfApplicable(f7, recommendations);

        // 8. Phishing Recognition (max 10 pts)
        PracticeFinding f8 = evaluatePhishingRecognition(request.getPhishingRecognition().trim());
        findings.add(f8);
        totalScore += f8.getScore();
        addRecommendationIfApplicable(f8, recommendations);

        // Sort recommendations by priority: HIGH first, then MEDIUM, then LOW
        recommendations.sort(Comparator.comparingInt(r -> {
            switch (r.getPriority()) {
                case "HIGH": return 1;
                case "MEDIUM": return 2;
                default: return 3;
            }
        }));

        // Clamp score to bounds [0, 100]
        int finalScore = Math.max(0, Math.min(100, totalScore));
        AssessmentCategory category = AssessmentCategory.fromScore(finalScore);

        SecurityAssessment assessment = new SecurityAssessment(
                IDGenerator.generate(),
                userId,
                finalScore,
                category,
                category.getLabel(),
                category.getSummary(),
                SCORE_SCALE_INTERPRETATION,
                DISCLAIMER,
                answersMap,
                findings,
                recommendations,
                LocalDateTime.now()
        );

        assessmentRepository.save(assessment);
        return assessment;
    }

    public List<SecurityAssessment> getUserAssessments(String userId) {
        if (!InputValidator.isNotBlank(userId)) {
            return Collections.emptyList();
        }
        return assessmentRepository.findByUserId(userId);
    }

    public SecurityAssessment getAssessment(String assessmentId, String userId) {
        if (!InputValidator.isNotBlank(assessmentId)) {
            throw new IllegalArgumentException("Assessment ID is required.");
        }
        SecurityAssessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new NotFoundException("Assessment not found: " + assessmentId));

        if (!assessment.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to view this assessment.");
        }
        return assessment;
    }

    public SecurityAssessment getLatestAssessment(String userId) {
        if (!InputValidator.isNotBlank(userId)) {
            return null;
        }
        return assessmentRepository.findLatestByUserId(userId).orElse(null);
    }

    // ── Input Validation ────────────────────────────────────────────────────────

    private void validateSubmission(AssessmentRequest req) {
        requireChoice("twoFactorAuth", req.getTwoFactorAuth(), "ALWAYS", "SOMETIMES", "NEVER", "NOT_SURE");
        requireChoice("passwordReuse", req.getPasswordReuse(), "NEVER", "RARELY", "SOMETIMES", "FREQUENTLY", "NOT_SURE");
        requireChoice("strongPasswords", req.getStrongPasswords(), "MANAGER_OR_STRONG", "MEMORIZED_COMPLEX", "SIMPLE_PATTERNS", "NOT_SURE");
        requireChoice("systemUpdates", req.getSystemUpdates(), "AUTOMATIC", "REGULAR_MANUAL", "DELAYED", "NOT_SURE");
        requireChoice("linkVerification", req.getLinkVerification(), "ALWAYS_VERIFY", "SOMETIMES_VERIFY", "RARELY_VERIFY", "NOT_SURE");
        requireChoice("recoveryMethods", req.getRecoveryMethods(), "UP_TO_DATE_SECURE", "BASIC_EMAIL_SMS", "OUTDATED_OR_WEAK", "NOT_SURE");
        requireChoice("publicWifiPrecautions", req.getPublicWifiPrecautions(), "ALWAYS_PROTECTED", "NEVER_USE_PUBLIC", "BASIC_HTTPS", "NO_PRECAUTIONS", "NOT_SURE");
        requireChoice("phishingRecognition", req.getPhishingRecognition(), "VERY_CONFIDENT", "SOMEWHAT_CONFIDENT", "NOT_CONFIDENT", "NOT_SURE");
    }

    private void requireChoice(String fieldName, String value, String... allowed) {
        if (!InputValidator.isNotBlank(value)) {
            throw new IllegalArgumentException("Missing required answer for: " + fieldName);
        }
        String trimmed = value.trim();
        for (String valid : allowed) {
            if (valid.equals(trimmed)) return;
        }
        throw new IllegalArgumentException("Invalid option '" + trimmed + "' for practice: " + fieldName);
    }

    // ── Rule-Based Practice Evaluators ─────────────────────────────────────────

    private PracticeFinding evaluateTwoFactor(String answer) {
        switch (answer) {
            case "ALWAYS":
                return new PracticeFinding(
                        "twoFactorAuth",
                        "Two-Factor Authentication (2FA)",
                        answer,
                        "Enabled on all important accounts (email, banking, social media)",
                        15, 15, "SECURE",
                        "Full score (+15 pts): Multi-factor authentication provides strong protection against unauthorized access even if passwords are leaked or guessed.",
                        null, null, null
                );
            case "SOMETIMES":
                return new PracticeFinding(
                        "twoFactorAuth",
                        "Two-Factor Authentication (2FA)",
                        answer,
                        "Enabled on some accounts, but not all",
                        8, 15, "MODERATE",
                        "Partial score (+8 pts): While some accounts are protected, critical accounts without 2FA remain vulnerable to credential theft and automated attacks.",
                        "Enable two-factor authentication on all critical accounts, especially your primary email and banking portals.",
                        "Enable 2FA across all critical accounts",
                        "Set up two-factor authentication on your primary email, banking apps, and major social accounts."
                );
            case "NEVER":
                return new PracticeFinding(
                        "twoFactorAuth",
                        "Two-Factor Authentication (2FA)",
                        answer,
                        "Not enabled on any accounts",
                        0, 15, "VULNERABLE",
                        "Zero score (+0 pts): Accounts without two-factor authentication rely solely on single passwords, making them vulnerable to credential breaches and automated guessing.",
                        "Enable two-factor authentication (authenticator app or security keys) on all your key accounts immediately.",
                        "Turn on two-factor authentication",
                        "Enable an authenticator app (such as Google Authenticator or Bitwarden) for your primary email and financial services."
                );
            case "NOT_SURE":
            default:
                return new PracticeFinding(
                        "twoFactorAuth",
                        "Two-Factor Authentication (2FA)",
                        answer,
                        "Not sure if 2FA is enabled or what it is",
                        4, 15, "NEEDS_ATTENTION",
                        "Cautionary score (+4 pts): Unverified 2FA configuration leaves account defense in an unknown state. Unprotected accounts are treated as at risk.",
                        "Audit your account settings to verify and activate two-factor authentication on key services.",
                        "Review and activate two-factor authentication",
                        "Check the security settings of your email and primary online services to confirm 2FA is active."
                );
        }
    }

    private PracticeFinding evaluatePasswordReuse(String answer) {
        switch (answer) {
            case "NEVER":
                return new PracticeFinding(
                        "passwordReuse",
                        "Password Reuse Across Accounts",
                        answer,
                        "Every account has a unique password",
                        15, 15, "SECURE",
                        "Full score (+15 pts): Using distinct passwords isolates breaches, ensuring a leak on one service cannot compromise your other accounts.",
                        null, null, null
                );
            case "RARELY":
                return new PracticeFinding(
                        "passwordReuse",
                        "Password Reuse Across Accounts",
                        answer,
                        "Only reuse passwords for throwaway or unimportant websites",
                        10, 15, "MODERATE",
                        "Minor deduction (+10 pts): Keeping critical accounts separate is good, but secondary sites can still be leveraged for phishing or identity mapping.",
                        "Transition remaining throwaway accounts to unique passwords using a password manager.",
                        "Eliminate remaining password reuse",
                        "Generate distinct passwords even for non-essential websites using a password manager."
                );
            case "SOMETIMES":
                return new PracticeFinding(
                        "passwordReuse",
                        "Password Reuse Across Accounts",
                        answer,
                        "A few passwords shared across multiple websites",
                        4, 15, "MODERATE",
                        "Significant deduction (+4 pts): Reusing passwords across multiple services exposes you to credential stuffing attacks if any single service is breached.",
                        "Identify and replace all shared passwords with unique credentials.",
                        "Change shared passwords to unique passwords",
                        "Audit your accounts and change any password that is shared across more than one website."
                );
            case "FREQUENTLY":
                return new PracticeFinding(
                        "passwordReuse",
                        "Password Reuse Across Accounts",
                        answer,
                        "Same password used for most or all accounts",
                        0, 15, "VULNERABLE",
                        "Zero score (+0 pts): Reusing a single password across multiple accounts creates a severe single point of failure; a single compromise grants access to your entire digital life.",
                        "Immediately reset your passwords, prioritizing your primary email and financial accounts with distinct credentials.",
                        "Reset reused passwords immediately",
                        "Assign a unique password to each service, starting with your email, bank, and password vault."
                );
            case "NOT_SURE":
            default:
                return new PracticeFinding(
                        "passwordReuse",
                        "Password Reuse Across Accounts",
                        answer,
                        "Not sure how many accounts share passwords",
                        2, 15, "NEEDS_ATTENTION",
                        "Cautionary score (+2 pts): Unchecked password reuse makes it likely that old passwords have been shared or compromised in past breaches.",
                        "Use a password manager to audit your credentials and eliminate duplicate passwords.",
                        "Audit passwords for reuse",
                        "Review all your saved logins and replace repeated passwords with unique ones."
                );
        }
    }

    private PracticeFinding evaluateStrongPasswords(String answer) {
        switch (answer) {
            case "MANAGER_OR_STRONG":
                return new PracticeFinding(
                        "strongPasswords",
                        "Password Strength & Generation",
                        answer,
                        "Use a password manager or randomly generated complex passwords (16+ chars)",
                        15, 15, "SECURE",
                        "Full score (+15 pts): High-entropy random passwords are mathematically resistant to brute-force and dictionary attacks.",
                        null, null, null
                );
            case "MEMORIZED_COMPLEX":
                return new PracticeFinding(
                        "strongPasswords",
                        "Password Strength & Generation",
                        answer,
                        "Memorized complex passwords with mixed symbols and numbers",
                        10, 15, "MODERATE",
                        "Partial score (+10 pts): Complex memorized passwords are good, but human memory often relies on predictable substitution patterns.",
                        "Adopt a reputable password manager to generate truly random passphrases of 16+ characters.",
                        "Adopt a password manager for high-entropy passwords",
                        "Install and configure a trusted password manager (e.g., Bitwarden) for random credential generation."
                );
            case "SIMPLE_PATTERNS":
                return new PracticeFinding(
                        "strongPasswords",
                        "Password Strength & Generation",
                        answer,
                        "Short passwords, dictionary words, names, or simple patterns",
                        0, 15, "VULNERABLE",
                        "Zero score (+0 pts): Simple or patterned passwords can be cracked in seconds by automated dictionary and pattern-guessing tools.",
                        "Replace simple passwords with long, complex passphrases of at least 14-16 characters.",
                        "Replace weak passwords with long passphrases",
                        "Update simple or short passwords with unique passphrases composed of multiple random words or characters."
                );
            case "NOT_SURE":
            default:
                return new PracticeFinding(
                        "strongPasswords",
                        "Password Strength & Generation",
                        answer,
                        "Not sure how strong my passwords are",
                        4, 15, "NEEDS_ATTENTION",
                        "Cautionary score (+4 pts): Unverified password complexity often conceals predictable or easily crackable credentials.",
                        "Test your password practices using our offline Password Checker and update weak ones.",
                        "Evaluate and upgrade password strength",
                        "Check your password habits against best practices and replace any weak credentials."
                );
        }
    }

    private PracticeFinding evaluateSystemUpdates(String answer) {
        switch (answer) {
            case "AUTOMATIC":
                return new PracticeFinding(
                        "systemUpdates",
                        "Operating System & Application Updates",
                        answer,
                        "Automatic updates enabled and installed promptly",
                        10, 10, "SECURE",
                        "Full score (+10 pts): Applying security updates automatically eliminates known vulnerabilities before exploit kits can leverage them.",
                        null, null, null
                );
            case "REGULAR_MANUAL":
                return new PracticeFinding(
                        "systemUpdates",
                        "Operating System & Application Updates",
                        answer,
                        "Manually check and install updates every few weeks",
                        7, 10, "MODERATE",
                        "Partial score (+7 pts): Regular manual patching is helpful, but leaves a window of exposure between security patch releases and installation.",
                        "Enable automatic updates for your OS, web browsers, and key security software.",
                        "Enable automatic updates on all devices",
                        "Turn on automatic updates in Windows/macOS/iOS/Android and your web browsers."
                );
            case "DELAYED":
                return new PracticeFinding(
                        "systemUpdates",
                        "Operating System & Application Updates",
                        answer,
                        "Postpone or ignore updates for months",
                        0, 10, "VULNERABLE",
                        "Zero score (+0 pts): Unpatched systems contain well-documented vulnerabilities that cybercriminals routinely exploit through drive-by downloads and exploit kits.",
                        "Immediately check for and apply all pending system, browser, and software updates.",
                        "Install all pending software and OS updates",
                        "Run system update tools on your PC and mobile devices to patch known security vulnerabilities."
                );
            case "NOT_SURE":
            default:
                return new PracticeFinding(
                        "systemUpdates",
                        "Operating System & Application Updates",
                        answer,
                        "Not sure if updates are turned on or up to date",
                        3, 10, "NEEDS_ATTENTION",
                        "Cautionary score (+3 pts): If update automation is unverified, critical security patches may be missing.",
                        "Verify update settings across your computer and smartphone.",
                        "Verify system update configuration",
                        "Check Windows Update or mobile system settings to confirm automatic updates are active."
                );
        }
    }

    private PracticeFinding evaluateLinkVerification(String answer) {
        switch (answer) {
            case "ALWAYS_VERIFY":
                return new PracticeFinding(
                        "linkVerification",
                        "Verification of Suspicious Links",
                        answer,
                        "Always verify sender, inspect URLs, and never click untrusted links",
                        15, 15, "SECURE",
                        "Full score (+15 pts): Proactive inspection of domain names, sender headers, and destination URLs prevents phishing and drive-by malware infections.",
                        null, null, null
                );
            case "SOMETIMES_VERIFY":
                return new PracticeFinding(
                        "linkVerification",
                        "Verification of Suspicious Links",
                        answer,
                        "Check links only when something looks noticeably suspicious",
                        6, 15, "MODERATE",
                        "Partial score (+6 pts): Checking only obvious scams leaves you susceptible to sophisticated spear-phishing and typosquatted domains.",
                        "Develop a strict habit of hovering over all links and verifying the exact domain before clicking.",
                        "Practice universal link inspection",
                        "Always inspect the true destination URL and sender domain before clicking links in emails or messages."
                );
            case "RARELY_VERIFY":
                return new PracticeFinding(
                        "linkVerification",
                        "Verification of Suspicious Links",
                        answer,
                        "Rarely or never verify links before clicking",
                        0, 15, "VULNERABLE",
                        "Zero score (+0 pts): Clicking unverified links is the primary infection vector for credential harvesting, malware droppers, and phishing scams.",
                        "Never click links in unsolicited emails or SMS messages; navigate directly to official websites instead.",
                        "Adopt safe link browsing practices",
                        "Avoid clicking direct links from emails or messages; bookmark official websites or type URLs directly."
                );
            case "NOT_SURE":
            default:
                return new PracticeFinding(
                        "linkVerification",
                        "Verification of Suspicious Links",
                        answer,
                        "Not sure how to verify links safely",
                        3, 15, "NEEDS_ATTENTION",
                        "Cautionary score (+3 pts): Lacking knowledge of URL inspection methods leaves you exposed to deceptive social engineering.",
                        "Learn how to preview destination URLs and verify domain certificates.",
                        "Learn link verification techniques",
                        "Review guidelines on how to inspect email headers and preview link destinations before opening."
                );
        }
    }

    private PracticeFinding evaluateRecoveryMethods(String answer) {
        switch (answer) {
            case "UP_TO_DATE_SECURE":
                return new PracticeFinding(
                        "recoveryMethods",
                        "Account Recovery & Backup Methods",
                        answer,
                        "Dedicated backup email and offline recovery codes stored safely",
                        10, 10, "SECURE",
                        "Full score (+10 pts): Secure offline backup codes and separate recovery channels protect against lockouts without introducing weak recovery backdoors.",
                        null, null, null
                );
            case "BASIC_EMAIL_SMS":
                return new PracticeFinding(
                        "recoveryMethods",
                        "Account Recovery & Backup Methods",
                        answer,
                        "Standard phone number or primary email recovery without offline backup codes",
                        6, 10, "MODERATE",
                        "Partial score (+6 pts): SMS recovery is vulnerable to SIM-swap attacks, and sharing recovery to your primary email creates a mutual lockout loop if compromised.",
                        "Download offline emergency backup codes for your 2FA accounts and store them in an encrypted or physical vault.",
                        "Generate offline backup recovery codes",
                        "Generate and securely store one-time recovery codes for your primary Google, Apple, or Microsoft accounts."
                );
            case "OUTDATED_OR_WEAK":
                return new PracticeFinding(
                        "recoveryMethods",
                        "Account Recovery & Backup Methods",
                        answer,
                        "Outdated contact details or easily guessable security questions",
                        0, 10, "VULNERABLE",
                        "Zero score (+0 pts): Outdated recovery info risks permanent lockout, while predictable security questions (pet name, high school) are easily researched by attackers.",
                        "Update recovery contact details and remove or falsify weak security questions with random strings.",
                        "Update account recovery options and remove weak questions",
                        "Audit recovery emails and phone numbers; replace predictable security question answers with random passphrases."
                );
            case "NOT_SURE":
            default:
                return new PracticeFinding(
                        "recoveryMethods",
                        "Account Recovery & Backup Methods",
                        answer,
                        "Not sure what recovery options are configured",
                        2, 10, "NEEDS_ATTENTION",
                        "Cautionary score (+2 pts): Unverified recovery settings can lead to permanent account loss during an incident or compromise.",
                        "Review the recovery options configured on your core accounts.",
                        "Review account recovery configuration",
                        "Check account recovery settings on your email and password manager to ensure up-to-date contact information."
                );
        }
    }

    private PracticeFinding evaluatePublicWifi(String answer) {
        switch (answer) {
            case "ALWAYS_PROTECTED":
                return new PracticeFinding(
                        "publicWifiPrecautions",
                        "Public Wi-Fi Precautions",
                        answer,
                        "Always use a VPN or mobile hotspot, and avoid sensitive accounts",
                        10, 10, "SECURE",
                        "Full score (+10 pts): Encrypting traffic through a trustworthy VPN or mobile hotspot protects data against eavesdropping and rogue access points.",
                        null, null, null
                );
            case "NEVER_USE_PUBLIC":
                return new PracticeFinding(
                        "publicWifiPrecautions",
                        "Public Wi-Fi Precautions",
                        answer,
                        "Never connect to public Wi-Fi networks",
                        10, 10, "SECURE",
                        "Full score (+10 pts): Completely avoiding untrusted public Wi-Fi networks eliminates exposure to local network interception and rogue hotspots.",
                        null, null, null
                );
            case "BASIC_HTTPS":
                return new PracticeFinding(
                        "publicWifiPrecautions",
                        "Public Wi-Fi Precautions",
                        answer,
                        "Connect to public Wi-Fi but rely on HTTPS and avoid banking",
                        5, 10, "MODERATE",
                        "Partial score (+5 pts): While HTTPS encrypts application traffic, unencrypted public Wi-Fi still exposes DNS queries, visited domains, and invites SSL-stripping attempts.",
                        "Use a trusted VPN or your smartphone's cellular hotspot when working on public networks.",
                        "Configure a VPN for public network use",
                        "Install and enable a reputable VPN service whenever connecting to coffee shop, hotel, or airport Wi-Fi."
                );
            case "NO_PRECAUTIONS":
                return new PracticeFinding(
                        "publicWifiPrecautions",
                        "Public Wi-Fi Precautions",
                        answer,
                        "Connect freely to open Wi-Fi networks without extra precautions",
                        0, 10, "VULNERABLE",
                        "Zero score (+0 pts): Open Wi-Fi exposes your device to packet sniffing, Evil Twin rogue hotspots, and local network attacks.",
                        "Stop using open Wi-Fi for sensitive work; switch to cellular data tethering or a VPN.",
                        "Disable automatic public Wi-Fi connections",
                        "Configure your phone and laptop to avoid automatically connecting to open, unencrypted Wi-Fi networks."
                );
            case "NOT_SURE":
            default:
                return new PracticeFinding(
                        "publicWifiPrecautions",
                        "Public Wi-Fi Precautions",
                        answer,
                        "Not sure what precautions are needed on public Wi-Fi",
                        2, 10, "NEEDS_ATTENTION",
                        "Cautionary score (+2 pts): Unawareness of wireless network vulnerabilities increases risk of eavesdropping on public hotspots.",
                        "Learn the risks of public Wi-Fi and consider using mobile data instead.",
                        "Review public Wi-Fi security best practices",
                        "Learn how to use mobile hotspot tethering rather than connecting to unprotected public networks."
                );
        }
    }

    private PracticeFinding evaluatePhishingRecognition(String answer) {
        switch (answer) {
            case "VERY_CONFIDENT":
                return new PracticeFinding(
                        "phishingRecognition",
                        "Phishing & Impersonation Recognition",
                        answer,
                        "Confident spotting artificial urgency, spoofed domains, and fake requests",
                        10, 10, "SECURE",
                        "Full score (+10 pts): High scam literacy allows you to recognize social engineering tactics and fake authority pretexts.",
                        null, null, null
                );
            case "SOMEWHAT_CONFIDENT":
                return new PracticeFinding(
                        "phishingRecognition",
                        "Phishing & Impersonation Recognition",
                        answer,
                        "Can spot obvious scams, but unsure about sophisticated impersonations",
                        6, 10, "MODERATE",
                        "Partial score (+6 pts): Scammers increasingly craft high-quality impersonations with accurate logos and contextual details.",
                        "Adopt a strict policy: never act on unexpected urgent requests without out-of-band verification via official phone numbers.",
                        "Establish out-of-band verification rules",
                        "Never transfer money or verify credentials based on inbound communications; call the verified official number directly."
                );
            case "NOT_CONFIDENT":
                return new PracticeFinding(
                        "phishingRecognition",
                        "Phishing & Impersonation Recognition",
                        answer,
                        "Struggle to tell legitimate communications from fraudulent messages",
                        0, 10, "VULNERABLE",
                        "Zero score (+0 pts): Inability to identify social engineering greatly elevates the risk of falling victim to account takeover or financial fraud.",
                        "Familiarize yourself with common phishing indicators and use CyberShield's Incident Assessor whenever you receive suspicious messages.",
                        "Review common phishing and impersonation indicators",
                        "Study the warning signs of phishing (scare tactics, urgent deadlines, mismatched sender domains, generic greetings)."
                );
            case "NOT_SURE":
            default:
                return new PracticeFinding(
                        "phishingRecognition",
                        "Phishing & Impersonation Recognition",
                        answer,
                        "Not sure what phishing or impersonation looks like",
                        2, 10, "NEEDS_ATTENTION",
                        "Cautionary score (+2 pts): Lack of familiarity with modern impersonation scams leaves you vulnerable to deceptive communications.",
                        "Read CyberShield educational guides on identifying phishing messages.",
                        "Study social engineering and scam tactics",
                        "Learn the core tactics scammers use: authority pretexting, artificial urgency, and counterfeit websites."
                );
        }
    }

    private void addRecommendationIfApplicable(PracticeFinding finding, List<AssessmentRecommendation> recommendations) {
        if (finding.getRecommendation() == null || finding.getTaskTitle() == null) {
            return;
        }
        String priority;
        if ("VULNERABLE".equals(finding.getStatus())) {
            priority = "HIGH";
        } else if ("NEEDS_ATTENTION".equals(finding.getStatus()) || "MODERATE".equals(finding.getStatus())) {
            priority = "MEDIUM";
        } else {
            priority = "LOW";
        }

        recommendations.add(new AssessmentRecommendation(
                IDGenerator.generate(),
                finding.getPracticeKey(),
                finding.getTitle(),
                priority,
                finding.getRecommendation(),
                finding.getTaskTitle(),
                finding.getTaskDescription()
        ));
    }
}

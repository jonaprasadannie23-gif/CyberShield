package com.cybershield.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Educational password estimator used by backend tests.
 * The website evaluates strength in the browser so the password is never transmitted.
 */
@Service
public class PasswordStrengthService {

    public enum StrengthLevel {
        VERY_WEAK("Very Weak"),
        WEAK("Weak"),
        FAIR("Fair"),
        STRONG("Strong"),
        VERY_STRONG("Very Strong");

        private final String label;

        StrengthLevel(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public record StrengthResult(StrengthLevel level, int score, List<String> suggestions, String disclaimer) {}

    public StrengthResult evaluate(String password) {
        List<String> suggestions = new ArrayList<>();
        int score = 0;

        if (password == null || password.isEmpty()) {
            suggestions.add("Enter a password to evaluate.");
            return new StrengthResult(StrengthLevel.VERY_WEAK, 0, suggestions, disclaimer());
        }

        int len = password.length();
        if (len < 8) {
            suggestions.add("Use at least 8 characters.");
        } else if (len < 12) {
            score += 15;
            suggestions.add("Consider using 12 or more characters for better strength.");
        } else if (len < 16) {
            score += 25;
        } else {
            score += 35;
        }

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c ->
                "!@#$%^&*()_+-=[]{}|;':\",./<>?".indexOf(c) >= 0);

        if (hasUpper) score += 15; else suggestions.add("Add uppercase letters (A–Z).");
        if (hasLower) score += 15; else suggestions.add("Add lowercase letters (a–z).");
        if (hasDigit) score += 15; else suggestions.add("Include numbers (0–9).");
        if (hasSpecial) score += 20; else suggestions.add("Include special characters (e.g. !@#$%^&*).");

        String lc = password.toLowerCase();
        if (lc.contains("password") || lc.contains("123456") || lc.contains("qwerty")) {
            score -= 20;
            suggestions.add("Avoid common patterns like 'password', '123456', or 'qwerty'.");
        }
        if (isRepeatingChars(password)) {
            score -= 10;
            suggestions.add("Avoid repeating the same character many times (e.g. 'aaaa').");
        }

        score = Math.max(0, Math.min(100, score));

        StrengthLevel level;
        if (score < 20) level = StrengthLevel.VERY_WEAK;
        else if (score < 40) level = StrengthLevel.WEAK;
        else if (score < 60) level = StrengthLevel.FAIR;
        else if (score < 80) level = StrengthLevel.STRONG;
        else level = StrengthLevel.VERY_STRONG;

        if (suggestions.isEmpty()) {
            suggestions.add("Great password! Remember to use a unique password for every account.");
        }

        return new StrengthResult(level, score, suggestions, disclaimer());
    }

    private boolean isRepeatingChars(String s) {
        if (s.length() < 4) return false;
        for (int i = 0; i <= s.length() - 4; i++) {
            char c = s.charAt(i);
            if (c == s.charAt(i + 1) && c == s.charAt(i + 2) && c == s.charAt(i + 3)) return true;
        }
        return false;
    }

    private String disclaimer() {
        return "This is an educational strength estimate only. It does not determine whether "
                + "your password has been leaked or compromised in a data breach.";
    }
}

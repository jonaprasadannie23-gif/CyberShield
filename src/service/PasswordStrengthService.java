package service;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates password strength locally.
 *
 * IMPORTANT NOTICE (displayed to the user):
 * This is an educational strength estimator only.  It does NOT check whether
 * a password has appeared in data breaches, nor does it guarantee security.
 * Passwords are never saved, logged, transmitted, or stored anywhere.
 */
public class PasswordStrengthService {

    public enum StrengthLevel {
        VERY_WEAK("Very Weak", "#e74c3c"),
        WEAK("Weak", "#e67e22"),
        FAIR("Fair", "#f39c12"),
        STRONG("Strong", "#2ecc71"),
        VERY_STRONG("Very Strong", "#27ae60");

        private final String label;
        private final String color;

        StrengthLevel(String label, String color) {
            this.label = label;
            this.color = color;
        }

        public String getLabel() { return label; }
        public String getColor() { return color; }
    }

    public static final class StrengthResult {
        private final StrengthLevel level;
        private final int score;
        private final java.util.List<String> suggestions;
        private final String disclaimer;

        public StrengthResult(StrengthLevel level, int score,
                              java.util.List<String> suggestions, String disclaimer) {
            this.level       = level;
            this.score       = score;
            this.suggestions = suggestions;
            this.disclaimer  = disclaimer;
        }

        public StrengthLevel level()                    { return level; }
        public int score()                               { return score; }
        public java.util.List<String> suggestions()      { return suggestions; }
        public String disclaimer()                       { return disclaimer; }
    }

    /**
     * Evaluates the password and returns a StrengthResult.
     * The input password is consumed in-memory only; nothing is persisted.
     *
     * @param password  the candidate password (will NOT be stored)
     * @return          StrengthResult with level, score and improvement suggestions
     */
    public StrengthResult evaluate(String password) {
        List<String> suggestions = new ArrayList<>();
        int score = 0;

        if (password == null || password.isEmpty()) {
            suggestions.add("Enter a password to evaluate.");
            return new StrengthResult(StrengthLevel.VERY_WEAK, 0, suggestions, disclaimer());
        }

        // --- Length ---
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

        // --- Character variety ---
        boolean hasUpper   = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower   = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit   = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c ->
                "!@#$%^&*()_+-=[]{}|;':\",./<>?".indexOf(c) >= 0);

        if (hasUpper)   score += 15; else suggestions.add("Add uppercase letters (A–Z).");
        if (hasLower)   score += 15; else suggestions.add("Add lowercase letters (a–z).");
        if (hasDigit)   score += 15; else suggestions.add("Include numbers (0–9).");
        if (hasSpecial) score += 20; else suggestions.add("Include special characters (e.g. !@#$%^&*).");

        // --- Penalise common patterns ---
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
        if (score < 20)      level = StrengthLevel.VERY_WEAK;
        else if (score < 40) level = StrengthLevel.WEAK;
        else if (score < 60) level = StrengthLevel.FAIR;
        else if (score < 80) level = StrengthLevel.STRONG;
        else                  level = StrengthLevel.VERY_STRONG;

        if (suggestions.isEmpty()) {
            suggestions.add("Great password! Remember to use a unique password for every account.");
        }

        return new StrengthResult(level, score, suggestions, disclaimer());
    }

    // --- Helpers ---

    private boolean isRepeatingChars(String s) {
        if (s.length() < 4) return false;
        for (int i = 0; i <= s.length() - 4; i++) {
            char c = s.charAt(i);
            if (c == s.charAt(i + 1) && c == s.charAt(i + 2) && c == s.charAt(i + 3)) return true;
        }
        return false;
    }

    private String disclaimer() {
        return "⚠ This is an educational strength estimate only. It does not determine whether "
             + "your password has been leaked or compromised in a data breach.";
    }
}

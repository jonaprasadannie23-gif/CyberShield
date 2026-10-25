package util;

import java.util.regex.Pattern;

/**
 * Centralised input validation utilities used across UI and service layers.
 */
public final class InputValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_]{3,20}$");

    private InputValidator() {}

    /** Returns true if the string is non-null and not blank. */
    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    /** Returns true if the email address looks structurally valid. */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Returns true if the username is 3–20 characters, alphanumeric + underscore.
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /** Returns true if the password meets the minimum 8-character requirement. */
    public static boolean isPasswordLongEnough(String password) {
        return password != null && password.length() >= 8;
    }

    /**
     * Sanitises a URL string: trims whitespace and strips any embedded
     * control characters.  The URL is NEVER opened or resolved.
     */
    public static String sanitiseUrl(String url) {
        if (url == null) return "";
        return url.trim().replaceAll("[\\x00-\\x1F\\x7F]", "");
    }

    /** Truncates a string to maxLen characters, appending "…" if needed. */
    public static String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "…";
    }
}

package com.cybershield.util;

import java.util.regex.Pattern;

public final class InputValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_]{3,20}$");

    private InputValidator() {}

    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    public static boolean isPasswordLongEnough(String password) {
        return password != null && password.length() >= 8;
    }

    public static String sanitiseUrl(String url) {
        if (url == null) return "";
        return url.trim().replaceAll("[\\x00-\\x1F\\x7F]", "");
    }
}

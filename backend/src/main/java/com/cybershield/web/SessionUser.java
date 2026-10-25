package com.cybershield.web;

import com.cybershield.exception.UnauthorizedException;
import jakarta.servlet.http.HttpSession;

public final class SessionUser {

    public static final String USER_ID_ATTR = "userId";

    private SessionUser() {}

    public static String requireUserId(HttpSession session) {
        Object userId = session.getAttribute(USER_ID_ATTR);
        if (userId == null || userId.toString().isBlank()) {
            throw new UnauthorizedException("Please sign in to continue.");
        }
        return userId.toString();
    }
}

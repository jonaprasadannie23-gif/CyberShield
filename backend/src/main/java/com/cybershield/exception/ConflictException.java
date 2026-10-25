package com.cybershield.exception;

/**
 * Thrown when a resource already exists (e.g. duplicate username or email).
 * Maps to HTTP 409 Conflict via GlobalExceptionHandler.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}

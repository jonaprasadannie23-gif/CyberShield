package com.cybershield.service;

import com.cybershield.exception.ConflictException;
import com.cybershield.exception.UnauthorizedException;
import com.cybershield.model.User;
import com.cybershield.repository.UserRepository;
import com.cybershield.util.IDGenerator;
import com.cybershield.util.InputValidator;
import com.cybershield.util.PasswordHasher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user. fullName is optional and may be null or blank.
     * Throws IllegalArgumentException (400) for validation failures.
     * Throws ConflictException (409) for duplicate username or email.
     * Passwords are hashed with SHA-256 + random salt and never stored in plain text.
     */
    public User register(String username, String email, String fullName, String plainPassword) {
        // --- Required field validation ---
        if (!InputValidator.isNotBlank(username))
            throw new IllegalArgumentException("Username is required.");
        if (!InputValidator.isValidUsername(username))
            throw new IllegalArgumentException(
                    "Username must be 3–20 characters using only letters, digits, or underscores.");
        if (!InputValidator.isNotBlank(email))
            throw new IllegalArgumentException("Email address is required.");
        if (!InputValidator.isValidEmail(email))
            throw new IllegalArgumentException("Please enter a valid email address.");
        if (!InputValidator.isPasswordLongEnough(plainPassword))
            throw new IllegalArgumentException("Password must be at least 8 characters.");

        // --- Uniqueness checks (409 Conflict, not 400) ---
        if (userRepository.existsByUsername(username.trim()))
            throw new ConflictException("Username '" + username.trim() + "' is already taken.");
        if (userRepository.existsByEmail(email.trim()))
            throw new ConflictException("An account with that email address already exists.");

        // --- Hash password; never store plain text ---
        String hash = PasswordHasher.hash(plainPassword);

        // fullName is optional: store trimmed value, or null if blank
        String storedFullName = (fullName != null && !fullName.isBlank())
                ? fullName.trim() : null;

        User user = new User(
                IDGenerator.generate(),
                username.trim(),
                email.trim().toLowerCase(),
                hash,
                storedFullName);
        userRepository.save(user);
        return user;
    }

    public User login(String username, String plainPassword) {
        if (!InputValidator.isNotBlank(username) || !InputValidator.isNotBlank(plainPassword))
            throw new UnauthorizedException("Username and password are required.");

        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password."));

        if (!PasswordHasher.verify(plainPassword, user.getPasswordHash()))
            throw new UnauthorizedException("Invalid username or password.");

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    public User requireUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException(
                        "Session is invalid. Please sign in again."));
    }
}

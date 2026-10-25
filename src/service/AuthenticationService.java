package service;

import model.User;
import repository.UserRepository;
import util.IDGenerator;
import util.InputValidator;
import util.PasswordHasher;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Handles user registration and login logic.
 * Business rules live here; the UI only calls this service.
 */
public class AuthenticationService {

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // -----------------------------------------------------------------------
    // Registration
    // -----------------------------------------------------------------------

    /**
     * Registers a new user after validating all inputs.
     *
     * @throws IllegalArgumentException with a user-friendly message on any violation
     */
    public User register(String username, String email, String fullName, String plainPassword)
            throws IllegalArgumentException {

        // Validate inputs
        if (!InputValidator.isNotBlank(username))
            throw new IllegalArgumentException("Username is required.");
        if (!InputValidator.isValidUsername(username))
            throw new IllegalArgumentException("Username must be 3–20 characters, letters, digits or underscore.");
        if (!InputValidator.isNotBlank(email))
            throw new IllegalArgumentException("Email address is required.");
        if (!InputValidator.isValidEmail(email))
            throw new IllegalArgumentException("Please enter a valid email address.");
        if (!InputValidator.isNotBlank(fullName))
            throw new IllegalArgumentException("Full name is required.");
        if (!InputValidator.isPasswordLongEnough(plainPassword))
            throw new IllegalArgumentException("Password must be at least 8 characters.");

        // Uniqueness checks
        if (userRepository.existsByUsername(username))
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        if (userRepository.existsByEmail(email))
            throw new IllegalArgumentException("An account with that email address already exists.");

        // Hash password – never store plain text
        String hash = PasswordHasher.hash(plainPassword);

        User user = new User(IDGenerator.generate(), username.trim(),
                             email.trim().toLowerCase(), hash, fullName.trim());
        userRepository.save(user);
        return user;
    }

    // -----------------------------------------------------------------------
    // Login
    // -----------------------------------------------------------------------

    /**
     * Authenticates a user by username and plain-text password.
     *
     * @return the authenticated User
     * @throws IllegalArgumentException with a user-friendly message on failure
     */
    public User login(String username, String plainPassword)
            throws IllegalArgumentException {

        if (!InputValidator.isNotBlank(username) || !InputValidator.isNotBlank(plainPassword))
            throw new IllegalArgumentException("Username and password are required.");

        Optional<User> opt = userRepository.findByUsername(username.trim());
        if (opt.isEmpty())
            throw new IllegalArgumentException("Invalid username or password.");

        User user = opt.get();
        if (!PasswordHasher.verify(plainPassword, user.getPasswordHash()))
            throw new IllegalArgumentException("Invalid username or password.");

        // Record last login time
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return user;
    }
}

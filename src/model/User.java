package model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a registered application user.
 *
 * OOP – Encapsulation: all fields are private; access is via validated
 * getters/setters. The password field stores a salted hash, never plain text.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- private fields (Encapsulation) ---
    private String userId;
    private String username;
    private String email;
    private String passwordHash;   // bcrypt-style SHA-256 + salt hash (see PasswordHasher)
    private String fullName;
    private LocalDateTime registeredAt;
    private LocalDateTime lastLoginAt;

    // --- Constructors (overloading) ---

    public User(String userId, String username, String email,
                String passwordHash, String fullName) {
        this.userId       = userId;
        this.username     = username;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.fullName     = fullName;
        this.registeredAt = LocalDateTime.now();
    }

    // Minimal constructor for deserialization compatibility
    public User() {}

    // --- Getters ---

    public String getUserId()       { return userId; }
    public String getUsername()     { return username; }
    public String getEmail()        { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName()     { return fullName; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public LocalDateTime getLastLoginAt()  { return lastLoginAt; }

    // --- Setters with basic validation ---

    public void setEmail(String email) {
        if (email != null && email.contains("@")) {
            this.email = email;
        }
    }

    public void setFullName(String fullName) {
        if (fullName != null && !fullName.isBlank()) {
            this.fullName = fullName;
        }
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash != null && !passwordHash.isBlank()) {
            this.passwordHash = passwordHash;
        }
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    @Override
    public String toString() {
        return "User{userId='" + userId + "', username='" + username +
               "', email='" + email + "'}";
    }
}

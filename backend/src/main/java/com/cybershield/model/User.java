package com.cybershield.model;

import java.time.LocalDateTime;

/**
 * Registered application user.
 * OOP – Encapsulation: all fields private; passwordHash excluded from JSON responses.
 */
public class User {

    private String userId;
    private String username;
    private String email;

    // Stored in JSON data files; never returned by REST DTOs.
    private String passwordHash;

    private String fullName;
    private LocalDateTime registeredAt;
    private LocalDateTime lastLoginAt;

    // --- Constructors ---

    public User() {}

    public User(String userId, String username, String email,
                String passwordHash, String fullName) {
        this.userId       = userId;
        this.username     = username;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.fullName     = fullName;
        this.registeredAt = LocalDateTime.now();
    }

    // --- Getters ---

    public String getUserId()       { return userId; }
    public String getUsername()     { return username; }
    public String getEmail()        { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName()     { return fullName; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public LocalDateTime getLastLoginAt()  { return lastLoginAt; }

    // --- Setters ---

    public void setUserId(String userId)           { this.userId = userId; }
    public void setUsername(String username)       { this.username = username; }
    public void setEmail(String email)             { if (email != null && email.contains("@")) this.email = email; }
    public void setPasswordHash(String h)          { if (h != null && !h.isBlank()) this.passwordHash = h; }
    public void setFullName(String fullName)       { this.fullName = fullName; }  // null = not provided
    public void setRegisteredAt(LocalDateTime t)   { this.registeredAt = t; }
    public void setLastLoginAt(LocalDateTime t)    { this.lastLoginAt = t; }

    @Override
    public String toString() {
        return "User{userId='" + userId + "', username='" + username + "'}";
    }
}

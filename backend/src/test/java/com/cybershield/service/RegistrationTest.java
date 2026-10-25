package com.cybershield.service;

import com.cybershield.exception.ConflictException;
import com.cybershield.model.User;
import com.cybershield.repository.UserRepository;
import com.cybershield.storage.JsonFileStorage;
import com.cybershield.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuthenticationService registration rules.
 * Uses an isolated temp directory so tests don't touch the main data folder.
 */
class RegistrationTest {

    private AuthenticationService authService;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDir = Files.createTempDirectory("cs-test-");
        JsonFileStorage storage = new JsonFileStorage(tempDir.toString(),
                new com.fasterxml.jackson.databind.ObjectMapper()
                        .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()));
        UserRepository repo = new UserRepository(storage);
        authService = new AuthenticationService(repo);
    }

    // ── Successful registration ──────────────────────────────────────────────

    @Test
    void successfulRegistration_returnsUserWithoutPasswordHash() {
        User user = authService.register("alice", "alice@example.com", "Alice Smith", "Secure1!");

        assertNotNull(user.getUserId());
        assertEquals("alice", user.getUsername());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("Alice Smith", user.getFullName());
        assertNotNull(user.getRegisteredAt());
        // passwordHash must never be null (it's hashed, not plain)
        assertNotNull(user.getPasswordHash());
        // but it must NOT equal the plain password
        assertNotEquals("Secure1!", user.getPasswordHash());
    }

    @Test
    void successfulRegistration_withOptionalFullNameBlank() {
        // fullName is optional – blank should be stored as null
        User user = authService.register("bob", "bob@example.com", "", "Secure1!");
        assertNotNull(user.getUserId());
        // fullName stored as null when blank
        assertNull(user.getFullName());
    }

    @Test
    void successfulRegistration_withOptionalFullNameNull() {
        User user = authService.register("carol", "carol@example.com", null, "Secure1!");
        assertNotNull(user.getUserId());
        assertNull(user.getFullName());
    }

    @Test
    void successfulRegistration_emailNormalisedToLowercase() {
        User user = authService.register("dave", "Dave@Example.COM", "Dave", "Secure1!");
        assertEquals("dave@example.com", user.getEmail());
    }

    @Test
    void successfulRegistration_usernameTrimmed() {
        User user = authService.register("  eve  ", "eve@example.com", "Eve", "Secure1!");
        assertEquals("eve", user.getUsername());
    }

    // ── Password hashing ─────────────────────────────────────────────────────

    @Test
    void passwordHash_verifyRoundTrip() {
        User user = authService.register("frank", "frank@example.com", "Frank", "MyPass99!");
        assertTrue(PasswordHasher.verify("MyPass99!", user.getPasswordHash()),
                "Stored hash must verify against the original password");
        assertFalse(PasswordHasher.verify("WrongPassword", user.getPasswordHash()),
                "Wrong password must not verify");
    }

    @Test
    void passwordHash_differentSaltsForSamePassword() {
        User u1 = authService.register("grace", "grace@example.com", "Grace", "SamePass1!");
        User u2 = authService.register("heidi", "heidi@example.com", "Heidi", "SamePass1!");
        assertNotEquals(u1.getPasswordHash(), u2.getPasswordHash(),
                "Same password must produce different hashes due to random salt");
    }

    // ── Username validation (400) ─────────────────────────────────────────────

    @Test
    void register_blankUsername_throws400() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register("", "x@x.com", "X", "password1"));
        assertTrue(ex.getMessage().toLowerCase().contains("username"));
    }

    @Test
    void register_usernameTooShort_throws400() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register("ab", "x@x.com", "X", "password1"));
        assertTrue(ex.getMessage().toLowerCase().contains("username"));
    }

    @Test
    void register_usernameTooLong_throws400() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register("a".repeat(21), "x@x.com", "X", "password1"));
        assertTrue(ex.getMessage().toLowerCase().contains("username"));
    }

    @Test
    void register_usernameWithInvalidChars_throws400() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register("user name!", "x@x.com", "X", "password1"));
        assertTrue(ex.getMessage().toLowerCase().contains("username"));
    }

    // ── Email validation (400) ────────────────────────────────────────────────

    @Test
    void register_blankEmail_throws400() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register("ivan", "", "Ivan", "password1"));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void register_invalidEmailFormat_throws400() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register("judy", "not-an-email", "Judy", "password1"));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }

    // ── Password validation (400) ─────────────────────────────────────────────

    @Test
    void register_passwordTooShort_throws400() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register("karl", "karl@x.com", "Karl", "short"));
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
        assertTrue(ex.getMessage().contains("8"));
    }

    @Test
    void register_passwordExactly8Chars_succeeds() {
        User user = authService.register("lena", "lena@x.com", "Lena", "12345678");
        assertNotNull(user.getUserId());
    }

    // ── Duplicate detection (409) ─────────────────────────────────────────────

    @Test
    void register_duplicateUsername_throws409() {
        authService.register("mike", "mike@example.com", "Mike", "password1");

        ConflictException ex = assertThrows(ConflictException.class,
                () -> authService.register("mike", "mike2@example.com", "Mike2", "password1"));
        assertTrue(ex.getMessage().toLowerCase().contains("username") ||
                   ex.getMessage().toLowerCase().contains("already"));
    }

    @Test
    void register_duplicateUsername_caseInsensitive_throws409() {
        authService.register("nina", "nina@example.com", "Nina", "password1");

        ConflictException ex = assertThrows(ConflictException.class,
                () -> authService.register("NINA", "nina2@example.com", "Nina2", "password1"));
        assertTrue(ex.getMessage().toLowerCase().contains("taken") ||
                   ex.getMessage().toLowerCase().contains("already"));
    }

    @Test
    void register_duplicateEmail_throws409() {
        authService.register("oscar", "shared@example.com", "Oscar", "password1");

        ConflictException ex = assertThrows(ConflictException.class,
                () -> authService.register("pete", "shared@example.com", "Pete", "password1"));
        assertTrue(ex.getMessage().toLowerCase().contains("email") ||
                   ex.getMessage().toLowerCase().contains("already"));
    }

    @Test
    void register_duplicateEmail_caseInsensitive_throws409() {
        authService.register("quinn", "quinn@example.com", "Quinn", "password1");

        ConflictException ex = assertThrows(ConflictException.class,
                () -> authService.register("rose", "QUINN@EXAMPLE.COM", "Rose", "password1"));
        assertTrue(ex.getMessage().toLowerCase().contains("email") ||
                   ex.getMessage().toLowerCase().contains("already"));
    }

    // ── Login after registration ──────────────────────────────────────────────

    @Test
    void loginAfterRegistration_succeeds() {
        authService.register("sam", "sam@example.com", "Sam", "MyPass99!");
        User logged = authService.login("sam", "MyPass99!");
        assertEquals("sam", logged.getUsername());
        assertNotNull(logged.getLastLoginAt());
    }

    @Test
    void loginAfterRegistration_wrongPassword_fails() {
        authService.register("tina", "tina@example.com", "Tina", "MyPass99!");
        assertThrows(com.cybershield.exception.UnauthorizedException.class,
                () -> authService.login("tina", "wrongpass"));
    }
}

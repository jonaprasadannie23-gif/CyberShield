package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Provides password hashing using SHA-256 with a random salt.
 * Passwords are NEVER stored in plain text.
 *
 * Format of stored value: BASE64(salt) + ":" + BASE64(hash)
 *
 * NOTE: For a production system, use bcrypt or Argon2.  SHA-256+salt is
 * used here to avoid external dependencies while still preventing plain-text
 * storage – appropriate for a college OOP project.
 */
public final class PasswordHasher {

    private static final int SALT_BYTES = 16;
    private static final String ALGORITHM = "SHA-256";

    private PasswordHasher() {}   // utility class – no instantiation

    /**
     * Hashes a plain-text password with a freshly generated random salt.
     * @param plainPassword the raw password entered by the user
     * @return stored string in format "salt:hash" (both Base64 encoded)
     */
    public static String hash(String plainPassword) {
        byte[] salt = generateSalt();
        byte[] hash = computeHash(plainPassword, salt);
        return Base64.getEncoder().encodeToString(salt)
               + ":"
               + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifies a plain-text password against a previously stored hash.
     * @param plainPassword  password candidate
     * @param storedHash     value returned by {@link #hash(String)}
     * @return true if the password matches
     */
    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;
        String[] parts = storedHash.split(":");
        if (parts.length != 2) return false;
        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);
            byte[] actual   = computeHash(plainPassword, salt);
            // Constant-time comparison to prevent timing attacks
            if (actual.length != expected.length) return false;
            int diff = 0;
            for (int i = 0; i < actual.length; i++) {
                diff |= actual[i] ^ expected[i];
            }
            return diff == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // --- helpers ---

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        return salt;
    }

    private static byte[] computeHash(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            return md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}

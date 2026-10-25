package util;

import java.util.UUID;

/**
 * Generates unique identifiers for domain objects.
 */
public final class IDGenerator {

    private IDGenerator() {}

    /** Generates a compact, URL-safe unique ID string. */
    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}

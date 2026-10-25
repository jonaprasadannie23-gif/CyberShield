package com.cybershield.util;

import java.util.UUID;

public final class IDGenerator {

    private IDGenerator() {}

    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}

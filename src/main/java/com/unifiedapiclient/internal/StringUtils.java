package com.unifiedapiclient.internal;

import java.util.Locale;
import java.util.Objects;

/**
 * Utility class for string operations.
 */
public final class StringUtils {
    private static final String ENV_KEY_SUFFIX = "_API_BASE_URL";

    private StringUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(name, "name cannot be null");
        if (!hasText(value)) {
            throw new IllegalArgumentException("%s cannot be null or blank".formatted(name));
        }
        return value;
    }

    public static String toBaseUrlEnvKey(String serviceName) {
        String normalized = requireNonBlank(serviceName, "serviceName");
        return normalized.toUpperCase(Locale.ROOT)
                .replace("-", "_")
                + ENV_KEY_SUFFIX;
    }
}


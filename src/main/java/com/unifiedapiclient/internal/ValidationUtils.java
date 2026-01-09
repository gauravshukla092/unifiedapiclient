package com.unifiedapiclient.internal;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

/**
 * Utility class for validation operations.
 */
final class ValidationUtils {
    private ValidationUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    static void validateBaseUrl(String baseUrl) {
        String value = StringUtils.requireNonBlank(baseUrl, "baseUrl");
        URI parsed;
        try {
            parsed = URI.create(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("baseUrl must be a valid URI: %s".formatted(baseUrl), e);
        }
        if (parsed.getScheme() == null || parsed.getHost() == null) {
            throw new IllegalArgumentException("baseUrl must include scheme and host: %s".formatted(baseUrl));
        }
    }

    static int toTimeoutMs(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout cannot be null");
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        long timeoutMs = timeout.toMillis();
        if (timeoutMs > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("timeout is too large: %d ms (max: %d)".formatted(timeoutMs, Integer.MAX_VALUE));
        }
        return (int) timeoutMs;
    }

    static int requirePositiveTimeoutMs(int timeoutMs, String name) {
        Objects.requireNonNull(name, "name cannot be null");
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("%s must be positive, but was: %d".formatted(name, timeoutMs));
        }
        return timeoutMs;
    }
}


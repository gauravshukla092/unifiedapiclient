package com.unifiedapiclient;

/**
 * Entry point for the Unified API Client library.
 */
public class UnifiedApiClient {
    private final java.util.function.Supplier<String> versionSupplier;

    public UnifiedApiClient() {
        this(() -> "0.1.0");
    }

    public UnifiedApiClient(java.util.function.Supplier<String> versionSupplier) {
        this.versionSupplier = java.util.Objects.requireNonNull(versionSupplier, "versionSupplier");
    }

    public String version() {
        return versionSupplier.get();
    }

    public String version(java.util.function.Function<String, String> mapper) {
        return java.util.Objects.requireNonNull(mapper, "mapper").apply(version());
    }
}

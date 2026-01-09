package com.unifiedapiclient.internal;

import com.unifiedapiclient.Authorization;
import com.unifiedapiclient.ConfigSource;
import com.unifiedapiclient.UnifiedApiClient;
import com.unifiedapiclient.UnifiedApiClientBuilder;

import java.time.Duration;
import java.util.Map;

/**
 * Resolves and builds complete client configuration from a builder.
 */
public final class ClientConfiguration {
    private ClientConfiguration() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static ResolvedConfiguration resolve(UnifiedApiClientBuilder builder) {
        // Validate that either serviceName or baseUrl is provided
        if (!StringUtils.hasText(builder.baseUrl) && !StringUtils.hasText(builder.serviceName)) {
            throw new IllegalArgumentException("serviceName is required when baseUrl is not set");
        }

        // Resolve service name
        String serviceName = resolveServiceName(builder.serviceName);

        // Resolve config source
        ConfigSource configSource = builder.configSource != null
                ? builder.configSource
                : ConfigManager.getInstance();

        // Resolve base URL
        String baseUrl = resolveBaseUrl(builder.baseUrl, builder.serviceName, configSource);
        ValidationUtils.validateBaseUrl(baseUrl);

        // Resolve timeouts
        TimeoutConfig timeouts = resolveTimeouts(builder, configSource);

        // Resolve other settings
        boolean relaxedSsl = resolveRelaxedSsl(builder.relaxedSsl, configSource);
        String userAgent = resolveUserAgent(builder.userAgent, configSource);
        int maxLogEndpointLen = resolveMaxLogEndpointLen(builder.maxLogEndpointLen, configSource);
        Map<String, String> customHeaders = resolveCustomHeaders(builder.customHeaders);
        boolean loggingEnabled = resolveLoggingEnabled(builder.loggingEnabled);
        Authorization authorization = resolveAuthorization(builder, serviceName, configSource);
        java.util.function.Supplier<String> versionSupplier = resolveVersionSupplier(builder.versionSupplier);

        return new ResolvedConfiguration(
                serviceName,
                baseUrl,
                timeouts.connectTimeoutMs(),
                timeouts.readTimeoutMs(),
                relaxedSsl,
                userAgent,
                maxLogEndpointLen,
                customHeaders,
                loggingEnabled,
                authorization,
                versionSupplier
        );
    }

    private static String resolveServiceName(String serviceName) {
        return StringUtils.hasText(serviceName) ? serviceName : UnifiedApiClient.DEFAULT_SERVICE_NAME;
    }

    private static String resolveBaseUrl(String baseUrl, String serviceName, ConfigSource configSource) {
        if (StringUtils.hasText(baseUrl)) {
            return baseUrl;
        }
        return BaseUrlResolver.resolve(serviceName, configSource);
    }

    private static TimeoutConfig resolveTimeouts(UnifiedApiClientBuilder builder, ConfigSource configSource) {
        Duration timeout = builder.timeout != null
                ? builder.timeout
                : Duration.ofMillis(ConfigResolver.getApiSettingLong(
                        configSource, "timeout_ms", UnifiedApiClient.DEFAULT_TIMEOUT.toMillis()));

        int timeoutMs = ValidationUtils.toTimeoutMs(timeout);

        int connectTimeoutMs = builder.connectTimeoutMs != null
                ? ValidationUtils.requirePositiveTimeoutMs(builder.connectTimeoutMs, "connectTimeoutMs")
                : timeoutMs;

        int readTimeoutMs = builder.readTimeoutMs != null
                ? ValidationUtils.requirePositiveTimeoutMs(builder.readTimeoutMs, "readTimeoutMs")
                : timeoutMs;

        return new TimeoutConfig(connectTimeoutMs, readTimeoutMs);
    }

    private static boolean resolveRelaxedSsl(Boolean relaxedSsl, ConfigSource configSource) {
        return relaxedSsl != null
                ? relaxedSsl
                : ConfigResolver.getApiSettingBoolean(configSource, "ssl_relaxed", UnifiedApiClient.DEFAULT_RELAXED_SSL);
    }

    private static String resolveUserAgent(String userAgent, ConfigSource configSource) {
        return StringUtils.hasText(userAgent)
                ? userAgent
                : ConfigResolver.getApiSettingString(configSource, "user_agent", UnifiedApiClient.DEFAULT_USER_AGENT);
    }

    private static int resolveMaxLogEndpointLen(Integer maxLogEndpointLen, ConfigSource configSource) {
        int resolved = maxLogEndpointLen != null
                ? maxLogEndpointLen
                : ConfigResolver.getApiSettingInt(configSource, "log_endpoint_max_len", UnifiedApiClient.DEFAULT_MAX_LOG_ENDPOINT_LEN);

        if (resolved <= 0) {
            throw new IllegalArgumentException("maxLogEndpointLen must be positive");
        }
        return resolved;
    }

    private static Map<String, String> resolveCustomHeaders(Map<String, String> customHeaders) {
        return customHeaders.isEmpty() ? null : Map.copyOf(customHeaders);
    }

    private static boolean resolveLoggingEnabled(Boolean loggingEnabled) {
        return loggingEnabled == null || loggingEnabled;
    }

    private static Authorization resolveAuthorization(UnifiedApiClientBuilder builder, String serviceName, ConfigSource configSource) {
        if (builder.authorizationDisabled) {
            return null;
        }
        return builder.authorization != null
                ? builder.authorization
                : AuthorizationResolver.resolve(serviceName, configSource);
    }

    private static java.util.function.Supplier<String> resolveVersionSupplier(java.util.function.Supplier<String> versionSupplier) {
        return versionSupplier != null ? versionSupplier : () -> UnifiedApiClient.DEFAULT_VERSION;
    }

    private record TimeoutConfig(int connectTimeoutMs, int readTimeoutMs) {}

    public record ResolvedConfiguration(
            String serviceName,
            String baseUrl,
            int connectTimeoutMs,
            int readTimeoutMs,
            boolean relaxedSsl,
            String userAgent,
            int maxLogEndpointLen,
            Map<String, String> customHeaders,
            boolean loggingEnabled,
            Authorization authorization,
            java.util.function.Supplier<String> versionSupplier
    ) {}
}


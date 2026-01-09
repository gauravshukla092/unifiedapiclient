package com.unifiedapiclient.internal;

import com.unifiedapiclient.ConfigSource;

final class BaseUrlResolver {
    private static final String CONFIG_PREFIX = "domain_apis.";
    private static final String BASE_URL_SUFFIX = ".base_url";

    private BaseUrlResolver() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    static String resolve(String serviceName, ConfigSource configSource) {
        String resolvedServiceName = StringUtils.requireNonBlank(serviceName, "serviceName");
        String configKey = CONFIG_PREFIX + resolvedServiceName + BASE_URL_SUFFIX;
        String envVarName = StringUtils.toBaseUrlEnvKey(resolvedServiceName);

        // Priority 1: Configuration source
        if (configSource != null) {
            String baseUrl = configSource.getOptional(configKey);
            if (StringUtils.hasText(baseUrl)) {
                return baseUrl;
            }
        }

        // Priority 2: Environment variable
        String baseUrl = System.getenv(envVarName);
        if (StringUtils.hasText(baseUrl)) {
            return baseUrl;
        }

        // Priority 3: System property
        baseUrl = System.getProperty(envVarName);
        if (StringUtils.hasText(baseUrl)) {
            return baseUrl;
        }

        // No valid base URL found - throw exception with helpful message
        throw new IllegalStateException(buildErrorMessage(resolvedServiceName, configKey, envVarName));
    }

    private static String buildErrorMessage(String serviceName, String configKey, String envVarName) {
        return """
                Service '%s' not configured. Provide one of:
                1) Config key: %s
                2) Environment variable: %s
                3) System property (-D): %s
                """.formatted(serviceName, configKey, envVarName, envVarName);
    }
}


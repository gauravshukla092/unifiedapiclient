package com.unifiedapiclient.internal;

import com.unifiedapiclient.Authorization;
import com.unifiedapiclient.ConfigSource;

/**
 * Resolves authorization configuration for services from configuration sources.
 */
final class AuthorizationResolver {
    private static final String CONFIG_PREFIX = "domain_apis.";
    private static final String AUTH_VALUE_SUFFIX = ".auth_value";
    private static final String AUTH_HEADER_SUFFIX = ".auth_header";
    private static final String DEFAULT_HEADER_NAME = "Authorization";

    private AuthorizationResolver() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    static Authorization resolve(String serviceName, ConfigSource configSource) {
        if (configSource == null || !StringUtils.hasText(serviceName)) {
            return null;
        }

        String authValueKey = CONFIG_PREFIX + serviceName + AUTH_VALUE_SUFFIX;
        String headerValue = configSource.getOptional(authValueKey);
        if (!StringUtils.hasText(headerValue)) {
            return null;
        }

        String authHeaderKey = CONFIG_PREFIX + serviceName + AUTH_HEADER_SUFFIX;
        String headerName = configSource.getOptional(authHeaderKey);
        if (!StringUtils.hasText(headerName)) {
            headerName = DEFAULT_HEADER_NAME;
        }

        return new Authorization(headerName, headerValue);
    }
}


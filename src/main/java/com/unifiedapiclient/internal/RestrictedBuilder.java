package com.unifiedapiclient.internal;

import com.unifiedapiclient.Authorization;
import com.unifiedapiclient.ServiceClientBuilder;
import com.unifiedapiclient.UnifiedApiClient;
import com.unifiedapiclient.UnifiedApiClientBuilder;

import java.util.Map;
import java.util.Objects;

/**
 * Restricted builder implementation for safe, hybrid configuration of UnifiedApiClient.
 */
public final class RestrictedBuilder implements ServiceClientBuilder {
    private final UnifiedApiClientBuilder builder;

    public RestrictedBuilder(String serviceName) {
        this.builder = new UnifiedApiClientBuilder().serviceName(serviceName);
    }

    @Override
    public ServiceClientBuilder withHeader(String key, String value) {
        builder.customHeader(key, value);
        return this;
    }

    @Override
    public ServiceClientBuilder withHeaders(Map<String, String> headers) {
        Objects.requireNonNull(headers, "headers cannot be null");
        builder.customHeaders(headers);
        return this;
    }

    @Override
    public ServiceClientBuilder withBaseUrl(String baseUrl) {
        builder.baseUrl(baseUrl);
        return this;
    }

    @Override
    public ServiceClientBuilder withTimeouts(int connectMs, int readMs) {
        builder.connectTimeoutMs = ValidationUtils.requirePositiveTimeoutMs(connectMs, "connectMs");
        builder.readTimeoutMs = ValidationUtils.requirePositiveTimeoutMs(readMs, "readMs");
        return this;
    }

    @Override
    public ServiceClientBuilder withAuthorization(Authorization auth) {
        builder.authorization = Objects.requireNonNull(auth, "auth cannot be null");
        builder.authorizationDisabled = false;
        return this;
    }

    @Override
    public ServiceClientBuilder disableAuthorization() {
        builder.authorization = null;
        builder.authorizationDisabled = true;
        return this;
    }

    @Override
    public ServiceClientBuilder enableLogging(boolean enabled) {
        builder.loggingEnabled = enabled;
        return this;
    }

    @Override
    public UnifiedApiClient build() {
        return builder.build();
    }
}


package com.unifiedapiclient;

import com.unifiedapiclient.internal.StringUtils;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Builder for creating UnifiedApiClient instances with full configuration control.
 */
public final class UnifiedApiClientBuilder {
    // Public for internal package access
    public String serviceName;
    public String baseUrl;
    public Duration timeout;
    public Integer connectTimeoutMs;
    public Integer readTimeoutMs;
    public Boolean relaxedSsl;
    public String userAgent;
    public Integer maxLogEndpointLen;
    public final Map<String, String> customHeaders = new LinkedHashMap<>();
    public ConfigSource configSource;
    public Supplier<String> versionSupplier;
    public Boolean loggingEnabled;
    public Authorization authorization;
    public boolean authorizationDisabled;

    public UnifiedApiClientBuilder() {
        // Public constructor - use UnifiedApiClient.builder() to create
    }

    public UnifiedApiClientBuilder serviceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public UnifiedApiClientBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public UnifiedApiClientBuilder timeout(Duration timeout) {
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        return this;
    }

    public UnifiedApiClientBuilder timeoutMillis(long timeoutMillis) {
        return timeout(Duration.ofMillis(timeoutMillis));
    }

    public UnifiedApiClientBuilder relaxedSsl(boolean relaxedSsl) {
        this.relaxedSsl = relaxedSsl;
        return this;
    }

    public UnifiedApiClientBuilder userAgent(String userAgent) {
        this.userAgent = StringUtils.requireNonBlank(userAgent, "userAgent");
        return this;
    }

    public UnifiedApiClientBuilder maxLogEndpointLen(int maxLogEndpointLen) {
        this.maxLogEndpointLen = maxLogEndpointLen;
        return this;
    }

    public UnifiedApiClientBuilder customHeader(String name, String value) {
        customHeaders.put(StringUtils.requireNonBlank(name, "header name"), Objects.requireNonNull(value, "header value"));
        return this;
    }

    public UnifiedApiClientBuilder customHeaders(Map<String, String> headers) {
        Objects.requireNonNull(headers, "headers");
        headers.forEach((name, value) ->
                customHeaders.put(StringUtils.requireNonBlank(name, "header name"), Objects.requireNonNull(value, "header value"))
        );
        return this;
    }

    /**
     * @param configSource the custom configuration source
     */
    public UnifiedApiClientBuilder configSource(ConfigSource configSource) {
        this.configSource = Objects.requireNonNull(configSource, "configSource");
        return this;
    }

    public UnifiedApiClientBuilder versionSupplier(Supplier<String> versionSupplier) {
        this.versionSupplier = Objects.requireNonNull(versionSupplier, "versionSupplier");
        return this;
    }

    public UnifiedApiClient build() {
        return new UnifiedApiClient(this);
    }
}


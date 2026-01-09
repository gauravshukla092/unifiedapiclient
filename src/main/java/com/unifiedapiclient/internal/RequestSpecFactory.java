package com.unifiedapiclient.internal;

import com.unifiedapiclient.Authorization;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

/**
 * Factory for creating RequestSpecification instances for HTTP requests.
 */
public final class RequestSpecFactory {
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String HTTP_CONNECTION_TIMEOUT = "http.connection.timeout";
    private static final String HTTP_SOCKET_TIMEOUT = "http.socket.timeout";
    private static final String HTTP_CONNECTION_MANAGER_TIMEOUT = "http.connection-manager.timeout";

    private RequestSpecFactory() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static RequestSpecification create(
            String serviceName,
            String baseUrl,
            int connectTimeoutMs,
            int readTimeoutMs,
            boolean relaxedSsl,
            String userAgent,
            int maxLogEndpointLen,
            Map<String, String> customHeaders,
            Authorization authorization,
            boolean loggingEnabled
    ) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .addHeader(USER_AGENT_HEADER, userAgent)
                .setConfig(createHttpClientConfig(connectTimeoutMs, readTimeoutMs));

        configureLogging(builder, serviceName, maxLogEndpointLen, loggingEnabled);
        configureCustomHeaders(builder, customHeaders);
        configureAuthorization(builder, authorization);

        RequestSpecification spec = builder.build();
        return applySslConfiguration(spec, relaxedSsl);
    }

    private static RestAssuredConfig createHttpClientConfig(int connectTimeoutMs, int readTimeoutMs) {
        int connectionManagerTimeoutMs = Math.max(connectTimeoutMs, readTimeoutMs);
        
        return RestAssured.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam(HTTP_CONNECTION_TIMEOUT, connectTimeoutMs)
                        .setParam(HTTP_SOCKET_TIMEOUT, readTimeoutMs)
                        .setParam(HTTP_CONNECTION_MANAGER_TIMEOUT, (long) connectionManagerTimeoutMs)
                );
    }

    private static void configureLogging(
            RequestSpecBuilder builder,
            String serviceName,
            int maxLogEndpointLen,
            boolean loggingEnabled
    ) {
        if (loggingEnabled) {
            builder.addFilter(new LoggingFilter(serviceName, maxLogEndpointLen));
        }
    }

    private static void configureCustomHeaders(RequestSpecBuilder builder, Map<String, String> customHeaders) {
        if (customHeaders != null && !customHeaders.isEmpty()) {
            customHeaders.forEach(builder::addHeader);
        }
    }

    private static void configureAuthorization(RequestSpecBuilder builder, Authorization authorization) {
        if (authorization != null) {
            builder.addHeader(authorization.headerName(), authorization.headerValue());
        }
    }

    private static RequestSpecification applySslConfiguration(RequestSpecification spec, boolean relaxedSsl) {
        return relaxedSsl ? spec.relaxedHTTPSValidation() : spec;
    }
}


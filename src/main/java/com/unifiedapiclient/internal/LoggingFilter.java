package com.unifiedapiclient.internal;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.UUID;

/**
 * Filter for logging HTTP requests and responses with correlation IDs.
 */
final class LoggingFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final int CORRELATION_ID_LENGTH = 8;
    private static final String DEFAULT_ENDPOINT = "/";
    private static final String ELLIPSIS = "...";
    private static final int NANOSECONDS_PER_MILLISECOND = 1_000_000;

    private final String serviceName;
    private final int maxEndpointLen;

    LoggingFilter(String serviceName, int maxEndpointLen) {
        this.serviceName = serviceName;
        this.maxEndpointLen = maxEndpointLen;
    }

    @Override
    public Response filter(
            FilterableRequestSpecification req,
            FilterableResponseSpecification res,
            FilterContext ctx
    ) {
        String correlationId = getOrGenerateCorrelationId(req);
        String endpoint = extractAndTruncateEndpoint(req.getURI(), req.getBaseUri());

        LOGGER.debug("-> [{}] [{}] {} {}", correlationId, serviceName, req.getMethod(), endpoint);

        long startNanos = System.nanoTime();
        Response response = ctx.next(req, res);
        long elapsedMs = (System.nanoTime() - startNanos) / NANOSECONDS_PER_MILLISECOND;

        LOGGER.debug("<- [{}] [{}] {} ({} ms)", correlationId, serviceName, response.statusCode(), elapsedMs);

        return response;
    }

    private String getOrGenerateCorrelationId(FilterableRequestSpecification req) {
        String correlationId = req.getHeaders().getValue(CORRELATION_HEADER);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = generateShortCorrelationId();
            req.header(CORRELATION_HEADER, correlationId);
        }
        return correlationId;
    }

    private static String generateShortCorrelationId() {
        return UUID.randomUUID().toString().substring(0, CORRELATION_ID_LENGTH);
    }

    private String extractAndTruncateEndpoint(String fullUri, String baseUri) {
        String endpoint = extractEndpoint(fullUri, baseUri);
        return truncateIfNeeded(endpoint);
    }

    private String truncateIfNeeded(String endpoint) {
        if (endpoint.length() > maxEndpointLen) {
            return endpoint.substring(0, maxEndpointLen) + ELLIPSIS;
        }
        return endpoint;
    }

    private static String extractEndpoint(String fullUri, String baseUri) {
        if (!StringUtils.hasText(fullUri)) {
            return DEFAULT_ENDPOINT;
        }

        // Try to extract relative path from base URI
        if (StringUtils.hasText(baseUri) && fullUri.startsWith(baseUri)) {
            String endpoint = fullUri.substring(baseUri.length());
            return endpoint.isEmpty() ? DEFAULT_ENDPOINT : endpoint;
        }

        // Parse URI to extract path and query
        try {
            URI parsed = URI.create(fullUri);
            String path = parsed.getRawPath();
            String query = parsed.getRawQuery();

            if (!StringUtils.hasText(path)) {
                return DEFAULT_ENDPOINT;
            }

            return StringUtils.hasText(query) ? path + "?" + query : path;
        } catch (IllegalArgumentException ignored) {
            // If URI parsing fails, return the full URI as fallback
            return fullUri;
        }
    }
}


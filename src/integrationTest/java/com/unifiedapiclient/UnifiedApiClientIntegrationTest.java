package com.unifiedapiclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Integration tests for UnifiedApiClient that make real HTTP requests to external APIs.
 * 
 * <p>These tests are separated from unit tests because they:
 * <ul>
 *   <li>Require network connectivity</li>
 *   <li>May fail due to external API downtime or rate limiting</li>
 *   <li>Should not run in offline/restricted CI environments</li>
 * </ul>
 * 
 * <p>To run these tests, use: {@code ./gradlew integrationTest}
 * 
 * <p><strong>Note:</strong> These tests use the public reqres.in API which is designed
 * for testing purposes. No real API keys are required.
 */
class UnifiedApiClientIntegrationTest {

    /**
     * Integration test: Real API call using reqres.in API.
     * Demonstrates hybrid approach with actual HTTP request.
     * Equivalent to: curl --location 'https://reqres.in/api/users' --header 'x-api-key: reqres_15fbcf8ed2fd4885b0b1a01f20c68d60'
     */
    @Test
    void hybridApproach_RealApiCall_ReqRes() {
        // Hybrid approach: Base URL and some settings from config (if available),
        // but API key and specific overrides set programmatically
        
        UnifiedApiClient client = UnifiedApiClient.builder()
                .serviceName("reqres_api")
                // Base URL set programmatically (could also come from config)
                .baseUrl("https://reqres.in")
                // API key header added programmatically (not from config for security)
                .customHeader("x-api-key", "reqres_15fbcf8ed2fd4885b0b1a01f20c68d60")
                // Timeout could come from config, but we set it explicitly
                .timeout(Duration.ofSeconds(30))
                .relaxedSsl(true)
                .build();

        assertNotNull(client);
        assertEquals("reqres_api", client.getServiceName());
        
        // Make actual API call - equivalent to the curl command
        // GET https://reqres.in/api/users with x-api-key header
        var response = client.get("/api/users");
        
        // Verify the response
        assertNotNull(response);
        assertEquals(200, response.getStatusCode(), "API call should succeed");
        assertNotNull(response.getBody(), "Response should have body");
        
        // Verify response contains expected data structure
        String body = response.getBody().asString();
        assertTrue(body.contains("data") || body.contains("page"), 
                "Response should contain user data");
    }

    /**
     * Integration test: Using in-memory config for base URL, overriding with programmatic API key.
     * This shows how config files could provide base settings while secrets are set programmatically.
     */
    @Test
    void hybridApproach_ConfigBaseUrlWithProgrammaticApiKey() {
        // Simulate config file providing base URL
        Map<String, String> configMap = Map.of(
                "domain_apis.reqres_api.base_url", "https://reqres.in",
                "domain_apis.settings.timeout_ms", "30000"
        );

        // Hybrid: Base URL from config, API key from code (security best practice)
        UnifiedApiClient client = UnifiedApiClient.builder()
                .serviceName("reqres_api")
                .configSource(new ConfigSource.MapConfigSource(configMap))
                // API key set programmatically (not stored in config files)
                .customHeader("x-api-key", "reqres_15fbcf8ed2fd4885b0b1a01f20c68d60")
                .build();

        assertNotNull(client);
        
        // Make API call
        var response = client.get("/api/users");
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }
}


package com.unifiedapiclient;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UnifiedApiClientTest {
    @Test
    void returnsVersion() {
        UnifiedApiClient client = new UnifiedApiClient();

        assertEquals("0.1.0", client.version());
    }

    @Test
    void mapsVersionWithFunction() {
        UnifiedApiClient client = new UnifiedApiClient();

        assertEquals("v0.1.0", client.version(value -> "v" + value));
    }
}

package com.unifiedapiclient;

import java.util.Map;

public interface ServiceClientBuilder {
    ServiceClientBuilder withHeader(String key, String value);
    ServiceClientBuilder withHeaders(Map<String, String> headers);
    ServiceClientBuilder withBaseUrl(String baseUrl);
    ServiceClientBuilder withTimeouts(int connectMs, int readMs);
    ServiceClientBuilder withAuthorization(Authorization auth);
    ServiceClientBuilder disableAuthorization();
    ServiceClientBuilder enableLogging(boolean enabled);
    UnifiedApiClient build();
}

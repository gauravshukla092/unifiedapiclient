# unifiedapiclient

UnifiedApiClient is a reusable Java API integration client that provides a single, consistent way to integrate APIs across all layers of the test pyramid. It reduces boilerplate, standardizes configuration and logging, and enables engineers to use APIs seamlessly in UI, service, and database tests.

## Features

- **Multi-source configuration**: System properties, environment variables, config files (JSON/YAML/Properties)
- **Automatic correlation IDs**: Request tracing with X-Correlation-Id headers
- **Flexible builders**: Simple factory, restricted builder, and full builder patterns
- **RestAssured integration**: Returns RestAssured `Response` objects for rich validation and parsing capabilities
- **Comprehensive logging**: Request/response logging with correlation IDs and timing

## Prerequisites

- Java 21+
- Gradle (or use `./gradlew` once you add the wrapper)

## Dependencies

This library uses [RestAssured](https://restassured.io/) for HTTP request execution. All HTTP methods return RestAssured `Response` objects, allowing you to leverage RestAssured's powerful validation and parsing capabilities:

```java
import io.restassured.response.Response;

Response response = client.get("/users/123");
response.then().statusCode(200);
String name = response.jsonPath().getString("name");
User user = response.as(User.class);
```

## Quick Start

### 1. Simple Factory (Recommended)

The easiest way to create a client is using `create()` with a service name. Configuration is automatically resolved from config files, environment variables, or system properties.

```java
import com.unifiedapiclient.UnifiedApiClient;
import io.restassured.response.Response;

// Create client - configuration resolved automatically
UnifiedApiClient client = UnifiedApiClient.create("payment_service");

// Make HTTP requests
Response response = client.get("/health");
response.then().statusCode(200);
```

### 2. Restricted Builder (Hybrid Approach)

Use `forService()` when you want config defaults but need to override specific settings:

```java
UnifiedApiClient client = UnifiedApiClient.forService("payment_service")
        .withBaseUrl("https://api.example.com")  // Override base URL
        .withTimeouts(1000, 3000)                // Override timeouts
        .withHeader("X-API-Key", "secret")       // Add custom header
        .withAuthorization(Authorization.bearerToken("token123"))
        .build();

Response response = client.get("/users");
```

### 3. Full Builder (Advanced)

For complete control, use `builder()`:

```java
UnifiedApiClient client = UnifiedApiClient.builder()
        .serviceName("payment_service")
        .baseUrl("https://api.example.com")
        .timeout(Duration.ofSeconds(30))
        .relaxedSsl(false)
        .userAgent("custom-agent/1.0")
        .customHeader("X-API-Key", "key123")
        .build();
```

## Configuration

UnifiedApiClient supports multiple configuration sources with priority-based resolution:

1. **Builder overrides** (highest priority)
2. **System properties** (`-Dkey=value`)
3. **Environment variables**
4. **Config files** (JSON/YAML/Properties)
5. **Hardcoded defaults** (lowest priority)

### Base URL Configuration

Base URLs are resolved using the following patterns (in priority order):

1. **Config file key**: `domain_apis.{service}.base_url`
   ```json
   {
     "domain_apis": {
       "payment_service": {
         "base_url": "https://api.example.com"
       }
     }
   }
   ```

2. **Environment variable**: `{SERVICE}_API_BASE_URL`
   ```bash
   export PAYMENT_SERVICE_API_BASE_URL=https://api.example.com
   ```

3. **System property**: `{SERVICE}_API_BASE_URL`
   ```bash
   -DPAYMENT_SERVICE_API_BASE_URL=https://api.example.com
   ```

**Service name transformation**: Service names are converted to uppercase with underscores (e.g., `payment_service` → `PAYMENT_SERVICE`).

### Configuration File Support

UnifiedApiClient automatically detects config files in the project root or classpath:

- `config.properties`, `application.properties`
- `config.yaml`, `config.yml`, `application.yaml`, `application.yml`
- `config.json`, `application.json`

You can also specify a config file explicitly:

```bash
# System property
-Dunifiedapiclient.config.file=/path/to/config.json

# Environment variable
export UNIFIEDAPICLIENT_CONFIG_FILE=/path/to/config.json
```

### Example Config File

```json
{
  "domain_apis": {
    "payment_service": {
      "base_url": "https://api.payment.com",
      "auth_value": "Bearer token123",
      "auth_header": "Authorization"
    },
    "user_service": {
      "base_url": "https://api.users.com"
    }
  },
  "domain_apis": {
    "payment_service": {
      "base_url": "https://api.payment.com",
      "auth_value": "Bearer token123",
      "auth_header": "Authorization"
    },
    "user_service": {
      "base_url": "https://api.users.com"
    },
    "settings": {
      "timeout_ms": 30000,
      "ssl_relaxed": true,
      "user_agent": "unifiedapiclient/1.0",
      "log_endpoint_max_len": 2000
    }
  }
}
```

## HTTP Methods

All HTTP methods return RestAssured `Response` objects:

```java
// GET request
Response response = client.get("/users/123");

// GET with path parameters
Response response = client.get("/users/{id}", 123);
// or
Response response = client.get("/users/{id}", Map.of("id", 123));

// POST request
Map<String, Object> body = Map.of("name", "John", "email", "john@example.com");
Response response = client.post("/users", body);

// PUT request
Response response = client.put("/users/123", updatedUser);

// PATCH request
Response response = client.patch("/users/123", partialUpdate);

// DELETE request
Response response = client.delete("/users/123");

// HEAD request
Response response = client.head("/health");

// OPTIONS request
Response response = client.options("/users");
```

## Advanced Usage

### Custom Configuration Source

For testing or programmatic configuration, use `MapConfigSource`:

```java
Map<String, String> config = Map.of(
    "domain_apis.payment_service.base_url", "https://api.example.com",
    "domain_apis.settings.timeout_ms", "30000"
);

UnifiedApiClient client = UnifiedApiClient.builder()
        .serviceName("payment_service")
        .configSource(new UnifiedApiClient.MapConfigSource(config))
        .build();
```

### Authorization

Configure authorization via config or builder:

```java
// Via config file
{
  "domain_apis": {
    "payment_service": {
      "auth_value": "Bearer token123",
      "auth_header": "Authorization"
    }
  }
}

// Via builder
UnifiedApiClient client = UnifiedApiClient.forService("payment_service")
        .withAuthorization(Authorization.bearerToken("token123"))
        .build();

// Disable authorization
UnifiedApiClient client = UnifiedApiClient.forService("payment_service")
        .disableAuthorization()
        .build();
```

## Build & Test

```bash
# Run unit tests (fast, no network calls)
gradle test

# Run integration tests (requires network connectivity)
gradle integrationTest
```

## Documentation

- **Getting Started Guide**: See `GETTING_STARTED.md` for detailed examples of all public methods
- **Architecture**: See `ARCHITECTURE.md` for system design and component overview

## License

[Add your license information here]

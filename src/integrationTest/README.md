# Integration Tests

This directory contains integration tests that make real HTTP requests to external APIs.

## Why Separate?

These tests are separated from unit tests because they:
- **Require network connectivity** - Will fail in offline environments
- **May be flaky** - External API downtime or rate limiting can cause failures
- **Should not block CI/CD** - Unit tests should run fast and reliably

## Running Integration Tests

Integration tests are **not** run by default. To run them explicitly:

```bash
./gradlew integrationTest
```

Or on Windows:
```bash
gradlew.bat integrationTest
```

## CI/CD Configuration

In your CI/CD pipeline:
- **Unit tests** (`./gradlew test`) - Run always, required for builds
- **Integration tests** (`./gradlew integrationTest`) - Run optionally, skip in restricted networks

## Current Integration Tests

- `UnifiedApiClientIntegrationTest` - Tests real HTTP requests to reqres.in API

These tests verify that the library works correctly with real HTTP endpoints, but they should not be part of the standard test suite.


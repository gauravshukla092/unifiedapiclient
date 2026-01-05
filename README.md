# unifiedapiclient

UnifiedApiClient is a reusable Java API integration client that provides a single, consistent way to integrate APIs across all layers of the test pyramid. It reduces boilerplate, standardizes configuration and logging, and enables engineers to use APIs seamlessly in UI, service, and database tests.

## Prerequisites

- Java 21+
- Gradle (or use `./gradlew` once you add the wrapper)

## Project layout

```
src/main/java/com/unifiedapiclient/UnifiedApiClient.java
src/test/java/com/unifiedapiclient/UnifiedApiClientTest.java
```

## Build & test

```bash
gradle test
```

## Next steps

- Add the Gradle wrapper (`gradle wrapper`) so contributors can use `./gradlew`.
- Extend `UnifiedApiClient` with HTTP adapters, configuration, and logging.

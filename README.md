# HRS - Spec-Driven Development Workshop

This is the companion application for the [**Spec-Driven Development (SDD) Workshop**](https://martinelli.ch/sdd).<br>
It serves as a hands-on project where participants learn to build applications by driving implementation from
specifications.

## Tech Stack

- **Java 25** with **Spring Boot 4.0**
- **Vaadin 25** for the UI (Java-based views)
- **jOOQ** for type-safe SQL and data access
- **Flyway** for database migrations
- **H2** as the database (embedded, no external setup required)
- **Karibu Testing** for server-side UI unit tests
- **Playwright** for browser-based integration tests

## Prerequisites

- **Java 25** (or later)
- **Maven** (or use the included `mvnw` wrapper)

## Running the Application

The application uses an embedded H2 database — no Docker or external database setup required.

```bash
./mvnw spring-boot:run
```

Or run `HrsApplication.main()` directly from your IDE.

## Build and Code Generation

The Maven build uses a two-step pipeline during `generate-sources`:

1. **Flyway** runs database migrations against a file-based H2 database
2. **jOOQ** generates type-safe Java code from the H2 schema

```bash
./mvnw compile
```

## Running Tests

Unit tests (Karibu) are run by **Surefire** with `mvnw test`. Integration tests (Playwright) use the `*IT` suffix
and are run by the **Failsafe** plugin, which is bound to the `integration-test` and `verify` phases.

**Unit tests only:**

```bash
./mvnw test
```

**All tests (unit + integration):**

```bash
./mvnw verify
```

The `verify` phase ensures that the Failsafe plugin picks up all `*IT` classes (e.g. `PlaywrightIT` subclasses)
and reports their results correctly. Always use `verify` instead of `integration-test` directly, so that test
failures are properly detected.

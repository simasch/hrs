# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project context

HRS is the companion app for a **Spec-Driven Development (SDD)** workshop. The intent is that implementation is driven
by the artifacts in `docs/`:

- `docs/vision.md`, `docs/requirements.md` — what the system is for (a small ten-room hotel reservation system) and the
  FR-### catalog.
- `docs/use_cases.puml`, `docs/use_cases/UC-###-*.md` — actor/use-case overview and per-use-case specifications (
  preconditions, main flow, alternates, business rules).
- `docs/entity_model.md` — the ERD and per-entity attribute tables. The Flyway migrations under
  `src/main/resources/db/migration/` are the implementation of this model; keep them in sync.

When asked to implement a use case, read the matching `UC-###-*.md` first — the business rules there (e.g. BR-006 "no
overlap with existing reservations") are the source of truth, not assumptions from the schema alone.

## Tech stack

Java 25 · Spring Boot 4.0 · Vaadin 25 (Java views) · jOOQ (codegen at build time) · Flyway · PostgreSQL 18 ·
Testcontainers · Karibu / Vaadin Browserless for server-side UI unit tests · Playwright + Mopo for browser integration
tests.

## Build pipeline (important)

`generate-sources` chains three plugins in this order — they all share state via Maven properties, so don't reorder
them:

1. **groovy-maven-plugin** starts a PostgreSQL Testcontainer and writes its random JDBC URL into `${db.url}`.
2. **flyway-maven-plugin** migrates `src/main/resources/db/migration/` into that container.
3. **jooq-codegen-maven** generates type-safe code into package `ch.martinelli.edu.hrs.db` from the live schema (uses
   `EqualsAndHashCodeJavaGenerator` from `jooq-utilities`).

Consequence: any time you add or change a Flyway migration, run `./mvnw compile` (or any phase ≥ `generate-sources`) to
regenerate jOOQ classes before relying on them. Docker must be running.

## Running the app

**Always use `TestHrsApplication`, not `HrsApplication`.** The plain app has no datasource; `TestHrsApplication` wires
`TestcontainersConfiguration` (`@ServiceConnection` PostgreSQL container) so Boot has a database.

```bash
./mvnw spring-boot:test-run     # or run TestHrsApplication.main() in the IDE
```

## Tests

Two distinct test tiers; pick the right base class:

- **Server-side unit tests** extend `ch.martinelli.edu.hrs.core.ui.HrsBrowserlessTest` (`SpringBrowserlessTest` +
  `@SpringBootTest` + `TestcontainersConfiguration`, English locale forced). Run by Surefire — class name does **not**
  end in `IT`.
- **Browser integration tests** extend `ch.martinelli.edu.hrs.core.ui.PlaywrightIT` (
  `@SpringBootTest(webEnvironment = RANDOM_PORT)` + Playwright Chromium + Mopo). Run by Failsafe — class name **must**
  end in `IT` (e.g. `SearchAvailableRoomsIT`).

```bash
./mvnw test       # unit tests only (Surefire)
./mvnw verify     # unit + integration (Failsafe binds to integration-test + verify)
```

Use `verify`, not `integration-test`, so failures are reported. To run a single test: `./mvnw test -Dtest=ClassName` or
`./mvnw verify -Dit.test=ClassNameIT -DfailIfNoTests=false`.

To debug a Playwright test visually, set `launchOptions.headless = false` in `PlaywrightIT` (line 37).

## Package layout convention

- `ch.martinelli.edu.hrs` — application root (`HrsApplication`, app shell).
- `ch.martinelli.edu.hrs.<feature>.ui` — Vaadin views and view-side test base classes.
- `ch.martinelli.edu.hrs.<feature>.domain` — Business logic goes there.
- `ch.martinelli.edu.hrs.db` — **generated** jOOQ code; do not edit by hand.

New feature work generally adds: a Flyway migration (if schema changes) → a Vaadin view under `<feature>.ui` → a
`*BrowserlessTest` subclass for server-side UI logic → optionally a `*IT` Playwright test for end-to-end flows.

## Workshop-aware skills

This repo ships with skills under `aiup-core` (use-case spec, entity model, requirements) and `aiup-vaadin-jooq` (
implement, flyway-migration, browserless-test, karibu-test, playwright-test). Prefer these for spec/scaffolding work —
they encode the workshop's expected structure.

# Mentify Test Strategy

## Purpose

The `tests` repository contains repository-level integration and end-to-end tests for the Mentify backend. Backend modules retain their focused unit and service tests; this repository validates behavior across public service boundaries.

## Test pyramid

1. **Unit tests:** isolated functions and classes, primarily in the backend repository.
2. **Integration tests:** real HTTP, database, authentication, messaging, and service interactions.
3. **End-to-end tests:** complete user journeys through the API Gateway and supported infrastructure.

The development guide targets 80% line/branch coverage for new unit-tested code, integration coverage for all API endpoints, and 90% branch coverage for critical business logic. These are backend product-code targets. This repository reports API-test execution and traceability; it cannot measure backend code coverage unless it consumes JaCoCo reports produced by an instrumented backend build. Never infer code coverage from the number of API tests.

## Test principles

- Use Arrange-Act-Assert.
- Use Given-When-Then test names where practical.
- Test behavior rather than implementation details.
- Keep tests independent and repeatable.
- Use unique test data and clean up created data.
- Use controlled providers or containers for external systems.
- Never use production data or credentials.
- Record requirement traceability for every functional test.

## Backend relationship

The backend is a Java 17 Spring Boot microservice system containing the gateway, service discovery, configuration server, shared library, business services, PostgreSQL, an application Auth Service that issues JWTs, Kafka, Mailpit, AI, and payment integrations. Tests in this repository call supported interfaces and verify cross-service behavior without copying backend source code.

The executed register is generated from JUnit XML and `docs/test-case-mappings.csv` by `tools/test-evidence.py`. It distinguishes passed, failed, skipped, not-run, and unmapped tests. Backend JaCoCo artifacts remain the authoritative coverage measurement and gate result.

## Current verified state and limitations

The repository now contains service integration suites, contracts, security/resilience tests, performance checks, staging smoke, and read-only/write-gated Gateway journeys. `mvn clean verify` runs the deterministic suite; `mvn clean verify -Pintegration` safely skips live classes unless `TEST_ENV_ENABLED=true`. The default CI job does not claim live API coverage.

The source route audit links each of 187 public route rows to at least one request/case. A route link is not a live pass or complete business-semantic assertion. The mapping CSV has 662 case IDs targeting 404 class/method pairs, including opt-in Kafka event ingestion and local Mailpit email delivery. Neither mapping implies a live pass; the local run had no JaCoCo reports, so backend line/branch targets are unverified. See [`testing-requirements-matrix.md`](testing-requirements-matrix.md) for the complete requirement crosswalk and open actions.

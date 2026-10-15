# Mentify Test Strategy

## Purpose

The `tests` repository contains repository-level integration and end-to-end tests for the Mentify backend. Backend modules retain their focused unit and service tests; this repository validates behavior across public service boundaries.

## Test pyramid

1. **Unit tests:** isolated functions and classes, primarily in the backend repository.
2. **Integration tests:** real HTTP, database, authentication, messaging, and service interactions.
3. **End-to-end tests:** complete user journeys through the API Gateway and supported infrastructure.

The development guide targets 80% coverage for new unit-tested code, integration coverage for all API endpoints, and 90% branch coverage for critical business logic. Coverage and test-register evidence will be added as feature tests are implemented.

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

The backend is a Java 17 Spring Boot microservice system containing the gateway, service discovery, configuration server, shared library, business services, PostgreSQL, Keycloak, Kafka, Mailpit, AI, and payment integrations. Tests in this repository call supported interfaces and verify cross-service behavior without copying backend source code.

## Current limitation

TEST-001 only verifies the test framework itself. It does not start the backend or execute feature tests. Backend-backed integration and E2E environments will be introduced by later approved tickets.

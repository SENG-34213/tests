# Mentify Test Repository

This repository contains the Mentify platform's repository-level integration and end-to-end test suites. It complements the unit and service-level tests in [Mentify-backend](https://github.com/SENG-34213/Mentify-backend).

The repository contains reusable API/authentication clients, service integration
tests, authentication and authorization coverage, and gateway end-to-end smoke
tests completed through TEST-022. TEST-023 provides the final quality-gate,
traceability, execution, and limitation documentation.

## Prerequisites

- Java 17
- Maven 3.9+
- Git
- Docker Desktop (required by future Testcontainers suites, not by the foundation smoke test)

## Run locally

```bash
mvn clean test
mvn verify
```

The foundation smoke test does not require the Mentify backend, Docker, Keycloak, PostgreSQL, Kafka, Mailpit, AI providers, or Stripe.

When the backend environment is running, execute live integration tests with:

    $env:TEST_ENV_ENABLED="true"
    mvn verify -Pintegration

The integration profile checks configured services and fails clearly if a
required service is unavailable. See [docs/test-execution.md](docs/test-execution.md)
for service and E2E selectors.

## Configuration

Copy `.env.example` to `.env` for local overrides. Never commit `.env` or real credentials. Test configuration is read from environment variables, with safe local defaults in `src/test/resources/application-test.yml`.

## Repository relationship

The backend repository is a Java 17 Spring Boot microservice monorepo. This repository tests its public boundaries and complete workflows rather than copying backend implementation code. The backend integration branch is currently named `development`.

## Planned test layers

1. API integration tests through the gateway.
2. Service and database integration tests using Testcontainers.
3. Keycloak authentication and authorization tests.
4. Kafka, Mailpit, WebSocket, AI, and payment-provider integration tests using controlled test doubles or containers.
5. End-to-end user journeys.
6. Security, performance, and staging verification.

Staging verification is an explicit, manual-only, read-only workflow. It must
not be used against production; see [staging release smoke](docs/staging-release-smoke.md).

Authentication client usage and safe local credential configuration are documented in [docs/authentication-testing.md](docs/authentication-testing.md).

## Quality and traceability

- [Requirement traceability](docs/requirement-traceability.md)
- [Test-case index](docs/test-case-index.md)
- [Test execution](docs/test-execution.md)
- [CI test execution](docs/ci-test-execution.md)
- [Known limitations](docs/known-limitations.md)
- [Security testing](docs/security-testing.md)
- [Resilience testing](docs/resilience-testing.md)
- [Negative-path test cases](docs/negative-path-test-cases.md)
- [Performance testing](docs/performance-testing.md)
- [Performance baseline](docs/performance-baseline.md)
- [API contract testing](docs/api-contract-testing.md)
- [Contract baseline](docs/contract-baseline.md)
- [Contract change process](docs/contract-change-process.md)
- [Staging release smoke](docs/staging-release-smoke.md)
- [Staging smoke test cases](docs/staging-test-cases.md)

## Standards

- Java 17 and Maven are required.
- Tests use JUnit 5 and Arrange-Act-Assert structure.
- Test names should use Given-When-Then wording where practical.
- Use Conventional Commits: `test(scope): description`, `ci(scope): description`, or `docs(scope): description`.
- Never commit secrets, production data, or production connection strings.
- See `docs/coding-standards.md`, `docs/test-strategy.md`, and `docs/test-case-template.md`.

## CI

GitHub Actions runs static verification on pushes and pull requests, configures
Java 17, caches Maven dependencies, runs `mvn clean verify`, and uploads
Surefire/Failsafe reports. Live service tests remain opt-in and are documented
separately. Performance baselines are manual-only through
`.github/workflows/performance.yml`.

## Branch strategy

- `main`: production-ready test repository.
- `development`: integration branch.
- `feature/<ticket-id>-<slug>`: short-lived feature work.
- `fix/<ticket-id>-<slug>`: bug fixes.

Pull requests require review and passing CI before merging into `development`.

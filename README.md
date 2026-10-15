# Mentify Test Repository

This repository contains the Mentify platform's repository-level integration and end-to-end test suites. It complements the unit and service-level tests in [Mentify-backend](https://github.com/SENG-34213/Mentify-backend).

The repository foundation and reusable API/authentication clients are implemented through `TEST-004`. Backend API, database, messaging, security, and end-to-end suites will be added in later tickets.

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

When the backend environment is running, execute the readiness checks with:

    $env:TEST_ENV_ENABLED="true"
    mvn verify -Pintegration

The integration profile checks the configured services and fails clearly if a required service is unavailable.

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

Authentication client usage and safe local credential configuration are documented in [docs/authentication-testing.md](docs/authentication-testing.md).

## Standards

- Java 17 and Maven are required.
- Tests use JUnit 5 and Arrange-Act-Assert structure.
- Test names should use Given-When-Then wording where practical.
- Use Conventional Commits: `test(scope): description`, `ci(scope): description`, or `docs(scope): description`.
- Never commit secrets, production data, or production connection strings.
- See `docs/coding-standards.md`, `docs/test-strategy.md`, and `docs/test-case-template.md`.

## CI

GitHub Actions runs on pushes and pull requests. It checks out the repository, configures Java 17, caches Maven dependencies, runs `mvn verify`, and uploads Surefire reports. Service-backed tests will be enabled by later tickets.

## Branch strategy

- `main`: production-ready test repository.
- `development`: integration branch.
- `feature/<ticket-id>-<slug>`: short-lived feature work.
- `fix/<ticket-id>-<slug>`: bug fixes.

Pull requests require review and passing CI before merging into `development`.

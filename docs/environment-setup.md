# Test Environment Setup

## Current ticket

TEST-008 adds Course Service integration coverage to the optional integration profile. The default unit-test build does not require Docker or a running Mentify backend.

## Prerequisites

- Java 17
- Maven 3.9 or newer
- Git
- Docker Desktop for future Testcontainers suites

## Local commands

```bash
java -version
mvn -version
mvn clean test
mvn verify
```

Run backend readiness checks only when the required backend environment is running:

    $env:TEST_ENV_ENABLED="true"
    mvn verify -Pintegration

The integration profile checks the services listed in `TEST_REQUIRED_SERVICES`. By default this is `API_GATEWAY,KEYCLOAK`. It retries temporary failures and stops after `READINESS_TIMEOUT_SECONDS`.

Calendar integration tests additionally verify `CALENDAR_SERVICE`. Set `TEST_REQUIRED_SERVICES=API_GATEWAY,KEYCLOAK,CALENDAR_SERVICE` when running the complete calendar environment. The calendar service uses the backend API paths under `/api/v1/calendar` and is configured with `CALENDAR_SERVICE_BASE_URL` for readiness checks.

Course integration tests additionally verify `COURSE_SERVICE`. Set `TEST_REQUIRED_SERVICES=API_GATEWAY,KEYCLOAK,COURSE_SERVICE` when running the complete Course environment. The Course Service uses the backend API paths under `/api/v1/course` and is configured with `COURSE_SERVICE_BASE_URL` for readiness checks.

If a required service is unavailable, the integration command fails with the service URL and the last observed failure. This is intentional; an unavailable environment must not be reported as a passing integration test.

## Future services

Later tickets may require the backend API Gateway, Keycloak, PostgreSQL, Kafka, Mailpit, service discovery, configuration server, AI provider doubles, and payment-provider doubles. Their URLs and credentials must be supplied through environment variables or GitHub Secrets.

Use the backend repository's `docs/PROJECT_SETUP.md`, `.env.example`, `docker-compose.yml`, and `docs/KEYCLOAK_SETUP.md` as the source of truth for startup and service configuration.

## Secrets

Copy `.env.example` to `.env` for local values. Never commit `.env`, real passwords, tokens, API keys, production URLs containing credentials, or production data.

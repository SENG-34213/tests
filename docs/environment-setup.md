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

The integration profile checks the services listed in `TEST_REQUIRED_SERVICES`. For the backend's application-issued JWT flow, the default is `API_GATEWAY,AUTH_SERVICE` (Gateway on port 8080 and Auth Service on port 8085). Authentication login tests additionally require User Service because Auth Service validates the user's profile there. It retries temporary failures and stops after `READINESS_TIMEOUT_SECONDS`.

Calendar integration tests additionally verify `CALENDAR_SERVICE`. Set `TEST_REQUIRED_SERVICES=API_GATEWAY,AUTH_SERVICE,CALENDAR_SERVICE` when running the complete calendar environment. The calendar service uses the backend API paths under `/api/v1/calendar` and is configured with `CALENDAR_SERVICE_BASE_URL` for readiness checks.

Course integration tests additionally verify `COURSE_SERVICE`. Set `TEST_REQUIRED_SERVICES=API_GATEWAY,AUTH_SERVICE,COURSE_SERVICE` when running the complete Course environment. The Course Service uses the backend API paths under `/api/v1/course` and is configured with `COURSE_SERVICE_BASE_URL` for readiness checks.

## Step 3: JWT test accounts and fixtures

The backend `auth-service` issues the application JWTs; Keycloak is not part of this environment. Start the backend's PostgreSQL, Kafka, and Mailpit profiles when the selected services need them. Check Gateway, Auth Service, and User Service readiness before attempting login.

Use dedicated non-production accounts for all four roles: student, teacher, admin, and super-admin. Supply their credentials only through `STUDENT_TEST_USERNAME/PASSWORD`, `TEACHER_TEST_USERNAME/PASSWORD`, `ADMIN_TEST_USERNAME/PASSWORD`, and `SUPER_ADMIN_TEST_USERNAME/PASSWORD`. The generic `TEST_USERNAME/PASSWORD` pair is only a fallback. Never commit these values or paste them into chat.

Do not assume that enabling `MENTIFY_SAMPLE_USERS_ENABLED` creates account profiles. The current backend sample-user bootstrap only provisions Auth Service credentials for matching profiles that already exist. The super-admin bootstrap can create the initial super-admin only when none exists. Other roles must be registered as local test users; registration emails must go to Mailpit so temporary credentials stay inside the test environment.

Before registering users, configure the backend's local `.env` so SMTP uses Mailpit—not an external provider. For backend services running directly on the host, use `SMTP_HOST=localhost` and the host port mapped to Mailpit's container port `1025` (the current local mapping is host port `587`). For an application running in the Compose network, use `SMTP_HOST=mailpit` and `SMTP_PORT=1025`. Verify the message appears in Mailpit before using its temporary credential. Do not enable registration tests while SMTP points at Gmail or another external provider.

Create or identify a synthetic test course and a related enrollment; keep the resulting UUIDs in environment variables, not source files. The repository uses `TEST_COURSE_GRADE_ID` and `TEST_COURSE_TEACHER_ID` for Course CRUD fixtures, plus `TEST_ENROLLMENT_STUDENT_ID`, `TEST_ENROLLMENT_COURSE_ID`, and `TEST_ENROLLMENT_ID` for Enrollment fixtures. Some tests need additional ownership/free-course fixtures listed in [Course testing](course-testing.md) and [Enrollment testing](enrollment-testing.md). Use disposable, non-production data for write-enabled tests.

If a required service is unavailable, the integration command fails with the service URL and the last observed failure. This is intentional; an unavailable environment must not be reported as a passing integration test.

## Future services

The application JWT flow uses the backend Auth Service (including its database) rather than an external identity provider. Other tickets may require PostgreSQL, Kafka, Mailpit, service discovery, configuration server, AI provider doubles, and payment-provider doubles. Their URLs and credentials must be supplied through environment variables or GitHub Secrets.

Use the backend repository's `docs/PROJECT_SETUP.md`, `.env.example`, `docker-compose.yml`, and Auth Service/JWT documentation as the source of truth for startup and service configuration. Review SMTP settings in `.env` before registration so test credentials cannot be routed to a real email provider.

## Secrets

Copy `.env.example` to `.env` for local values. Never commit `.env`, real passwords, tokens, API keys, production URLs containing credentials, or production data.

# Course Service Testing

TEST-008 adds the first Course Service integration-test foundation. The tests use the reusable API, authentication, and readiness clients and target the backend contract from the `development` branch.

## Confirmed backend contract

The backend Course Service is located in the `course-service` module and uses these public paths:

- `GET /api/v1/course/my-courses` requires `TEACHER`.
- `POST /api/v1/course` requires `ADMIN` or `SUPER_ADMIN`.
- `PUT /api/v1/course/{courseId}` requires `ADMIN` or `SUPER_ADMIN`.
- `GET /api/v1/course/{courseId}` requires `ADMIN` or `SUPER_ADMIN`.
- `GET /api/v1/course` requires `ADMIN` or `SUPER_ADMIN`.
- `GET /api/v1/course/{courseId}/lookup` allows `STUDENT`, `TEACHER`, `ADMIN`, or `SUPER_ADMIN`.
- `POST /api/v1/course/bulk` requires `ADMIN` or `SUPER_ADMIN`.
- `DELETE /api/v1/course/{courseId}` requires `ADMIN` or `SUPER_ADMIN`.

TEST-008 verifies readiness, authenticated read access, unauthenticated rejection, and role-based rejection. Persistent Course Service workflows are reserved for later tickets.

## Environment

Set `TEST_ENV_ENABLED=true` before live integration testing. Include `COURSE_SERVICE` in `TEST_REQUIRED_SERVICES` and provide `COURSE_SERVICE_BASE_URL` when the default local URL is not correct.

Use the existing role-based variables:

- `STUDENT_TEST_USERNAME` and `STUDENT_TEST_PASSWORD`
- `TEACHER_TEST_USERNAME` and `TEACHER_TEST_PASSWORD`
- `ADMIN_TEST_USERNAME` and `ADMIN_TEST_PASSWORD`

Never commit credentials, tokens, production data, or production URLs containing secrets.

## Commands

    $env:TEST_ENV_ENABLED="true"
    $env:TEST_REQUIRED_SERVICES="API_GATEWAY,KEYCLOAK,COURSE_SERVICE"
    mvn verify -Pintegration -Dtest="Course*IT"

The default command `mvn clean verify` runs unit tests and does not require the backend environment.

## Test data

TEST-008 uses read-only requests and authorization-only requests. It does not create a persistent course. Course creation, update, and deletion workflows require a later ticket with controlled fixtures and cleanup.

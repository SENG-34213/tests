# Course Service Testing

TEST-008 adds the first Course Service integration-test foundation. The tests use the reusable API, authentication, and readiness clients and target the backend contract from the `development` branch.

## Confirmed backend contract

The backend Course Service is located in the `course-service` module and uses these public paths:

- `GET /api/v1/course/my-courses` requires `TEACHER`.
- `POST /api/v1/course` allows `TEACHER`, `ADMIN`, or `SUPER_ADMIN` (service-level validation still applies).
- `PUT /api/v1/course/{courseId}` allows `TEACHER`, `ADMIN`, or `SUPER_ADMIN` (teachers are further restricted to their own courses).
- `GET /api/v1/course/{courseId}` allows `TEACHER`, `ADMIN`, or `SUPER_ADMIN`.
- `GET /api/v1/course` requires `ADMIN` or `SUPER_ADMIN`.
- `GET /api/v1/course/{courseId}/lookup` allows `STUDENT`, `TEACHER`, `ADMIN`, or `SUPER_ADMIN`.
- `POST /api/v1/course/bulk` requires `ADMIN` or `SUPER_ADMIN`.
- `DELETE /api/v1/course/{courseId}` requires `ADMIN` or `SUPER_ADMIN`.

TEST-008 verifies readiness, authenticated read access, unauthenticated rejection, and role-based rejection. TEST-009 adds persistent CRUD workflow coverage with controlled fixtures and cleanup.

## Environment

Set `TEST_ENV_ENABLED=true` before live integration testing. Include `COURSE_SERVICE` in `TEST_REQUIRED_SERVICES` and provide `COURSE_SERVICE_BASE_URL` when the default local URL is not correct.

Use the existing role-based variables:

- `STUDENT_TEST_USERNAME` and `STUDENT_TEST_PASSWORD`
- `TEACHER_TEST_USERNAME` and `TEACHER_TEST_PASSWORD`
- `ADMIN_TEST_USERNAME` and `ADMIN_TEST_PASSWORD`
- `TEST_COURSE_GRADE_ID` and `TEST_COURSE_TEACHER_ID` for existing non-production fixtures used by CRUD tests
- `TEST_TEACHER_PORTAL_COURSE_ID` and `TEST_TEACHER_PORTAL_STUDENT_ID` for an owned course and enrolled student used by teacher portal data tests
- `TEST_TEACHER_PORTAL_UNOWNED_COURSE_ID` for cross-teacher ownership-denial checks
- `TEST_COURSE_CONTENT_OWNERSHIP_WRITE_ENABLED=true` plus
  `TEST_TEACHER_PORTAL_UNOWNED_COURSE_ID`, `TEST_TEACHER_PORTAL_UNOWNED_MODULE_ID`,
  `TEST_TEACHER_PORTAL_UNOWNED_LESSON_ID`, and
  `TEST_TEACHER_PORTAL_UNOWNED_LEARNING_MATERIAL_ID` for explicit cross-owner
  course-content write-denial checks; use only valid fixture IDs on a disposable
  non-production backend
- `TEST_STUDENT_PROGRESS_LESSON_ID` and `TEST_STUDENT_PROGRESS_WRITE_ENABLED=true` for a progress update test that restores its prior completion state
- `TEST_COURSE_ANNOUNCEMENT_WRITE_ENABLED=true` to create a synthetic announcement on an approved test course; announcements persist because no delete route exists
- `TEST_COURSE_CONTENT_UPLOAD_WRITE_ENABLED=true` to run cross-service module/lesson file upload checks; requires Course and Material services and cleans up references and uploaded synthetic materials

Never commit credentials, tokens, production data, or production URLs containing secrets.

## Commands

    $env:TEST_ENV_ENABLED="true"
    $env:TEST_REQUIRED_SERVICES="API_GATEWAY,KEYCLOAK,COURSE_SERVICE"
    mvn verify -Pintegration -Dtest="Course*IT"

The default command `mvn clean verify` runs unit tests and does not require the backend environment.

## Test data

TEST-008 uses read-only requests and authorization-only requests. TEST-009 creates uniquely named courses with configured fixtures, verifies the lifecycle, and deletes every created course in cleanup. The CRUD tests are skipped when the two fixture IDs are not configured.

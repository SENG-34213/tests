# Calendar Service Testing

TEST-006 added the Calendar Service integration-test foundation. TEST-007 adds authenticated event workflow coverage using the reusable API and authentication clients and targets the backend contract from the `development` branch.

## Confirmed backend contract

The backend Calendar Service is located in the `calender-service` module, while its public API uses these paths:

- `GET /api/v1/calendar/upcoming?limit={limit}` requires an authenticated Student, Teacher, Admin, or Super Admin.
- `GET /api/v1/calendar/events` requires an authenticated Student, Teacher, Admin, or Super Admin.
- `POST /api/v1/calendar/events` requires a Teacher, Admin, or Super Admin.
- `PUT /api/v1/calendar/events/{eventId}` requires a Teacher, Admin, or Super Admin.
- `POST /api/v1/calendar/events/{eventId}/cancel` requires a Teacher, Admin, or Super Admin.

TEST-006 verifies the safe read path and unauthenticated rejection. TEST-007 verifies authorized creation, retrieval, update, cancellation, role restrictions, and validation failures.

## Environment

Set `TEST_ENV_ENABLED=true` before integration testing. Include `CALENDAR_SERVICE` in `TEST_REQUIRED_SERVICES` and provide `CALENDAR_SERVICE_BASE_URL` when the default local URL is not correct.

Use dedicated test users from `.env.example`, including `STUDENT_TEST_USERNAME`, `TEACHER_TEST_USERNAME`, and `ADMIN_TEST_USERNAME` with matching password variables. Never commit credentials, tokens, production data, or production URLs containing secrets.

TEST-007 creates unique future-dated events and cancels them during cleanup. Teacher lifecycle tests use
`TEST_CALENDAR_COURSE_ID`, which must be assigned to the primary teacher and not the optional distinct
`SECONDARY_TEACHER_TEST_USERNAME`/`SECONDARY_TEACHER_TEST_PASSWORD` account. They use course visibility and verify
that the other teacher receives `404` when reading and `403` when updating or cancelling the event. Admin lifecycle tests use private visibility. Course
Service must be reachable for teacher course-event tests. If cleanup fails, investigate before treating the run as
successful.

## Commands

    $env:TEST_ENV_ENABLED="true"
    $env:TEST_REQUIRED_SERVICES="API_GATEWAY,AUTH_SERVICE,CALENDAR_SERVICE,COURSE_SERVICE"
    mvn verify -Pintegration

The default command `mvn clean verify` runs unit tests and does not require the backend environment.

The venue-create positive-path probe is separately gated by
`TEST_CALENDAR_VENUE_WRITE_ENABLED=true` and
`TEST_CALENDAR_DISPOSABLE_DATABASE_CONFIRMED=true`. The backend exposes no
venue-delete route, so use only a disposable Calendar database; its uniquely
named test venue remains until that database is discarded.

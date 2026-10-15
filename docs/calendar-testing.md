# Calendar Service Testing

TEST-006 adds the first Calendar Service integration-test foundation. The tests use the reusable API and authentication clients and target the backend contract from the `development` branch.

## Confirmed backend contract

The backend Calendar Service is located in the `calender-service` module, while its public API uses these paths:

- `GET /api/v1/calendar/upcoming?limit={limit}` requires an authenticated Student, Teacher, Admin, or Super Admin.
- `GET /api/v1/calendar/events` requires an authenticated Student, Teacher, Admin, or Super Admin.
- `POST /api/v1/calendar/events` requires a Teacher, Admin, or Super Admin.
- `PUT /api/v1/calendar/events/{eventId}` requires a Teacher, Admin, or Super Admin.
- `POST /api/v1/calendar/events/{eventId}/cancel` requires a Teacher, Admin, or Super Admin.

TEST-006 verifies the safe read path and unauthenticated rejection. Persistent event workflows are reserved for later tickets.

## Environment

Set `TEST_ENV_ENABLED=true` before integration testing. Include `CALENDAR_SERVICE` in `TEST_REQUIRED_SERVICES` and provide `CALENDAR_SERVICE_BASE_URL` when the default local URL is not correct.

Use dedicated test users from `.env.example`. Never commit credentials, tokens, production data, or production URLs containing secrets.

## Commands

    $env:TEST_ENV_ENABLED="true"
    $env:TEST_REQUIRED_SERVICES="API_GATEWAY,KEYCLOAK,CALENDAR_SERVICE"
    mvn verify -Pintegration

The default command `mvn clean verify` runs unit tests and does not require the backend environment.

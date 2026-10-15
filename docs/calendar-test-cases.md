# TEST-006 Calendar Service Test Cases

| ID | Test case | Endpoint | Expected result | Type |
| --- | --- | --- | --- | --- |
| TC-CAL-001 | Upcoming events without authentication | `GET /api/v1/calendar/upcoming` | `401 Unauthorized` | Integration |
| TC-CAL-002 | Create event without authentication | `POST /api/v1/calendar/events` | `401 Unauthorized` | Integration |
| TC-CAL-003 | Student reads upcoming events | `GET /api/v1/calendar/upcoming` | `200` with successful API response and data field | Integration |
| TC-CAL-004 | Calendar client configuration | Calendar client construction | Client is created only with a valid API client | Unit |
| TC-CAL-005 | Calendar Service readiness | Calendar Service actuator readiness endpoint | Service is ready before calendar tests execute | Integration |

## Preconditions

- Java 17 and Maven are installed.
- The backend API Gateway and Keycloak are running.
- The Calendar Service is running for integration tests.
- Dedicated Student credentials are configured for TC-CAL-003.
- `TEST_ENV_ENABLED=true` is set for integration tests.

## Isolation and safety

The tests do not create, update, or cancel a persistent calendar event. The create request in TC-CAL-002 is intentionally sent without authentication and should be rejected before any event is persisted.

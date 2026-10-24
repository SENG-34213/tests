# Calendar Service Test Cases

| ID | Test case | Endpoint | Expected result | Type |
| --- | --- | --- | --- | --- |
| TC-CAL-001 | Upcoming events without authentication | `GET /api/v1/calendar/upcoming` | `401 Unauthorized` | Integration |
| TC-CAL-002 | Create event without authentication | `POST /api/v1/calendar/events` | `401 Unauthorized` | Integration |
| TC-CAL-003 | Student reads upcoming events | `GET /api/v1/calendar/upcoming` | `200` with successful API response and data field | Integration |
| TC-CAL-004 | Calendar client configuration | Calendar client construction | Client is created only with a valid API client | Unit |
| TC-CAL-005 | Calendar Service readiness | Calendar Service actuator readiness endpoint | Service is ready before calendar tests execute | Integration |
| TC-CAL-006 | Teacher creates an event | `POST /api/v1/calendar/events` | Successful response containing an event identifier | Integration |
| TC-CAL-007 | Admin creates an event | `POST /api/v1/calendar/events` | Successful response containing an event identifier | Integration |
| TC-CAL-008 | Student retrieves events in a required date range | `GET /api/v1/calendar/events?start=…&end=…` | `200` with successful API response and data field | Integration |
| TC-CAL-009 | Teacher updates an event | `PUT /api/v1/calendar/events/{eventId}` | Successful response with updated event | Integration |
| TC-CAL-010 | Teacher cancels an event | `POST /api/v1/calendar/events/{eventId}/cancel` | Successful cancellation response | Integration |
| TC-CAL-011 | Student creates an event | `POST /api/v1/calendar/events` | `403 Forbidden` | Authorization |
| TC-CAL-012 | Student updates an event | `PUT /api/v1/calendar/events/{eventId}` | `403 Forbidden` | Authorization |
| TC-CAL-013 | Student cancels an event | `POST /api/v1/calendar/events/{eventId}/cancel` | `403 Forbidden` | Authorization |
| TC-CAL-014 | Required event fields are missing | `POST /api/v1/calendar/events` | `400 Bad Request` | Validation |
| TC-CAL-015 | Event date range is invalid | `POST /api/v1/calendar/events` | `400 Bad Request` | Validation |
| TC-CAL-016 | Event identifier does not exist | `PUT /api/v1/calendar/events/{eventId}` | `404 Not Found` | Negative |
| TC-CAL-017 | Venue list requires authentication | `GET /api/v1/calendar/venues` | `401` without token; `200` for authenticated user | Security |
| TC-CAL-018 | Venue creation is admin-only | `POST /api/v1/calendar/venues` | `401` unauthenticated; `403` student/teacher | Authorization |
| TC-CAL-019 | Invalid venue fields are rejected | `POST /api/v1/calendar/venues` | `400 Bad Request` | Validation |
| TC-CAL-020 | Teacher reads an event after updating it | `GET /api/v1/calendar/events/{eventId}` | `200`; response ID/title match the updated event | Integration |
| TC-CAL-021 | Unknown event detail is not found | `GET /api/v1/calendar/events/{eventId}` | `404 Not Found` | Negative |
| TC-CAL-022 | Event query requires a date range | `GET /api/v1/calendar/events` | Valid range returns `200`; missing required `start`/`end` returns `400` | Validation |
| TC-CAL-023 | Event detail requires authentication | `GET /api/v1/calendar/events/{eventId}` | `401 Unauthorized` without a token | Security |

## Preconditions

- Java 17 and Maven are installed.
- The backend API Gateway and Auth Service are running.
- The Calendar Service is running for integration tests.
- Dedicated Student credentials are configured for TC-CAL-003.
- `TEST_ENV_ENABLED=true` is set for integration tests.

## Isolation and safety

TEST-006 tests do not create persistent events. TEST-007 creates unique future events through authorized users and cancels them during cleanup. Tests do not access the database directly or depend on execution order.

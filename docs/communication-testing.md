# Communication Service integration testing

TEST-016 covers the HTTP contract exposed by Communication Service through the API Gateway.

## Scope

- Readiness of API Gateway, Keycloak, Course Service, Enrollment Service, and Communication Service.
- Authenticated-user lookup at `/api/communication/me`.
- Group creation, listing, retrieval, members, course membership, student membership, archive, and unarchive.
- Message creation, paginated history, update, and deletion for `ME` and `EVERYONE` scopes.
- Per-user communication settings read and update.
- Missing authentication, role authorization, validation, unknown IDs, and pagination input.
- WebSocket, Kafka, notifications, and real-time delivery are outside this repository ticket.

## Configuration

Copy `.env.example` into the local environment and provide the three role credentials used by the authentication
configuration. Set `TEST_REQUIRED_SERVICES` to include `COMMUNICATION_SERVICE`, `COURSE_SERVICE`, and
`ENROLLMENT_SERVICE`. The default Communication Service URL is `http://localhost:8086`.

Use approved non-production UUIDs for `TEST_COMMUNICATION_COURSE_ID`, `TEST_COMMUNICATION_STUDENT_ID`, and,
for message tests, `TEST_COMMUNICATION_GROUP_ID`. `TEST_COMMUNICATION_WRITE_ENABLED` remains `false` unless a
maintainer has approved isolated write fixtures. The write tests are skipped when required fixtures are absent or
the flag is disabled.

## Running

```powershell
mvn test
mvn -Pintegration verify
```

The normal test phase runs client/unit tests. The integration profile runs classes ending in `IT` and requires the
backend services and Keycloak to be running. Integration tests never use real production credentials or IDs.

## Cleanup and isolation

Tests use unique group names and message content. Groups created by the write workflow are archived and unarchived
within the workflow; the backend does not expose a group-delete endpoint. Message tests delete their unique message
for everyone after updating it. Do not enable writes against shared or production data.

## Deletion scopes and pagination

Message deletion sends `scope=ME` or `scope=EVERYONE` as a query parameter. Message history sends zero-based `page`
and positive `size` query parameters. The client rejects invalid local pagination values before making a request.

# Notification Service integration testing

TEST-019 verifies notification retrieval and management through the API Gateway using authenticated JWTs. The
Notification Service identifies the recipient from the JWT subject; tests do not access its database directly.

## Scope

The API coverage includes paginated notification listing and response metadata, unread-only filtering, unread count,
idempotent mark-one-as-read, mark-all-as-read, deletion, exact invalid/unknown identifier responses, authentication,
and cross-user mutation denial for a student-owned fixture. Kafka event ingestion and
email delivery are documented as optional because the repository has no safe public event-publish endpoint and live
email assertions require a deterministic mail-capture provider.

## Configuration

```properties
TEST_ENV_ENABLED=true
TEST_REQUIRED_SERVICES=API_GATEWAY,AUTH_SERVICE,NOTIFICATION_SERVICE
NOTIFICATION_SERVICE_BASE_URL=http://localhost:8093
TEST_NOTIFICATION_WRITE_ENABLED=false
TEST_NOTIFICATION_ID=<one-time-disposable-notification-owned-by-student>
```

Use the existing role credentials for two synthetic users, for example `STUDENT` and `TEACHER`. The configured
notification must belong to the student account; the write workflow marks and then deletes it, so it must be a
one-time disposable fixture and cannot be reused after a successful run. Do not commit
passwords, JWTs, recipient IDs, broker credentials, email addresses, or provider secrets. Write operations remain
disabled unless an approved non-production notification fixture is configured.

## API behavior

`GET /api/v1/notifications` accepts zero-based `page`, `size` from 1 through 100, and `unreadOnly`. The response
contains `notifications`, page metadata, and `unreadCount`. The unread-count endpoint returns a numeric data value.
Mark-read, read-all, and delete operations are scoped to the authenticated JWT subject.

## Event and email limitations

The backend consumes Kafka notification events internally. `NotificationEventEmailWorkflowIT` is opt-in and publishes
one synthetic `QUIZ_CREATED` event to the configured non-production broker. It verifies the user's HTTP notification
and email status, and uses a random `example.invalid` recipient. Before enabling it, confirm that backend notification
SMTP points only to local Mailpit; the test separately validates that the Mailpit API uses a local HTTP host. Real
email addresses and external SMTP delivery are never used. The event and email checks remain skipped in static CI.

Set `TEST_NOTIFICATION_EVENT_ENABLED=true`, `NOTIFICATION_EMAIL_TEST_MODE=true`,
`NOTIFICATION_KAFKA_BOOTSTRAP_SERVERS`, and `MAILPIT_API_BASE_URL`. Optional Kafka SASL values are read from the
environment and are not logged. The test removes only the notification it creates and Mailpit messages addressed to
its unique synthetic recipient. It does not create or delete Kafka topics.

## Running

```powershell
mvn test
mvn verify -Pintegration
mvn -Pintegration verify -Dtest="Notification*IT"
```

The normal test phase runs `NotificationClientTest`. Integration classes use the repository `*IT` convention and
require API Gateway, Auth Service, and Notification Service readiness.

# Notification Service integration testing

TEST-019 verifies notification retrieval and management through the API Gateway using authenticated JWTs. The
Notification Service identifies the recipient from the JWT subject; tests do not access its database directly.

## Scope

The API coverage includes paginated notification listing, unread-only filtering, unread count, mark-one-as-read,
mark-all-as-read, deletion, invalid identifiers, authentication, and recipient isolation. Kafka event ingestion and
email delivery are documented as optional because the repository has no safe public event-publish endpoint and live
email assertions require a deterministic mail-capture provider.

## Configuration

```properties
TEST_ENV_ENABLED=true
TEST_REQUIRED_SERVICES=API_GATEWAY,KEYCLOAK,NOTIFICATION_SERVICE
NOTIFICATION_SERVICE_BASE_URL=http://localhost:8093
TEST_NOTIFICATION_WRITE_ENABLED=false
TEST_NOTIFICATION_ID=<approved-notification-uuid>
```

Use the existing role credentials for two synthetic users, for example `STUDENT` and `TEACHER`. Do not commit
passwords, JWTs, recipient IDs, broker credentials, email addresses, or provider secrets. Write operations remain
disabled unless an approved non-production notification fixture is configured.

## API behavior

`GET /api/v1/notifications` accepts zero-based `page`, `size` from 1 through 100, and `unreadOnly`. The response
contains `notifications`, page metadata, and `unreadCount`. The unread-count endpoint returns a numeric data value.
Mark-read, read-all, and delete operations are scoped to the authenticated JWT subject.

## Event and email limitations

The backend consumes Kafka notification events internally. Event tests must only be enabled when a deterministic
publisher, broker, event schema, and cleanup strategy are available. Email tests must only be enabled with a local
mail-capture provider; real email delivery and retry delays are out of scope. When these dependencies are missing,
the related scenarios are documented and skipped rather than sent to external systems.

## Running

```powershell
mvn test
mvn verify -Pintegration
mvn -Pintegration verify -Dtest="Notification*IT"
```

The normal test phase runs `NotificationClientTest`. Integration classes use the repository `*IT` convention and
require API Gateway, Keycloak, and Notification Service readiness.

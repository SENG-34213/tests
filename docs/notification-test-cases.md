# TEST-019 notification test cases

| ID | Scenario | Expected result |
| --- | --- | --- |
| TC-TEST-019-01 | Service readiness | API Gateway, Auth Service, and Notification Service are checked. |
| TC-TEST-019-02 | Notification listing | Authenticated user receives a bounded page with valid metadata. |
| TC-TEST-019-03 | Default pagination | Default page `0` and size `20` are verified over HTTP. |
| TC-TEST-019-04 | Later page | Requested page is returned and stays within the requested size. |
| TC-TEST-019-05 | Page-size boundaries | Client rejects invalid values; HTTP endpoint rejects page < 0, size < 1, and size > 100 with `400`. |
| TC-TEST-019-06 | Unread filter | `unreadOnly=true` returns only records whose `readAt` is null. |
| TC-TEST-019-07 | Unread count | Count is numeric, non-negative and at least the unread items returned on the page. |
| TC-TEST-019-08 | Mark one read | Owner receives the notification and a non-null, stable `readAt` timestamp. |
| TC-TEST-019-09 | Mark all read | Owner receives `200` and a non-negative changed-record count. |
| TC-TEST-019-10 | Delete | Owner receives `200` when deleting their disposable approved notification. |
| TC-TEST-019-11 | Ownership list isolation | Two users receive separate notification collections. |
| TC-TEST-019-12 | Ownership mutation protection | Another user receives `404` when marking or deleting the student's notification. |
| TC-TEST-019-13 | Unauthenticated access | All notification endpoints reject missing authentication. |
| TC-TEST-019-14 | Invalid ID | Malformed notification UUID returns `400`. |
| TC-TEST-019-15 | Unknown ID | Unknown notification ID returns `404`. |
| TC-TEST-019-16 | Invalid pagination | Negative page, zero size, and size above 100 are rejected by the client. |
| TC-TEST-019-17 | Event ingestion | A synthetic `QUIZ_CREATED` Kafka event appears in the authenticated student's notification list. |
| TC-TEST-019-18 | Email delivery | The event reaches `SENT` and its synthetic `.invalid` recipient message is captured by local Mailpit. |

The HTTP pagination-boundary test records the intended `400` contract. The inspected shared exception handler does
not explicitly map method-validation exceptions; verify this case against the live non-production API and track a
backend handler correction if the endpoint returns `500`.

## Manual safeguards and live execution requirements

- **TC-TEST-019-17/18:** `NotificationEventEmailWorkflowIT` publishes one unique event to the configured non-production Kafka topic, checks the resulting owner-scoped notification and `SENT` status, and uses only a random `example.invalid` recipient. The email workflow must be enabled only after confirming backend SMTP points to the local Mailpit instance.
- Set `TEST_NOTIFICATION_EVENT_ENABLED=true`, `NOTIFICATION_EMAIL_TEST_MODE=true`, `NOTIFICATION_KAFKA_BOOTSTRAP_SERVERS`, and `MAILPIT_API_BASE_URL` for a controlled live run. The test deletes its notification and only Mailpit messages created for its unique synthetic recipient.
- These scenarios are source-mapped but are **not live-verified** unless the generated execution register records them as passed; static CI keeps them skipped.
- **Security review:** Review that tokens, recipient data, broker secrets, and email credentials are not exposed when changing notification integration or logging.
- **Documentation review:** Recheck configuration, ownership, cleanup, and skip rules when the suite changes.

# TEST-019 notification test cases

| ID | Scenario | Expected result |
| --- | --- | --- |
| TC-TEST-019-01 | Service readiness | API Gateway, Keycloak, and Notification Service are checked. |
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
| TC-TEST-019-17 | Event ingestion | Supported events are tested only with deterministic broker fixtures. |
| TC-TEST-019-18 | Email delivery | Mail capture is tested only with a safe deterministic provider. |
| TC-TEST-019-19 | Security | Tokens, recipient data, broker secrets, and email credentials are not exposed. |
| TC-TEST-019-20 | Documentation | Configuration, ownership, cleanup, and skip rules are documented. |

The HTTP pagination-boundary test records the intended `400` contract. The inspected shared exception handler does
not explicitly map method-validation exceptions; verify this case against the live non-production API and track a
backend handler correction if the endpoint returns `500`.

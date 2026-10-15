# TEST-019 notification test cases

| ID | Scenario | Expected result |
| --- | --- | --- |
| TC-TEST-019-01 | Service readiness | API Gateway, Keycloak, and Notification Service are checked. |
| TC-TEST-019-02 | Notification listing | Authenticated user receives a notification page. |
| TC-TEST-019-03 | Default pagination | Default page and size behavior is verified. |
| TC-TEST-019-04 | Later page | Valid later page returns safely. |
| TC-TEST-019-05 | Page-size boundaries | Sizes 1 and 100 are accepted; out-of-range values are rejected. |
| TC-TEST-019-06 | Unread filter | `unreadOnly=true` returns unread notifications only. |
| TC-TEST-019-07 | Unread count | Count is numeric and non-negative. |
| TC-TEST-019-08 | Mark one read | Owner can mark an approved notification read. |
| TC-TEST-019-09 | Mark all read | Owner can mark all own notifications read. |
| TC-TEST-019-10 | Delete | Owner can delete an approved notification. |
| TC-TEST-019-11 | Ownership list isolation | Two users receive separate notification collections. |
| TC-TEST-019-12 | Ownership mutation protection | A user cannot mutate another user's notification ID. |
| TC-TEST-019-13 | Unauthenticated access | All notification endpoints reject missing authentication. |
| TC-TEST-019-14 | Invalid ID | Malformed notification IDs are handled without a server error. |
| TC-TEST-019-15 | Unknown ID | Unknown notification IDs are handled without a server error. |
| TC-TEST-019-16 | Invalid pagination | Negative page, zero size, and size above 100 are rejected by the client. |
| TC-TEST-019-17 | Event ingestion | Supported events are tested only with deterministic broker fixtures. |
| TC-TEST-019-18 | Email delivery | Mail capture is tested only with a safe deterministic provider. |
| TC-TEST-019-19 | Security | Tokens, recipient data, broker secrets, and email credentials are not exposed. |
| TC-TEST-019-20 | Documentation | Configuration, ownership, cleanup, and skip rules are documented. |

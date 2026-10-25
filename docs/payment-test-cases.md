# TEST-018 payment test cases

| ID | Scenario | Expected result |
| --- | --- | --- |
| TC-TEST-018-01 | Required-service readiness | Gateway, Auth Service, Course, Enrollment, and Payment services are checked. |
| TC-TEST-018-02 | Start course payment | `201`, pending payment, amount/currency and non-empty client secret are returned. |
| TC-TEST-018-03 | Student payment list | `200`; newly initiated owned payment is present. |
| TC-TEST-018-04 | Student payment detail | `200`; payment ID, course, provider and status match the initiated payment. |
| TC-TEST-018-05 | Student payment status | `200`; status resource matches payment detail. |
| TC-TEST-018-06 | Payment ownership | Second student does not see payment in their list; detail/status return `404`. |
| TC-TEST-018-07 | Admin payment list | Administrator receives valid Page metadata and bounded content. |
| TC-TEST-018-08 | Admin filters | Status, student, course, date range and payment-reference filters return matching rows. |
| TC-TEST-018-09 | Admin pagination | Page and size values are accepted and returned in metadata; invalid page requests are rejected by client validation. |
| TC-TEST-018-10 | Admin payment detail | Administrator can inspect payment fields and ID for the selected row. |
| TC-TEST-018-11 | Successful verification | Requested student/course payment verification is returned. |
| TC-TEST-018-12 | Refund | Dedicated successful test payment transitions to `REFUNDED`; provider refund ID/reason/time persist. |
| TC-TEST-018-13 | Refund authorization | Students and teachers cannot refund payments. |
| TC-TEST-018-14 | Refund validation | Overlong refund reason returns `400 Bad Request`. |
| TC-TEST-018-15 | Unauthenticated access | Protected endpoints return 401. |
| TC-TEST-018-16 | Student authorization | Student cannot access administrator endpoints. |
| TC-TEST-018-17 | Teacher authorization | Teacher cannot access payment endpoints not granted to that role. |
| TC-TEST-018-18 | Invalid payment input | Missing/invalid course and payment IDs are handled safely. |
| TC-TEST-018-19 | Webhook signature | Missing signature is rejected; full webhook tests require deterministic fixtures. |
| TC-TEST-018-23 | Successful payment verification endpoint | Admin can verify arbitrary student/course pairs; teacher denied; anonymous caller rejected. |
| TC-TEST-018-24 | Invalid webhook signature | Webhook rejects absent and malformed Stripe signatures with a client error. |
| TC-TEST-018-25 | Signed unsupported webhook replay | With an explicitly configured test secret, a valid synthetic unsupported event is accepted and replayed without changing payment state. |
| TC-TEST-018-26 | Invalid payment input | Missing/malformed course IDs return `400`; unknown student payment detail/status return `404`. |
| TC-TEST-018-27 | Invalid admin filters | Reversed date range, unknown status, and malformed student UUID return `400`. |
| TC-TEST-018-28 | Refund fixture isolation | Refund workflow only touches the explicitly configured one-time `TEST_PAYMENT_REFUND_ID`. |
| TC-TEST-018-29 | Unknown admin payment operations | Admin detail and refund return `404` for an unknown payment ID. |
| TC-TEST-018-30 | Successful signed webhook transition | A gated signed `payment_intent.succeeded` event transitions one dedicated pending payment to success and safely ignores duplicate delivery. |
| TC-TEST-018-31 | Failed signed webhook transition | A gated signed `payment_intent.payment_failed` event transitions a separate dedicated pending payment to failed and safely ignores duplicate delivery. |
| TC-TEST-018-32 | Unknown course payment initiation | A syntactically valid but unknown course ID returns `404` before a payment record is created or Stripe is invoked. |

## Manual safeguards

- **TC-TEST-018-20 — Provider safety:** Confirm payment tests use test-mode/stub infrastructure and synthetic data; never use a live provider account.
- **TC-TEST-018-21 — Security review:** Review that secrets and provider credentials do not appear in source, reports, or logs.
- **TC-TEST-018-22 — Documentation review:** Recheck configuration, ownership, refund fixtures, cleanup, and execution rules when payment tests change.

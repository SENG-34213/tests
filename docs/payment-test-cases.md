# TEST-018 payment test cases

| ID | Scenario | Expected result |
| --- | --- | --- |
| TC-TEST-018-01 | Required-service readiness | Gateway, Keycloak, Course, Enrollment, and Payment services are checked. |
| TC-TEST-018-02 | Start course payment | Authenticated student receives safe payment-initiation data. |
| TC-TEST-018-03 | Student payment list | Student receives only permitted payment summaries. |
| TC-TEST-018-04 | Student payment detail | Owned payment details are returned. |
| TC-TEST-018-05 | Student payment status | Status matches the owned payment. |
| TC-TEST-018-06 | Payment ownership | Student cannot access another student’s payment. |
| TC-TEST-018-07 | Admin payment list | Administrator receives paginated payment data. |
| TC-TEST-018-08 | Admin filters | Status, student, course, date, and reference filters are supported. |
| TC-TEST-018-09 | Admin pagination | Page and size query parameters are accepted and validated. |
| TC-TEST-018-10 | Admin payment detail | Administrator can inspect documented payment fields only. |
| TC-TEST-018-11 | Successful verification | Requested student/course payment verification is returned. |
| TC-TEST-018-12 | Refund | Approved test payment can be refunded by an administrator. |
| TC-TEST-018-13 | Refund authorization | Students and teachers cannot refund payments. |
| TC-TEST-018-14 | Refund validation | Overlong refund reasons are rejected without a server error. |
| TC-TEST-018-15 | Unauthenticated access | Protected endpoints return 401. |
| TC-TEST-018-16 | Student authorization | Student cannot access administrator endpoints. |
| TC-TEST-018-17 | Teacher authorization | Teacher cannot access payment endpoints not granted to that role. |
| TC-TEST-018-18 | Invalid payment input | Missing/invalid course and payment IDs are handled safely. |
| TC-TEST-018-19 | Webhook signature | Missing signature is rejected; full webhook tests require deterministic fixtures. |
| TC-TEST-018-20 | Provider safety | Tests use only test-mode/stub infrastructure and synthetic data. |
| TC-TEST-018-21 | Security | Secrets and provider credentials never appear in source or logs. |
| TC-TEST-018-22 | Documentation | Configuration, ownership, refund, cleanup, and execution rules are documented. |

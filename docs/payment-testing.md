# Payment Service integration testing

TEST-018 verifies Payment Service access through the API Gateway using real authentication tokens and safe,
non-production fixtures.

## Scope

Coverage includes course-payment initiation, student payment history/detail/status, administrator listing and
filters, pagination, administrator detail, successful-payment verification, refund authorization and validation,
ownership protection, authentication, and invalid identifiers. Stripe webhook tests are documented as optional and
must be skipped unless deterministic test fixtures and a safe signature are available.

Frontend payment pages, direct database access, production Stripe, real cards, real financial data, load testing,
and unrelated service workflows are out of scope.

## Configuration

```properties
TEST_ENV_ENABLED=true
TEST_REQUIRED_SERVICES=API_GATEWAY,KEYCLOAK,COURSE_SERVICE,ENROLLMENT_SERVICE,PAYMENT_SERVICE
PAYMENT_SERVICE_BASE_URL=http://localhost:8090
TEST_PAYMENT_COURSE_ID=<approved-course-uuid>
TEST_PAYMENT_STUDENT_ID=<approved-student-uuid>
TEST_PAYMENT_SECOND_STUDENT_ID=<optional-second-student-uuid>
TEST_PAYMENT_REFUND_ID=<one-time-successful-test-payment-uuid>
TEST_PAYMENT_WRITE_ENABLED=false
TEST_PAYMENT_WEBHOOK_SUCCESS_ID=<one-time-pending-test-payment-uuid>
TEST_PAYMENT_WEBHOOK_FAILURE_ID=<one-time-pending-test-payment-uuid>
TEST_PAYMENT_WEBHOOK_WRITE_ENABLED=false
PAYMENT_PROVIDER_TEST_MODE=true
TEST_STRIPE_WEBHOOK_TEST_ENABLED=false
TEST_STRIPE_WEBHOOK_SECRET=<same-test-only-secret-configured-in-payment-service>
```

Use the existing student, optional `SECONDARY_STUDENT_TEST_USERNAME/PASSWORD`, administrator, and teacher credential
variables. The secondary student must match `TEST_PAYMENT_SECOND_STUDENT_ID` for cross-user tests. Never commit
passwords, tokens, Stripe keys, webhook secrets, card numbers, or real student information. Payment writes and refunds
remain disabled unless an approved test-mode provider or deterministic stub is configured.
The success-webhook fixture is separately gated: it must be a dedicated disposable `PENDING` payment, and the test
consumes it by transitioning it to `SUCCESS` (which may publish a payment notification).

## Safety and cleanup

Payment tests must not create uncontrolled duplicate payments. Write tests generate no production transactions and
use only approved test courses/accounts. Refunds require the exact `TEST_PAYMENT_REFUND_ID` for a dedicated `SUCCESS`
payment owned by the configured student in the configured course. Refunding consumes the fixture, so it may be used
only once and must not be shared. Refund tests are skipped when no such fixture exists. The Payment Service has no
public delete endpoint, so payment records are never deleted. Provider secrets and authorization headers must not
appear in logs or failure messages.

## Webhooks

The backend requires a `Stripe-Signature` header. Missing and malformed signatures are rejected by default. A separate
opt-in check signs a synthetic, unsupported PaymentIntent event with `TEST_STRIPE_WEBHOOK_SECRET`, verifies acceptance,
and replays the same event ID without changing a payment. A separate one-time success transition test requires
`TEST_PAYMENT_WEBHOOK_WRITE_ENABLED=true`, the dedicated pending payment ID, and the same configured non-production
webhook secret; success and failure tests independently verify `PENDING -> SUCCESS` or `PENDING -> FAILED` and duplicate
delivery. Each event consumes its own fixture; replenish each before rerunning. Never point these tests at production
Stripe or payments.

## Running

```powershell
mvn test
mvn verify -Pintegration
mvn -Pintegration verify -Dtest="Payment*IT"
```

The normal test phase runs `PaymentClientTest`. Payment integration classes use the existing `*IT` convention and
run through the integration profile after readiness checks for API Gateway, Keycloak, Course, Enrollment, and Payment
Service.

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
TEST_PAYMENT_WRITE_ENABLED=false
PAYMENT_PROVIDER_TEST_MODE=true
```

Use the existing student, administrator, and teacher credential variables. Never commit passwords, tokens, Stripe
keys, webhook secrets, card numbers, or real student information. Payment writes and refunds remain disabled unless
an approved test-mode provider or deterministic stub is configured.

## Safety and cleanup

Payment tests must not create uncontrolled duplicate payments. Write tests generate no production transactions and
use only approved test courses/accounts. Refunds are performed only against a safe test payment and may be skipped
when no eligible fixture exists. The Payment Service has no public delete endpoint, so shared payment records are
never deleted. Provider secrets and authorization headers must not appear in logs or failure messages.

## Webhooks

The backend requires a `Stripe-Signature` header. Tests only verify missing-signature rejection by default. Valid,
invalid, duplicate, and malformed webhook scenarios require deterministic test-mode fixtures and a test webhook secret;
they must be explicitly enabled and documented before execution.

## Running

```powershell
mvn test
mvn verify -Pintegration
mvn -Pintegration verify -Dtest="Payment*IT"
```

The normal test phase runs `PaymentClientTest`. Payment integration classes use the existing `*IT` convention and
run through the integration profile after readiness checks for API Gateway, Keycloak, Course, Enrollment, and Payment
Service.

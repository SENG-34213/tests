# Cross-service end-to-end testing

TEST-022 adds a small gateway-based verification layer on top of the service-specific integration suites. Unlike a
service test, an end-to-end test uses the API Gateway URL and verifies authentication, routing, downstream token
propagation, authorization, and cross-service identifiers as one journey.

## Configuration

```properties
TEST_ENV_ENABLED=true
TEST_E2E_ENABLED=true
TEST_REQUIRED_SERVICES=API_GATEWAY,AUTH_SERVICE,USER_SERVICE
TEST_E2E_COURSE_ID=<approved-course-uuid>
TEST_E2E_PROVIDER_JOURNEYS_ENABLED=false
```

The gateway URL comes from `API_GATEWAY_BASE_URL` (default `http://localhost:8080`). User, Course, and Enrollment
clients in the end-to-end support all use the gateway base URL; internal service ports are not used for journey
assertions. Add Course and Enrollment to `TEST_REQUIRED_SERVICES` when the course journey is enabled.

## Journeys

- Gateway authentication to User Service current-user data.
- Student/teacher/admin role preservation across gateway calls.
- Course lookup and enrollment consistency for the authenticated student: the test reads the student's ID from
  User Service, confirms the configured course ID returned by Course Service, checks that the student's own active
  course list contains that ID, and confirms the same student/course pair through Enrollment Service.
- Payment-notification and learning-provider journeys only when deterministic fixtures are explicitly enabled.
- Missing-token and downstream authorization/error behavior.

Provider journeys are skipped by default. They must use payment test mode, synthetic notification events, and a safe AI
provider; production credentials, cards, personal data, and direct database assertions are prohibited. The student
course journey uses only student-authorized endpoints; administrator-wide enrollment listings and teacher-only
course roster endpoints are not valid substitutes for the student's own enrollment view.

## Readiness and skip behavior

Every live class first requires `TEST_E2E_ENABLED=true`, then reuses the existing readiness client. Missing services
produce a clear assumption skip or readiness message instead of an unexplained endpoint assertion. Static unit tests
remain runnable without any backend services.

## Running locally

```powershell
mvn test
mvn verify -Pintegration
$env:TEST_ENV_ENABLED = "true"
$env:TEST_E2E_ENABLED = "true"
$env:TEST_REQUIRED_SERVICES = "API_GATEWAY,AUTH_SERVICE,USER_SERVICE,COURSE_SERVICE,ENROLLMENT_SERVICE"
mvn -Pintegration verify -Dtest="*Gateway*IT,*JourneyIT,*CrossService*IT"
```

Do not print environment variables containing credentials or tokens. Failsafe reports are written under
`target/failsafe-reports`.

# Cross-service end-to-end testing

TEST-022 adds a small gateway-based verification layer on top of the service-specific integration suites. Unlike a
service test, an end-to-end test uses the API Gateway URL and verifies authentication, routing, downstream token
propagation, authorization, and cross-service identifiers as one journey.

## Configuration

```properties
TEST_ENV_ENABLED=true
TEST_E2E_ENABLED=true
TEST_REQUIRED_SERVICES=API_GATEWAY,AUTH_SERVICE,USER_SERVICE,COURSE_SERVICE,ENROLLMENT_SERVICE,ASSIGNMENT_SERVICE,MATERIAL_SERVICE,PAYMENT_SERVICE,NOTIFICATION_SERVICE
TEST_E2E_COURSE_ID=<approved-course-uuid>
TEST_E2E_PROVIDER_JOURNEYS_ENABLED=false
TEST_E2E_WRITE_ENABLED=false
TEST_E2E_COURSE_GRADE_ID=<non-production-grade-uuid>
```

The gateway URL comes from `API_GATEWAY_BASE_URL` (default `http://localhost:8080`). All journey clients use the
gateway base URL; internal service ports are not used for journey assertions. `TEST_E2E_WRITE_ENABLED` is a separate
explicit opt-in for the complete synthetic workflow and requires a non-production grade ID and teacher, student, and
administrator test credentials. That workflow creates a course, enrollment, module, lesson, uploads and downloads a
material, creates an assignment/submission/grade, and creates a quiz/attempt/result, then attempts API cleanup.
Assignment deletion is a soft delete;
the backend has no submission-delete route, so the synthetic graded submission remains associated with the archived
assignment. Use a disposable non-production environment. Do not run it against production.

## Journeys

- Gateway authentication to User Service current-user data.
- Student/teacher/admin role preservation across gateway calls.
- Course lookup and enrollment identifier consistency with an approved course fixture.
- Complete teacher-created course, student self-enrollment/approval, lesson/material access, assignment submission and
  grading, quiz attempt/result, and administrator user/course/payment/notification reads.
- Payment-notification and learning-provider journeys only when deterministic fixtures are explicitly enabled.
- Missing-token and downstream authorization/error behavior.

Write journeys and provider journeys are skipped by default. The full write journey only uses synthetic test data and
does not initiate a payment; payment coverage in that journey is the administrator's read-only payment-list access.
Provider journeys must use payment test mode, synthetic notification events, and a safe AI provider; production
credentials, cards, personal data, and direct database assertions are prohibited.

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
$env:TEST_REQUIRED_SERVICES = "API_GATEWAY,AUTH_SERVICE,USER_SERVICE,COURSE_SERVICE,ENROLLMENT_SERVICE,ASSIGNMENT_SERVICE,MATERIAL_SERVICE,PAYMENT_SERVICE,NOTIFICATION_SERVICE"
mvn -Pintegration verify "-Dit.test=*Gateway*IT,*JourneyIT,*CrossService*IT,*CompleteWorkflowIT"
```

To execute the full write journey, also set `TEST_E2E_WRITE_ENABLED=true`, `TEST_E2E_COURSE_GRADE_ID`,
`STUDENT_TEST_USERNAME`/`STUDENT_TEST_PASSWORD`, `TEACHER_TEST_USERNAME`/`TEACHER_TEST_PASSWORD`, and
`ADMIN_TEST_USERNAME`/`ADMIN_TEST_PASSWORD`. Configure readiness for Assignment, Material, Payment, and
Notification services. The test uses the application's Auth Service JWT login; it does not use Keycloak.

## Frontend development-branch contract note

The frontend `development` branch currently submits `{ "courseId": "..." }` to `POST /api/v1/enrollments/me`.
The backend request contract and API integration suite use `{ "courseIds": ["..."] }`. The Step 10 API journey
uses the backend's current request contract; browser-level validation remains blocked until the frontend and backend
agree on the payload.

Do not print environment variables containing credentials or tokens. Failsafe reports are written under
`target/failsafe-reports`.

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
TEST_E2E_MODULE_ID=<student-course-module-uuid>
TEST_E2E_LESSON_ID=<student-course-lesson-uuid>
TEST_E2E_ASSIGNMENT_ID=<student-course-assignment-uuid>
TEST_E2E_QUIZ_ID=<published-student-course-quiz-uuid>
TEST_E2E_GRADED_SUBMISSION_ID=<graded-submission-owned-by-student>
TEST_E2E_TEACHER_COURSE_ID=<course-owned-by-teacher>
TEST_E2E_TEACHER_MODULE_ID=<teacher-course-module-uuid>
TEST_E2E_TEACHER_LESSON_ID=<teacher-course-lesson-uuid>
TEST_E2E_TEACHER_MATERIAL_ID=<teacher-course-material-uuid>
TEST_E2E_TEACHER_ASSIGNMENT_ID=<teacher-course-assignment-uuid>
TEST_E2E_TEACHER_SUBMISSION_ID=<graded-submission-for-teacher-course>
TEST_E2E_PROVIDER_JOURNEYS_ENABLED=false
```

The gateway URL comes from `API_GATEWAY_BASE_URL` (default `http://localhost:8080`). User, Course, and Enrollment
clients in the end-to-end support all use the gateway base URL; internal service ports are not used for journey
assertions. Add Course and Enrollment to `TEST_REQUIRED_SERVICES` when the course journey is enabled.

## Journeys

- Gateway authentication to User Service current-user data.
- Student/teacher/admin role preservation across gateway calls.
- Course lookup and enrollment identifier consistency with an approved course fixture.
- Student course learning journey: current identity, active enrollment, lesson, assignment, published quiz, and the
  student's graded submission. It verifies that the same course and assignment identifiers flow across services.
- Teacher course journey: an owned course, lesson, material, assignment, and persisted grade on its submission.
- Admin operations journey: user directory, course list, payment administration, and authenticated notification access.
- Payment-notification and learning-provider journeys only when deterministic fixtures are explicitly enabled.
- Missing-token and downstream authorization/error behavior.

The Student and Teacher journeys use approved, existing non-production fixtures and issue read-only requests. They do
not create or grade records. Resource creation, enrollment changes, quiz attempts, and grading mutations remain
covered by the service-level workflow tests, which have their own explicit write gates and cleanup rules.

Provider journeys are skipped by default. They must use payment test mode, synthetic notification events, and a safe AI
provider; production credentials, cards, personal data, and direct database assertions are prohibited.

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

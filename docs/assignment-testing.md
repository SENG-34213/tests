# Assignment Service integration testing

TEST-011 covers the Assignment Service through the public API Gateway. The backend module is
`services/assignment-service`, with a default direct service URL of `http://localhost:8084`.

## Covered API

- Assignment lifecycle: create, retrieve, course listing, full update, partial PATCH,
  publish, close, and delete.
- Student assignment dashboard: enrolled-course filtering, draft exclusion, pagination,
  and invalid page/date-range rejection.
- Submission lifecycle: create draft, update draft, submit, retrieve, list own submissions, and
  list submissions for an assignment.
- Grading: grade and return a submission.
- Security and validation: unauthenticated requests, role-protected operations, malformed IDs, and
  invalid request bodies.

The user-facing paths include `/api/v1/assignments/me`, `/api/v1/assignments`,
`/api/v1/courses/{courseId}/assignments`, `/api/v1/assignments/{assignmentId}/submissions`,
`/api/v1/submissions`, and `/api/v1/submissions/{submissionId}/grade|return`.

## Roles and dependencies

Assignment creation and management require `TEACHER`, `ADMIN`, or `SUPER_ADMIN`. Students create,
update, submit, and list their own submissions. Assignment and submission reads are available to
the roles defined by the backend controller. Live tests require API Gateway, Auth Service, Assignment
Service, Course Service, Enrollment Service, and Material Service for attachment/file workflows.

## Fixtures and safety

Set `TEST_ASSIGNMENT_COURSE_ID`, `TEST_ASSIGNMENT_MODULE_ID`, `TEST_ASSIGNMENT_STUDENT_ID`, and
`TEST_ASSIGNMENT_ID` to approved synthetic fixtures. The configured student ID must match the
student credential used by the integration test and be enrolled in the configured course.
TEST_ASSIGNMENT_LATE_SUBMISSION_ID must identify that student's draft submission on a published
assignment whose due date has passed and whose late-submission setting is disabled.
TEST_ASSIGNMENT_ATTEMPT_LIMIT_ID must identify a published assignment where the configured
student already has at least the allowed number of active attempts. These two boundary tests are
read-only when the backend rejects the request. The intended response is HTTP 409. The development
snapshot returned HTTP 500 for these rules; the companion backend branch
`feature/TEST-034-step6-backend-contract` now maps these conflicts to 409 and its Assignment
service suite passes. Live gateway verification still requires the configured fixture IDs and
credentials.
Assignment and submission writes are disabled unless `TEST_ASSIGNMENT_WRITE_ENABLED=true`. Write
tests use unique test data and delete assignments they create. Submissions have no deletion endpoint,
so a dedicated test fixture or documented cleanup process is required. Attachment-link and
multipart-upload workflows require `TEST_ATTACHMENT_WRITE_ENABLED=true` and approved assignment,
submission, course, and Material fixtures. They remove only test-created attachment/file links and
their associated test-created Material records; fixture assignments, submissions, and materials are
preserved.

Never commit credentials, tokens, provider secrets, or production data. Authentication credentials
are read from the existing role-specific environment variables.

## Verification

```powershell
mvn clean verify
$env:TEST_ENV_ENABLED = "true"
$env:TEST_REQUIRED_SERVICES = "API_GATEWAY,AUTH_SERVICE,COURSE_SERVICE,ENROLLMENT_SERVICE,ASSIGNMENT_SERVICE,MATERIAL_SERVICE"
mvn verify -Pintegration -Dtest="Assignment*IT,Submission*IT"
mvn verify -Pintegration
$env:TEST_ENV_ENABLED = "false"
mvn verify -Pintegration -Dtest="Assignment*IT,Submission*IT"
```

Without live services, integration tests stop at readiness or are skipped by missing fixture
assumptions; the normal unit suite remains runnable with `mvn test`.

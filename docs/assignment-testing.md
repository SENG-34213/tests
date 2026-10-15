# Assignment Service integration testing

TEST-011 covers the Assignment Service through the public API Gateway. The backend module is
`services/assignment-service`, with a default direct service URL of `http://localhost:8084`.

## Covered API

- Assignment lifecycle: create, retrieve, course listing, update, publish, close, and delete.
- Submission lifecycle: create draft, update draft, submit, retrieve, list own submissions, and
  list submissions for an assignment.
- Grading: grade and return a submission.
- Security and validation: unauthenticated requests, role-protected operations, malformed IDs, and
  invalid request bodies.

The public paths are `/api/v1/assignments`, `/api/v1/courses/{courseId}/assignments`,
`/api/v1/assignments/{assignmentId}/submissions`, `/api/v1/submissions`, and
`/api/v1/submissions/{submissionId}/grade|return`.

## Roles and dependencies

Assignment creation and management require `TEACHER`, `ADMIN`, or `SUPER_ADMIN`. Students create,
update, submit, and list their own submissions. Assignment and submission reads are available to
the roles defined by the backend controller. Live tests require API Gateway, Keycloak, Assignment
Service, Course Service, and Enrollment Service.

## Fixtures and safety

Set `TEST_ASSIGNMENT_COURSE_ID`, `TEST_ASSIGNMENT_MODULE_ID`, `TEST_ASSIGNMENT_STUDENT_ID`, and
`TEST_ASSIGNMENT_ID` to approved synthetic fixtures. Assignment and submission writes are disabled
unless `TEST_ASSIGNMENT_WRITE_ENABLED=true`. Write tests use unique test data and delete assignments
they create. Submissions have no deletion endpoint, so a dedicated test fixture or documented
cleanup process is required. File attachments and storage are outside TEST-011.

Never commit credentials, tokens, provider secrets, or production data. Authentication credentials
are read from the existing role-specific environment variables.

## Verification

```powershell
mvn clean verify
$env:TEST_ENV_ENABLED = "true"
$env:TEST_REQUIRED_SERVICES = "API_GATEWAY,KEYCLOAK,COURSE_SERVICE,ENROLLMENT_SERVICE,ASSIGNMENT_SERVICE"
mvn verify -Pintegration -Dtest="Assignment*IT,Submission*IT"
mvn verify -Pintegration
$env:TEST_ENV_ENABLED = "false"
mvn verify -Pintegration -Dtest="Assignment*IT,Submission*IT"
```

Without live services, integration tests stop at readiness or are skipped by missing fixture
assumptions; the normal unit suite remains runnable with `mvn test`.

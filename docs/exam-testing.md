# Exam Service integration testing

TEST-017 verifies the Exam Service through the API Gateway with application-issued JWT access tokens.

## Scope

The tests cover exam creation, retrieval, course listing, update, scheduling, cancellation, marking, batch marks,
completion, result sheets, summaries, student result retrieval, role authorization, validation, not-found handling,
and lifecycle transitions. Direct database checks, backend unit tests, frontend tests, load testing, payment,
notification, material, attendance, and communication workflows are outside this ticket.

## Required environment

The live integration profile requires API Gateway, Auth Service, Course Service, Enrollment Service, Exam Service,
Material Service (for attachment upload/download), a teacher or administrator account, and an enrolled student
fixture. Use the existing role credential variables and set:

```properties
TEST_ENV_ENABLED=true
TEST_REQUIRED_SERVICES=API_GATEWAY,AUTH_SERVICE,COURSE_SERVICE,ENROLLMENT_SERVICE,EXAM_SERVICE,MATERIAL_SERVICE
EXAM_SERVICE_BASE_URL=http://localhost:8089
TEST_EXAM_COURSE_ID=<approved-course-uuid>
TEST_EXAM_STUDENT_ID=<approved-enrolled-student-uuid>
TEST_EXAM_WRITE_ENABLED=false
```

The student account used by the lifecycle test must correspond to
`TEST_EXAM_STUDENT_ID` and be enrolled in `TEST_EXAM_COURSE_ID`; scheduled-exam
detail and attachment visibility checks rely on this identity match.

Never commit real credentials, tokens, or production identifiers. Set `TEST_EXAM_WRITE_ENABLED=true` only for an
approved non-production environment. The write workflow generates a unique exam title and leaves cleanup limited to
the backend operations that are safely supported; no exam-delete endpoint exists in the public contract.

## Lifecycle assumptions

The supported management sequence is `DRAFT`, `SCHEDULED`, `MARKING`, and `COMPLETED`. Cancellation is tested as a
separate terminal transition for an eligible exam. Marks are submitted as a non-empty `results` list containing
`studentId`, `attendanceStatus`, `marksObtained`, and optional `remarks`. The API rejects negative marks and values
with more than two decimal places.

## Running

```powershell
mvn test
mvn verify -Pintegration
mvn -Pintegration verify -Dtest="Exam*IntegrationTest,Exam*IT"
```

The normal test phase runs the client unit tests. Exam integration classes run with the integration profile and
require the services and credentials above. When integration is disabled, the existing readiness gate stops with a
clear configuration error before making API calls.

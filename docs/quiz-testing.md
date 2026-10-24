# Quiz and Assessment Integration Testing

TEST-014 covers the Assignment Service quiz API through the public API gateway. It does not copy or modify backend
source code and does not use direct database access.

## Endpoints and roles

- Teacher/admin management: `POST /api/quizzes`, `PUT/GET/DELETE /api/quizzes/{quizId}`,
  `GET /api/quizzes/teacher`, and `POST /api/quizzes/{quizId}/publish`.
- Teacher question management: `POST/PUT/DELETE /api/quizzes/{quizId}/questions`.
- Student access: `GET /api/courses/{courseId}/quizzes` and `GET /api/quizzes/{quizId}/student`.
- Student attempts: `POST /api/quizzes/{quizId}/attempts`, `PUT /api/quiz-attempts/{attemptId}/answers`,
  and `POST /api/quiz-attempts/{attemptId}/submit`.
- Teacher results and gradebook: `GET /api/quizzes/{quizId}/results` and
  `GET /api/v1/teacher/gradebook/quizzes?courseId=…`.
- AI draft validation: `POST /api/assignments/quizzes/ai/generate` rejects
  invalid settings before the upstream AI provider is called; successful generation remains provider-gated.
- Attempt workflow checks verify the student-safe question projection, answer persistence, score/percentage/pass
  calculation, empty-attempt scoring, active-attempt exclusion, submitted-attempt immutability, and configured
  maximum attempts.

Quiz requests require `courseId`, `title`, `durationMinutes`, `passMark`, and `maxAttempts`. Question requests
require `questionText`, `questionType` (`MULTIPLE_CHOICE_SINGLE_ANSWER`), `marks`, `questionOrder`, and four or five options. Each option uses
`optionText`, `correct`, and `optionOrder`. Answers use `questionId` and `selectedOptionId`.

## Environment

Default local values are in `.env.example`:

```text
TEST_ENV_ENABLED=false
TEST_REQUIRED_SERVICES=API_GATEWAY,AUTH_SERVICE,COURSE_SERVICE,ENROLLMENT_SERVICE,ASSIGNMENT_SERVICE,USER_SERVICE
ASSIGNMENT_SERVICE_BASE_URL=http://localhost:8084
COURSE_SERVICE_BASE_URL=http://localhost:8082
ENROLLMENT_SERVICE_BASE_URL=http://localhost:8083
TEST_QUIZ_COURSE_ID=<approved-synthetic-course-uuid>
TEST_QUIZ_WRITE_ENABLED=false
TEST_QUIZ_GRADEBOOK_COURSE_ID=<course-with-closed-quiz-uuid>
TEST_QUIZ_CLOSED_ID=<closed-quiz-uuid>
```

Credentials are read through the existing `STUDENT_TEST_*`, `TEACHER_TEST_*`, and `ADMIN_TEST_*` environment
variables. Never commit credentials, access tokens, real quiz records, correct-answer data, or student data.

The integration support verifies API Gateway, Auth Service, Assignment Service, Course Service, and Enrollment Service
before making API requests. The teacher results workflow also requires User Service to resolve submitted student
names; that extra dependency is readiness-checked only when that provider-backed results workflow runs. With
`TEST_ENV_ENABLED=false`, readiness fails before a live request is issued, which is the expected safety behavior for
integration execution.

## Isolation and cleanup

Quiz titles contain a UUID so parallel or repeated runs do not intentionally collide. Test-created IDs are held in
memory. Cleanup deletes only the quiz created by the test and never uses a shared fixture as a deletion target.
Attempt-limit scenarios should use a dedicated student or dedicated quiz because active attempts are stateful.
Published quizzes may have service-specific deletion rules; any cleanup limitation must be recorded in the test run.

## Commands

Run unit tests and static analysis:

```powershell
mvn clean verify
```

Run quiz integration tests in a configured environment:

```powershell
$env:TEST_ENV_ENABLED="true"
$env:TEST_REQUIRED_SERVICES="API_GATEWAY,AUTH_SERVICE,COURSE_SERVICE,ENROLLMENT_SERVICE,ASSIGNMENT_SERVICE"
mvn verify -Pintegration -Dtest="Quiz*IT"
```

Run the safety check with live testing disabled:

```powershell
$env:TEST_ENV_ENABLED="false"
mvn verify -Pintegration -Dtest="Quiz*IT"
```

The disabled command is expected to stop at readiness and may return non-zero because the environment is intentionally
disabled. Service accounts, course fixtures, and an enrolled student must be supplied by the test environment; the
repository does not provision application users or test data.

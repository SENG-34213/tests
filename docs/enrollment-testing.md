# Enrollment Service Testing

TEST-010 adds Enrollment Service integration coverage using the backend `development` contract. The backend module is intentionally named `entrollment-service`, while its public API uses `/api/v1/enrollments`.

## Endpoints and roles

- `GET /api/v1/enrollments` — `ADMIN`, `SUPER_ADMIN`
- `POST /api/v1/enrollments` — `ADMIN`, `SUPER_ADMIN`
- `POST /api/v1/enrollments/me` — `STUDENT`
- `PUT /api/v1/enrollments/{enrollmentId}` — `ADMIN`, `SUPER_ADMIN`
- `GET /api/v1/enrollments/students/{studentId}/courses/{courseId}/exists` — `STUDENT`, `TEACHER`, `ADMIN`, `SUPER_ADMIN`
- `GET /api/v1/enrollments/students/{studentId}/courses` — current `STUDENT` only
- `GET /api/v1/enrollments/courses/{courseId}/students` — `TEACHER`, `ADMIN`, `SUPER_ADMIN`
- `GET /api/v1/enrollments/students/unenrolled` — `ADMIN`, `SUPER_ADMIN`

The tests use the public API only. No backend source code or database access is included.

## Environment

The normal integration environment must include:

```powershell
$env:TEST_ENV_ENABLED="true"
$env:TEST_REQUIRED_SERVICES="API_GATEWAY,KEYCLOAK,COURSE_SERVICE,ENROLLMENT_SERVICE"
$env:ENROLLMENT_SERVICE_BASE_URL="http://localhost:8083"
```

Role credentials are supplied through `STUDENT_TEST_USERNAME/PASSWORD`, `TEACHER_TEST_USERNAME/PASSWORD`, and `ADMIN_TEST_USERNAME/PASSWORD`. Read-only fixture tests additionally require approved non-production values for `TEST_ENROLLMENT_STUDENT_ID`, `TEST_ENROLLMENT_OTHER_STUDENT_ID`, and `TEST_ENROLLMENT_COURSE_ID`.

## Write-test safety

The service has no delete endpoint. Therefore self-enrollment and administrator create/update tests are disabled unless `TEST_ENROLLMENT_WRITE_ENABLED=true` and an alternate approved course fixture is configured. Created IDs are kept in memory only. Because the API does not support deletion, the documentation and test output must identify any controlled fixture cleanup that must be performed by the environment owner; tests never delete records they did not create.

Do not commit credentials, tokens, production IDs, or production URLs. Do not enable write tests against production or shared uncontrolled data.

## Verification

Unit and static checks:

```powershell
mvn clean verify
```

Enrollment integration tests:

```powershell
mvn verify -Pintegration -Dtest="Enrollment*IT"
```

Complete integration suite:

```powershell
mvn verify -Pintegration
```

Disabled-environment safety check:

```powershell
$env:TEST_ENV_ENABLED="false"
mvn verify -Pintegration -Dtest="Enrollment*IT"
```

Readiness must stop execution before API requests when the environment is disabled or a required service is unavailable. The disabled command may return non-zero by design.

## Out of scope

Payment, notification, Kafka, communication, account provisioning, database cleanup automation, performance, load, penetration, staging, and production testing remain outside TEST-010.

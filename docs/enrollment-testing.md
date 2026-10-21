# Enrollment Service Testing

TEST-010 adds Enrollment Service integration coverage using the backend `development` contract. The backend module is intentionally named `entrollment-service`, while its public API uses `/api/v1/enrollments`.

## Endpoints and roles

- `GET /api/v1/enrollments` — `ADMIN`, `SUPER_ADMIN`
- `POST /api/v1/enrollments` — `TEACHER`, `ADMIN`, `SUPER_ADMIN`
- `POST /api/v1/enrollments/me` — `STUDENT`
- `PUT /api/v1/enrollments/{enrollmentId}` — `TEACHER`, `ADMIN`, `SUPER_ADMIN`
- `DELETE /api/v1/enrollments/{enrollmentId}` — `TEACHER`, `ADMIN`, `SUPER_ADMIN` (soft delete)
- `GET /api/v1/enrollments/students/{studentId}/courses/{courseId}/exists` — `STUDENT`, `TEACHER`, `ADMIN`, `SUPER_ADMIN`
- `GET /api/v1/enrollments/students/{studentId}/courses` — current `STUDENT` only
- `GET /api/v1/enrollments/courses/{courseId}/students` — `TEACHER`, `ADMIN`, `SUPER_ADMIN`
- `GET /api/v1/enrollments/courses/{courseId}` — `TEACHER`, `ADMIN`, `SUPER_ADMIN`
- `GET /api/v1/enrollments/students/unenrolled` — `ADMIN`, `SUPER_ADMIN`

The tests use the public API only. No backend source code or database access is included.

## Environment

The normal integration environment must include:

```powershell
$env:TEST_ENV_ENABLED="true"
$env:TEST_REQUIRED_SERVICES="API_GATEWAY,KEYCLOAK,COURSE_SERVICE,ENROLLMENT_SERVICE,COMMUNICATION_SERVICE"
$env:ENROLLMENT_SERVICE_BASE_URL="http://localhost:8083"
```

Role credentials are supplied through `STUDENT_TEST_USERNAME/PASSWORD`, `TEACHER_TEST_USERNAME/PASSWORD`, `ADMIN_TEST_USERNAME/PASSWORD`, and Super Admin credentials for the full role matrix. Read-only fixtures require approved non-production values for `TEST_ENROLLMENT_STUDENT_ID`, `TEST_ENROLLMENT_OTHER_STUDENT_ID`, and `TEST_ENROLLMENT_COURSE_ID`. Admin write workflows use two alternate courses the test student is not enrolled in. Student self-enrollment uses `TEST_ENROLLMENT_FREE_COURSE_ID`, verified as free. Teacher ownership denial uses `TEST_ENROLLMENT_UNOWNED_COURSE_ID`, owned by a different teacher; `TEST_ENROLLMENT_ID` can identify an active enrollment there for update/delete denial checks.

## Write-test safety

Write tests are disabled unless `TEST_ENROLLMENT_WRITE_ENABLED=true` and approved non-production fixtures are configured. Lifecycle workflows track IDs returned by their own create calls and use the enrollment soft-delete endpoint in normal completion and failure cleanup. Tests never delete fixture records they did not create. Write workflows also depend on Course and Communication services because enrollment validates courses and synchronizes group membership. Use only courses without active communication groups for all write fixtures: enrollment deletion does not remove a student already synchronized to a communication group, and no public member-removal route is available for reliable cleanup.

Do not commit credentials, tokens, production IDs, or production URLs. Write tests require Course and Communication services. Do not enable write tests against production or shared uncontrolled data.

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

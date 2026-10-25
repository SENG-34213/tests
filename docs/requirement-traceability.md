# Requirement Traceability

This matrix maps development-guide requirements to backend behavior covered by
this repository. `Covered` means executable tests and a documented execution
path exist. `Partially Covered` means the client or contract coverage exists but
the complete live journey needs fixtures or infrastructure. `Skipped by
Environment` means the test is intentionally gated.

## Requirement matrix

| Requirement | Backend area | Test ticket | Test class or package | Status | Notes |
|---|---|---|---|---|---|
| Authentication and token propagation | Auth Service, API Gateway | TEST-009, TEST-021, TEST-022 | `auth/*`, `user/*`, `e2e/GatewaySmokeIT` | Covered | JWTs are issued by the application Auth Service; credentials are environment-only. |
| User profile and administration | User Service | TEST-021 | `user/*` | Covered | Live tests require User Service and test users. |
| Course authorization and lifecycle | Course Service | TEST-010 | `course/*` | Covered | Writes require approved non-production fixtures. |
| Enrollment access and workflows | Enrollment Service | TEST-011 | `enrollment/*`, `e2e/CourseEnrollmentJourneyIT` | Partially Covered | Cross-service journey requires `TEST_E2E_COURSE_ID`. |
| Assignment and submission behavior | Assignment Service | TEST-012 | `assignment/*` | Covered | Attachment and writes are fixture/flag gated. |
| Learning materials and attachments | Material Service | TEST-013 | `material/*`, `assignment/*Attachment*` | Covered | File operations require approved fixtures. |
| Quiz management and attempts | Quiz Service | TEST-014 | `quiz/*` | Covered | Includes fixture-gated cross-teacher read/update/delete denial for teacher-owned quizzes. |
| Attendance sessions and summaries | Attendance Service | TEST-015 | `attendance/*` | Covered | Includes fixture-gated denial when a teacher lists sessions for another teacher's course. |
| Communication groups and messages | Communication Service | TEST-016 | `communication/*` | Covered | Synthetic writes are opt-in. |
| Exam lifecycle and results | Exam Service | TEST-017 | `exam/*` | Covered | Requires approved course/student fixtures. |
| Payment authorization and safe flows | Payment Service | TEST-018 | `payment/*` | Partially Covered | Provider writes/refunds are disabled by default. |
| Notification ownership and validation | Notification Service | TEST-019 | `notification/*` | Covered | Email provider mode is disabled by default. |
| AI conversations and provider boundary | AI Service | TEST-020 | `ai/*` | Partially Covered | Provider calls require explicit test mode. |
| Cross-service workflow | API Gateway and core services | TEST-022 | `e2e/*` | Partially Covered | `CompleteWorkflowIT` implements student, teacher, and admin journeys; live execution still needs write opt-in, role credentials, a grade fixture, and all required services. The frontend enrollment payload mismatch is documented. |
| Service readiness and failure reporting | All configured services | TEST-002, TEST-022 | `environment/*` | Covered | Failures identify the missing service. |
| CI build, reports, and safe defaults | Maven and GitHub Actions | TEST-022, TEST-023 | `.github/workflows/test.yml` | Covered | Static CI needs no live services or secrets. |
| Final traceability and quality review | Test repository | TEST-023 | This document and related final documents | Covered | Reviewed during TEST-023. |
| Backend code-coverage and executed-register evidence | Backend CI and tests repository | TEST-027 | Backend JaCoCo artifacts, `tools/test-evidence.py`, `docs/test-case-mappings.csv` | Partially Covered | Mapping has 662 case rows targeting 404 class/method pairs, including gated notification Kafka and Mailpit workflows. CI rejects executed unmapped methods. Backend JaCoCo reports were not supplied; matching backend artifacts are required to measure product coverage. |
| Staging release-candidate smoke verification | API Gateway JWT endpoint, test repository CI | TEST-028 | `staging/StagingSmokeIT`, `.github/workflows/staging-smoke.yml`, `docs/staging-test-cases.md` | Partially Covered | Read-only probes and local policy tests exist; the protected GitHub Environment must be configured and an authorized live staging run must provide evidence. |

## Service inventory

| Service | Base URL variable | Main package | Roles | Live-test selector | Limitation |
|---|---|---|---|---|---|
| User | `USER_SERVICE_BASE_URL` | `user` | student, teacher, admin | `*User*IT` | Requires Auth Service/test users. |
| Course | `COURSE_SERVICE_BASE_URL` | `course` | student, teacher, admin | `*Course*IT` | Writes require fixtures. |
| Enrollment | `ENROLLMENT_SERVICE_BASE_URL` | `enrollment` | student, teacher, admin | `*Enrollment*IT` | Requires enrollment fixtures. |
| Assignment | `ASSIGNMENT_SERVICE_BASE_URL` | `assignment` | student, teacher, admin | `*Assignment*IT,*Submission*IT` | Writes/attachments default off. |
| Material | `MATERIAL_SERVICE_BASE_URL` | `material` | student, teacher, admin | `*Material*IT` | Requires approved IDs/files. |
| Calendar | `CALENDAR_SERVICE_BASE_URL` | `calendar` | authenticated users | `*Calendar*IT` | Requires Calendar readiness. |
| Quiz | service configuration | `quiz` | student, teacher, admin | `*Quiz*IT` | Writes/provider are gated. |
| Attendance | `ATTENDANCE_SERVICE_BASE_URL` | `attendance` | student, teacher, admin | `*Attendance*IT` | Requires fixtures. |
| Communication | `COMMUNICATION_SERVICE_BASE_URL` | `communication` | student, teacher, admin | `*Communication*IT` | Synthetic writes are opt-in. |
| Exam | `EXAM_SERVICE_BASE_URL` | `exam` | student, teacher, admin | `*Exam*IT` | Requires approved fixtures. |
| Payment | `PAYMENT_SERVICE_BASE_URL` | `payment` | student, admin | `*Payment*IT` | Provider mode/write flags off. |
| Notification | `NOTIFICATION_SERVICE_BASE_URL` | `notification` | student, admin | `*Notification*IT` | Email mode is opt-in. |
| AI | `AI_SERVICE_BASE_URL` | `ai` | authenticated users | `*Ai*IT` | Provider mode is opt-in. |
| API Gateway | `API_GATEWAY_BASE_URL` | `api`, `auth`, `e2e` | public/authenticated | `*Gateway*IT,*AuthorizationIT` | Public boundary. |
| Auth Service | `AUTH_SERVICE_BASE_URL` | `auth`, `environment` | configured users | `*Authentication*IT` | No credentials in source. |

## Verification

```powershell
mvn clean test
mvn verify -Pintegration
mvn -Dtest="*User*IT" verify -Pintegration
mvn -Dtest="*Gateway*IT,*JourneyIT" verify -Pintegration
```

Static CI runs with live flags disabled. Live commands are for approved
non-production environments only.

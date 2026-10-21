# Requirement Traceability

This matrix maps development-guide requirements to backend behavior covered by
this repository. `Covered` means executable tests and a documented execution
path exist. `Partially Covered` means the client or contract coverage exists but
the complete live journey needs fixtures or infrastructure. `Skipped by
Environment` means the test is intentionally gated.

## Requirement matrix

| Requirement | Backend area | Test ticket | Test class or package | Status | Notes |
|---|---|---|---|---|---|
| Authentication and token propagation | Keycloak, API Gateway | TEST-009, TEST-021, TEST-022 | `auth/*`, `user/*`, `e2e/GatewaySmokeIT` | Covered | Credentials are environment-only. |
| User profile and administration | User Service | TEST-021 | `user/*` | Covered | Live tests require User Service and test users. |
| Course authorization and lifecycle | Course Service | TEST-010 | `course/*` | Covered | Writes require approved non-production fixtures. |
| Enrollment access and workflows | Enrollment Service | TEST-011 | `enrollment/*`, `e2e/CourseEnrollmentJourneyIT` | Partially Covered | Cross-service journey requires `TEST_E2E_COURSE_ID`. |
| Assignment and submission behavior | Assignment Service | TEST-012 | `assignment/*` | Covered | Attachment and writes are fixture/flag gated. |
| Learning materials and attachments | Material Service | TEST-013 | `material/*`, `assignment/*Attachment*` | Covered | File operations require approved fixtures. |
| Quiz management and attempts | Quiz Service | TEST-014 | `quiz/*` | Covered | Writes are disabled unless explicitly enabled. |
| Attendance sessions and summaries | Attendance Service | TEST-015 | `attendance/*` | Covered | Live execution requires fixtures. |
| Communication groups and messages | Communication Service | TEST-016 | `communication/*` | Covered | Synthetic writes are opt-in. |
| Exam lifecycle and results | Exam Service | TEST-017 | `exam/*` | Covered | Requires approved course/student fixtures. |
| Payment authorization and safe flows | Payment Service | TEST-018 | `payment/*` | Partially Covered | Provider writes/refunds are disabled by default. |
| Notification ownership and validation | Notification Service | TEST-019 | `notification/*` | Covered | Email provider mode is disabled by default. |
| AI conversations and provider boundary | AI Service | TEST-020 | `ai/*` | Partially Covered | Provider calls require explicit test mode. |
| Cross-service workflow | API Gateway and core services | TEST-022 | `e2e/*` | Skipped by Environment | Enable E2E and provide services/fixtures. |
| Service readiness and failure reporting | All configured services | TEST-002, TEST-022 | `environment/*` | Covered | Failures identify the missing service. |
| CI build, reports, and safe defaults | Maven and GitHub Actions | TEST-022, TEST-023 | `.github/workflows/test.yml` | Covered | Static CI needs no live services or secrets. |
| Final traceability and quality review | Test repository | TEST-023 | This document and related final documents | Covered | Reviewed during TEST-023. |
| Backend code-coverage and executed-register evidence | Backend CI and tests repository | TEST-027 | Backend JaCoCo artifacts, `tools/test-evidence.py`, `docs/test-case-mappings.csv` | Partially Covered | Backend publishes per-service JaCoCo artifacts and applies configured module/scope checks; tests CI generates run-specific JUnit evidence. The mapping CSV still needs case-level rows, and combined coverage evidence requires matching backend artifacts. |

## Service inventory

| Service | Base URL variable | Main package | Roles | Live-test selector | Limitation |
|---|---|---|---|---|---|
| User | `USER_SERVICE_BASE_URL` | `user` | student, teacher, admin | `*User*IT` | Requires Keycloak/test users. |
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
| Keycloak | `KEYCLOAK_BASE_URL` | `auth`, `environment` | configured users | `*Authentication*IT` | No credentials in source. |

## Verification

```powershell
mvn clean test
mvn verify -Pintegration
mvn -Dtest="*User*IT" verify -Pintegration
mvn -Dtest="*Gateway*IT,*JourneyIT" verify -Pintegration
```

Static CI runs with live flags disabled. Live commands are for approved
non-production environments only.

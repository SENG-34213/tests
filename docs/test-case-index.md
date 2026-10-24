# Test-Case Index

Detailed scenario tables remain beside each service in `docs/*-test-cases.md`.
This index records the executable coverage and its environment status.

| ID range | Service | Scenario | Roles | Test package | Level | Expected result | Environment | Status |
|---|---|---|---|---|---|---|---|---|
| TC-AUTH-* | Keycloak/Gateway | Login, refresh, logout, protected access | student, teacher, admin | `auth`, `user/AuthenticationIT` | integration/security | Valid credentials work; invalid access is rejected | Keycloak, gateway, users | Environment-gated |
| TC-USER-* | User | Profile, validation, authorization, administration | student, teacher, admin | `user` | integration/security | Role and validation rules are enforced | User Service, Keycloak | Environment-gated |
| TC-COURSE-* | Course and teacher portal | Course lookup/CRUD, dashboard/search, portal data, and authorization | student, teacher, admin | `course` | integration/security | Allowed operations succeed; forbidden operations fail | Course, Keycloak; CRUD and owned-course/enrolled-student fixtures | Environment-gated |
| TC-ENROLLMENT-* | Enrollment | Reads, status transitions, soft-delete cleanup, and cross-service access | student, teacher, admin | `enrollment`, `e2e/CourseEnrollmentJourneyIT` | integration/e2e | Enrollment state, cleanup, and role rules are correct | Enrollment, course and optional communication fixtures | Environment-gated |
| TC-ASSIGNMENT-* | Assignment | Assignment, submission, attachment workflows | student, teacher, admin | `assignment` | integration | Lifecycle and authorization contracts hold | Assignment/Material, fixtures | Environment-gated |
| TC-MATERIAL-* | Material | Upload, list, download, archive, cleanup | teacher, admin | `material` | integration/validation | Contract and safe cleanup behavior hold | Material, files/fixtures | Environment-gated |
| TC-CALENDAR-* | Calendar | Authentication and event workflow | authenticated users | `calendar` | integration | Calendar access follows role rules | Calendar | Environment-gated |
| TC-QUIZ-* | Quiz | Management, validation, attempts | student, teacher | `quiz` | integration | Quiz lifecycle and validation hold | Quiz, optional fixtures | Environment-gated |
| TC-ATTENDANCE-* | Attendance | Sessions, marking, summaries | student, teacher | `attendance` | integration | Attendance rules and summaries hold | Attendance, fixtures | Environment-gated |
| TC-COMMUNICATION-* | Communication | Groups, messages, settings, attachment workflows, STOMP authentication/authorization, and test-topic delivery | student, teacher | `communication` | integration/security | HTTP access/lifecycle and opt-in WebSocket denial, membership checks, and echo delivery are asserted; production group broadcast still needs member fixtures | Communication and Gateway, optional Course/Enrollment/Material fixtures; explicit WebSocket opt-in | Environment-gated |
| TC-EXAM-* | Exam | Lifecycle, marking, results | student, teacher, admin | `exam` | integration | State transitions and results hold | Exam, fixtures | Environment-gated |
| TC-PAYMENT-* | Payment | Student payment, admin access, validation | student, admin | `payment` | integration/security | Safe payment and authorization hold | Payment; provider optional | Environment-gated |
| TC-NOTIFICATION-* | Notification | Ownership, authorization, validation | student, admin | `notification` | integration/security | Access and validation hold | Notification; email optional | Environment-gated |
| TC-AI-* | AI | Health, conversations, provider boundary, lesson/material summaries, quiz generation, opt-in WebSocket authorization and error delivery | authenticated users | `ai` | integration/security | HTTP contracts and WebSocket auth/destination denial and safe error delivery are asserted; successful provider-backed chat remains fixture-gated | AI; deterministic provider/content fixtures optional; explicit WebSocket opt-in | Environment-gated |
| TC-E2E-* | Gateway | Authentication, course/enrollment, authorization | student, teacher | `e2e` | e2e/smoke | Gateway journey succeeds or skips clearly | Gateway, Keycloak, fixtures | Disabled by default |
| TC-FOUNDATION-* | Repository | Configuration and smoke behavior | n/a | `foundation`, `environment`, `api/*Test` | unit | Support is deterministic without services | Java/Maven | Static CI |
| TC-TEST-028-* | Staging | Read-only readiness, identity discovery, public/protected gateway probes | n/a | `staging/StagingSmokeIT`, `staging/*Test` | unit/integration/smoke | Only approved staging is probed; required checks pass or the run fails | Protected staging environment | Manual-only; live run not yet evidenced |

## Naming and selection rules

- `*Test` is a static test and runs with `mvn test`.
- `*IT` and `*IntegrationTest` are live tests and run with the `integration`
  Maven profile.
- Service selection uses the class patterns in `docs/test-execution.md`.
- Methods should describe behavior and outcome, such as
  `shouldRejectStudentAccessToAdminEndpoint`.
- Reserved JUnit vocabulary is `unit`, `integration`, `e2e`, `security`,
  `validation`, `smoke`, and `slow`. The current reliable selector is class
  naming; tag filtering must be wired into Maven before being advertised.
- Run-specific execution results are generated by `tools/test-evidence.py`;
  this index remains the suite-level catalog. Stable case-to-JUnit mappings
  belong in `docs/test-case-mappings.csv`. Unmapped methods and cases not
  executed in a run remain visible in the generated register.

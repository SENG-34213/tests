# Test-Case Index

Detailed scenario tables remain beside each service in `docs/*-test-cases.md`.
This index records the executable coverage and its environment status.

| ID range | Service | Scenario | Roles | Test package | Level | Expected result | Environment | Status |
|---|---|---|---|---|---|---|---|---|
| TC-AUTH-* | Auth Service/Gateway | Login, refresh, logout, protected access | student, teacher, admin | `auth`, `user/AuthenticationIT` | integration/security | Valid credentials work; invalid access is rejected | Auth Service, gateway, users | Environment-gated |
| TC-USER-* | User | Profile, validation, authorization, administration | student, teacher, admin | `user` | integration/security | Role and validation rules are enforced | User Service, Auth Service | Environment-gated |
| TC-COURSE-* | Course and teacher portal | Course lookup/CRUD, dashboard/search, portal data, and authorization | student, teacher, admin | `course` | integration/security | Allowed operations succeed; forbidden operations fail | Course, Auth Service; CRUD and owned-course/enrolled-student fixtures | Environment-gated |
| TC-ENROLLMENT-* | Enrollment | Reads, status transitions, soft-delete cleanup, and cross-service access | student, teacher, admin | `enrollment`, `e2e/CourseEnrollmentJourneyIT` | integration/e2e | Enrollment state, cleanup, and role rules are correct | Enrollment, course and optional communication fixtures | Environment-gated |
| TC-ASSIGNMENT-* | Assignment | Assignment, submission, attachment workflows | student, teacher, admin | `assignment` | integration | Lifecycle and authorization contracts hold | Assignment/Material, fixtures | Environment-gated |
| TC-MATERIAL-* | Material | Upload, list, download, archive, cleanup | teacher, admin | `material` | integration/validation | Contract and safe cleanup behavior hold | Material, files/fixtures | Environment-gated |
| TC-CALENDAR-* | Calendar | Authentication, event lifecycle, role checks, and cross-teacher course-event ownership | student, teacher, admin | `calendar` | integration/security | Lifecycle succeeds; another teacher cannot mutate an unassigned course event | Calendar and Course Services; `TEST_CALENDAR_COURSE_ID`; optional distinct secondary teacher | Environment-gated |
| TC-QUIZ-* | Quiz | Management, validation, attempts, scoring, and quiz-owner checks | student, teacher | `quiz` | integration/security | Quiz lifecycle and validation hold; a different teacher cannot read or manage the quiz | Quiz, Course, Enrollment, optional User Service; fixtures and optional secondary teacher | Environment-gated |
| TC-ATTENDANCE-* | Attendance | Session lifecycle, marking, summaries, and course-owner checks | student, teacher, admin | `attendance` | integration/security | Attendance rules hold; a different teacher cannot list sessions for the course | Attendance and fixtures; optional distinct secondary teacher | Environment-gated |
| TC-COMMUNICATION-* | Communication | Groups, messages, settings, attachment upload/download, STOMP authentication/authorization and delivery | student, teacher | `communication` | integration/security | HTTP lifecycle, non-member denial, direct/Gateway test delivery and fixture-gated persisted group broadcast are asserted; attachment bytes can be checked against the synthetic upload | Communication and Gateway, optional Course/Enrollment/Material fixtures; explicit WebSocket and storage-host opt-in | Environment-gated |
| TC-EXAM-* | Exam | Lifecycle, cancellation, marking/results, student visibility, and attachments | student, teacher, admin | `exam` | integration/security | State transitions, course ownership, student mark-entry denial, result reads, attachment visibility and optional exact-byte downloads are asserted | Exam, fixtures, and optional Material storage-host configuration | Environment-gated |
| TC-PAYMENT-* | Payment | Student payment, admin access, validation | student, admin | `payment` | integration/security | Safe payment and authorization hold | Payment; provider optional | Environment-gated |
| TC-NOTIFICATION-* | Notification | Ownership, authorization, validation | student, admin | `notification` | integration/security | Access and validation hold | Notification; email optional | Environment-gated |
| TC-AI-* | AI | Health, conversations, provider boundary, lesson/material summaries, quiz generation, opt-in WebSocket authorization and error delivery | authenticated users | `ai` | integration/security | HTTP contracts and WebSocket auth/destination denial and safe error delivery are asserted; successful provider-backed chat remains fixture-gated | AI; deterministic provider/content fixtures optional; explicit WebSocket opt-in | Environment-gated |
| TC-E2E-* | Gateway | Authentication, complete student/teacher/admin workflows, course/enrollment, authorization | student, teacher, admin | `e2e` | e2e/smoke | Gateway journeys succeed or skip clearly; opt-in workflow removes disposable objects and leaves the archived assignment's synthetic submission because no delete route exists | Gateway, Auth Service, all journey services, role credentials, grade fixture for writes | Disabled by default |
| TC-FOUNDATION-* | Repository | Configuration and smoke behavior | n/a | `foundation`, `environment`, `api/*Test` | unit | Support is deterministic without services | Java/Maven | Static CI |
| TC-UNIT-* | Test harness | API client contracts, configuration parsing, readiness, fixture-safe policies, resilience toggles, and repository utility behavior | n/a | JUnit `*Test` classes in the test harness | unit | Every executed unit-test method has an explicit mapping and JUnit records its outcome | None; deterministic unit tests | Static CI |
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

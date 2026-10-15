# Test-Case Index

Detailed scenario tables remain beside each service in `docs/*-test-cases.md`.
This index records the executable coverage and its environment status.

| ID range | Service | Scenario | Roles | Test package | Level | Expected result | Environment | Status |
|---|---|---|---|---|---|---|---|---|
| TC-AUTH-* | Keycloak/Gateway | Login, refresh, logout, protected access | student, teacher, admin | `auth`, `user/AuthenticationIT` | integration/security | Valid credentials work; invalid access is rejected | Keycloak, gateway, users | Environment-gated |
| TC-USER-* | User | Profile, validation, authorization, administration | student, teacher, admin | `user` | integration/security | Role and validation rules are enforced | User Service, Keycloak | Environment-gated |
| TC-COURSE-* | Course | Lookup, CRUD, authorization | student, teacher, admin | `course` | integration | Allowed operations succeed; forbidden operations fail | Course, fixtures | Environment-gated |
| TC-ENROLLMENT-* | Enrollment | Reads and cross-service access | student, teacher, admin | `enrollment`, `e2e/CourseEnrollmentJourneyIT` | integration/e2e | Enrollment and role rules are correct | Enrollment, course fixture | Environment-gated |
| TC-ASSIGNMENT-* | Assignment | Assignment, submission, attachment workflows | student, teacher, admin | `assignment` | integration | Lifecycle and authorization contracts hold | Assignment/Material, fixtures | Environment-gated |
| TC-MATERIAL-* | Material | Upload, list, download, archive, cleanup | teacher, admin | `material` | integration/validation | Contract and safe cleanup behavior hold | Material, files/fixtures | Environment-gated |
| TC-CALENDAR-* | Calendar | Authentication and event workflow | authenticated users | `calendar` | integration | Calendar access follows role rules | Calendar | Environment-gated |
| TC-QUIZ-* | Quiz | Management, validation, attempts | student, teacher | `quiz` | integration | Quiz lifecycle and validation hold | Quiz, optional fixtures | Environment-gated |
| TC-ATTENDANCE-* | Attendance | Sessions, marking, summaries | student, teacher | `attendance` | integration | Attendance rules and summaries hold | Attendance, fixtures | Environment-gated |
| TC-COMMUNICATION-* | Communication | Groups, messages, settings | student, teacher | `communication` | integration | Communication access/lifecycle hold | Communication, fixtures | Environment-gated |
| TC-EXAM-* | Exam | Lifecycle, marking, results | student, teacher, admin | `exam` | integration | State transitions and results hold | Exam, fixtures | Environment-gated |
| TC-PAYMENT-* | Payment | Student payment, admin access, validation | student, admin | `payment` | integration/security | Safe payment and authorization hold | Payment; provider optional | Environment-gated |
| TC-NOTIFICATION-* | Notification | Ownership, authorization, validation | student, admin | `notification` | integration/security | Access and validation hold | Notification; email optional | Environment-gated |
| TC-AI-* | AI | Health, conversations, provider boundary | authenticated users | `ai` | integration/security | AI contract and role rules hold | AI; provider optional | Environment-gated |
| TC-E2E-* | Gateway | Authentication, course/enrollment, authorization | student, teacher | `e2e` | e2e/smoke | Gateway journey succeeds or skips clearly | Gateway, Keycloak, fixtures | Disabled by default |
| TC-FOUNDATION-* | Repository | Configuration and smoke behavior | n/a | `foundation`, `environment`, `api/*Test` | unit | Support is deterministic without services | Java/Maven | Static CI |

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

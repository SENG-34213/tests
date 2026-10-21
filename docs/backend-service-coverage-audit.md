# Backend Service Test Coverage Audit

Audit date: 2026-10-21  
Test repository baseline: current `feature/TEST-028-staging-release-smoke` worktree  
Backend source inspected: local Mentify backend snapshot at `feature/teacher-backend-contracts` (`c748e35a`; its `origin/development` was reported as `2736e371`).

## Conclusion

Service testing is substantial, but it does **not** cover every backend route or
every service behavior. The repository has executable integration suites for
the major user-facing services, plus unit, contract, security, E2E, performance,
and staging checks. A passing static Maven verification is evidence that the
tests compile/pass; it is not proof that live service integrations ran or that
all API endpoints are covered.

This audit compared the test repository's clients and `*IT` suites with the
controllers in the inspected backend source. It is a route-family-level audit,
not a claim that every one of the backend's 246 controller mapping declarations
has an independently asserted success and failure case. Some endpoints have
useful representative tests; untested variants and fixture-dependent success
paths remain.

## Coverage by service

| Service / route family | Current evidence in this repository | Finding / remaining work |
|---|---|---|
| Authentication / user | Login, refresh, logout, protected access, profile, admin, validation | Password recovery verify/reset and registration success scenarios need explicit end-to-end confirmation; existing API/client presence alone is not proof of workflow coverage. |
| Course and teacher portal | Base course lookup/CRUD/authorization; new dashboard/search and student-role denial tests added in `TeacherPortalAuthorizationIT` | Course modules, lessons, learning-material CRUD/uploads, grades/monthly fees, announcements, student progress, teacher directory/summary/gradebook/analytics still lack complete end-to-end success/lifecycle coverage. Teacher portal success with representative course/student data is fixture-dependent and not claimed here. |
| Enrollment | Enrollment reads and cross-service behavior, with authorization and selected lifecycle workflows | Broad coverage exists; ensure mutations and all state transitions are exercised against disposable fixtures. |
| Assignment / submission / file | Assignment and submission workflows; assignment and submission attachment clients/workflows | Broad coverage exists; review limits, malformed files, ownership boundaries and cleanup failure paths against current backend behavior. |
| Quiz | Management, question, attempt, validation and authorization suites | AI quiz generation and edge cases for attempt lifecycle need live verification; don't infer coverage from a client method. |
| Exam | Lifecycle, student results, authorization and validation suites | Exam attachment list/upload/download/delete routes need explicit confirmation/coverage. |
| Material | Public lifecycle, upload/download/archive and authorization | `ai-sources` plus internal association and maintenance audit/cleanup routes are not demonstrated by the public lifecycle suite. Internal routes should be tested in service-level/security tests, not exposed as ordinary user flows. |
| Attendance | Session lifecycle, records, summaries, validation, authorization | Representative coverage is present; verify cancellation/completion edge states and student-specific summary boundaries. |
| Calendar | Event lifecycle, queries, authentication | Venue endpoints are not represented in the suite; internal projection/reconciliation endpoints are infrastructure contracts and not covered by user-facing tests. |
| Communication | Group/message/settings lifecycle, validation, authorization | Message attachment upload/download needs explicit workflow coverage; WebSocket behavior is outside the current REST suites. |
| Payment | Student start/read/status and admin/refund authorization | Stripe webhook signature/idempotency and internal service lookup are not demonstrated by the ordinary payment integration suite. |
| Notification | Workflow, ownership, authorization and validation | Core REST paths are represented; delivery provider behavior is optional and needs provider-specific test doubles or an explicit exclusion. |
| AI | Health, conversation, provider boundary, authorization, validation; lesson/material summarization validation and quiz-generation authorization | Successful lesson/material summarization and quiz-generation behavior is not guaranteed by validation/auth tests; external provider success requires controlled provider fixtures. |

## Changes made in this audit

- Added `CourseClient` calls for the six `/api/v1/teacher` read endpoints.
- Added read-only teacher dashboard/search success probes, unauthenticated
  checks for dashboard/search, and student-role denial checks for all six
  teacher portal routes.
- Added local required-parameter validation checks and catalogued these cases
  in `docs/course-test-cases.md`.

These checks add coverage of route access and role boundaries. They do not
replace data-backed teacher directory, academic summary, gradebook, analytics,
or broad course-content lifecycle tests.

## Scope and execution caveats

- Backend internal/service-to-service endpoints are separated from user-facing
  route coverage. They should be exercised with service-specific credentials,
  contract tests, or component tests; ordinary user tokens should not be used
  to probe them.
- Provider callbacks, asynchronous messaging/WebSocket behavior, and external
  AI/payment providers require controlled test doubles or dedicated non-prod
  environments.
- Live `*IT` results depend on service availability, credentials and fixtures.
  Unless the integration profile is run against those services, status is
  “implemented, not live-verified,” not “passed.”
- `docs/test-case-index.md`, service case catalogs, and generated run evidence
  should be updated whenever additional workflows are implemented or run.

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

## Test-to-case traceability status

A source scan of `*IT.java` files found 232 conventional annotated test methods.
The mapping CSV contains 208 rows, mapping 155 distinct integration methods;
77 annotated methods do not yet have a CSV case mapping. There are no duplicate
case IDs or compound method-name cells. Thus the mappings are useful but are
not yet a complete register of executable integration coverage. The counts include gateway,
resilience, staging, and infrastructure tests as well as user-facing services.
Mapping work should use the service catalogs' existing case IDs where they
describe the method; otherwise add a specific documented case before mapping it.

Unmapped methods by test package at this audit:

| Test package | Unmapped methods |
|---|---:|
| AI | 5 |
| API | 1 |
| Assignment | 0 |
| Attendance | 6 |
| Authentication | 0 |
| Calendar | 11 |
| Communication | 8 |
| End-to-end | 5 |
| Enrollment | 1 |
| Environment | 1 |
| Exam | 4 |
| Material | 8 |
| Notification | 3 |
| Payment | 4 |
| Quiz | 5 |
| Resilience | 2 |
| Security | 3 |
| Staging | 4 |
| User | 6 |

Course, Assignment, Authentication, and User self-service conventional
integration methods were reconciled with their case catalogs in this pass;
missing role/validation and ownership scenarios were added, and stale
method-name/ID mismatches were corrected. Assignment attachment case IDs now
match their documentation. Compound mapping cells in Attendance, Quiz, and
Enrollment were split into one row per JUnit method, and catalogs expanded
where a prior case combined distinct behaviors. Other package counts above
remain explicit traceability work rather than evidence that those tests are
absent.

The local evidence-generator run also exposed and fixed a class-name join bug:
the CSV stores simple Java class names while JUnit XML stores fully qualified
names. The generator now resolves a simple name only when it identifies one
unique JUnit class, and regression tests cover exact, unique, and ambiguous
resolution. The latest local evidence run read 91 executed static JUnit
methods; 7 matched current case mappings and 84 remain unmapped. No backend
JaCoCo reports were supplied, so backend code coverage remains unavailable.

## Coverage by service

| Service / route family | Current evidence in this repository | Finding / remaining work |
|---|---|---|
| Authentication / user | Login, refresh, logout, role access, password recovery privacy/error paths (unknown account `200`; invalid OTP/token `400`), JWKS, self-service profile and teacher-route checks, admin and lookup checks; admin list results now assert role-filter correctness; student/teacher existence endpoints assert `false` for unknown IDs; student batch lookup checks own-vs-other ownership when distinct secondary credentials are configured | Password-reset happy path and successful registration/status/profile mutation workflows still need dedicated isolated accounts and mail capture. Cross-user and other integration cases have not been live-verified in this run. |
| Course and teacher portal | Base course lookup/CRUD/authorization; module/lesson and learning-material-reference CRUD; explicitly gated cross-service module/lesson material upload; grade/monthly-fee lifecycle; announcement create/read and progress read/update; dashboard/search and analytics field/date checks; wrong-owner teacher portal reads; separately gated wrong-owner module/lesson/material create/update/delete denials; student lookup/teacher create missing-resource `404` and teacher-role denial of student progress `403` | Ownership probes use known foreign-course resource fixtures and are write-shaped security regression tests, so they require explicit opt-in and have not been live-verified. Interactions among all analytics source services and teacher upload-route cross-owner denial need controlled fixtures; other fixture-gated success tests remain unverified live. |
| Enrollment | Role/auth checks for reads and writes; course enrollment list; opt-in self-enrollment; admin create, course/status transitions, soft delete and membership cleanup; teacher writes checked against owned and unowned courses; unknown enrollment update/delete now assert `404` | Write paths require verified unused/free courses without active communication groups because group membership is not removed by enrollment deletion; Course/Communication runtime execution remains unverified. |
| Assignment / submission / file | Assignment CRUD/publish/close and partial PATCH; student assignment dashboard filters/page validation; submission draft/submit/grade/return; rubric ordering/persistence; DTO bounds for attempts, late penalty, rubric shape and grade marks; attachment Material-link and multipart-upload workflows; submission-file workflows and secondary-student ownership denial; unknown assignment, attachment, and submission-file IDs require `404` | Multipart writes and secondary-student ownership flow are opt-in and unverified live. Remaining: business-rule boundaries for due dates/late submission and max attempts, aggregate rubric marks exceeding assignment maximum (backend currently throws generic `IllegalArgumentException` and global handler returns `500`), assignment-attachment cross-user ownership, binary file byte checks, and cleanup failure handling. Other backend service methods throw generic `RuntimeException`, which the inspected global handler returns as `500`; do not treat that as a correct client-error contract. |
| Quiz | Management, questions, student-safe quiz projection, teacher quiz results and closed-course gradebook reads, attempt scoring/submission, duplicate active-attempt rejection, submitted-attempt immutability, attempt-limit enforcement, AI generation input validation, and authorization | Teacher results assert latest-submission aggregation and student metadata; gradebook success requires a configured closed quiz/course fixture. These expanded workflows are fixture-gated and not live-verified. AI quiz generation success and enrollment-dependent attempt paths still require controlled provider/service fixtures. |
| Exam | Lifecycle, student results/upcoming/detail, PDF-only attachment validation, attachment upload/list/download-metadata/delete; scheduled-exam student-visible vs teacher-only attachment list/download access; invalid DTOs assert `400` and unknown exam/course UUIDs assert `404` | Student lifecycle and attachment-visibility success need the authenticated student to match the enrolled `TEST_EXAM_STUDENT_ID`; attachment tests require opt-in exam writes plus Course, Enrollment, Exam and Material services and have not been live-verified. Actual stored attachment bytes need storage/provider-backed checks. |
| Material | Inspected public controller route families include upload, filtered list, AI-source list, get, download metadata, update, archive, delete, and maintenance audit/cleanup; tests cover lifecycle, purpose/status/course/search filters, role/auth boundaries, unknown-ID `404`, AI-source authorization, and dry-run maintenance; an explicitly gated presigned-URL byte comparison is implemented | The backend snapshot does contain Material HTTP controllers; previous audit wording claiming otherwise was incorrect. Internal material-association routes remain service-contract behavior; the byte comparison requires reachable non-production S3/MinIO and an explicitly approved exact host and has not been live-verified; cross-user file visibility still needs fixtures. |
| Attendance | Session lifecycle, records, summaries, validation, authorization | Session tests now assert exact conflict/not-found behavior for incomplete, cancelled, completed, duplicate, and out-of-session marking cases; they mark all enrolled fixture students before completion. Summary responses assert count/percentage invariants. Still requires a live fixture run to validate integration behavior and fixture correctness. |
| Calendar | Event create/update/read/cancel lifecycle, correctly parameterized date-range query, upcoming query, venue list auth, venue create-role denial and validation; unknown detail/update and missing range assert exact `404`/`400`, anonymous detail access asserts `401` | Internal projection/reconciliation endpoints are infrastructure contracts and not covered by user-facing tests. Venue creation success requires persistent admin-write fixtures because no delete route exists. |
| Communication | Group/message/settings lifecycle, validation, authorization; attachment upload/link/download-metadata/delete cleanup workflow; exact invalid-input/unknown-group status assertions; message page-size cap; opt-in direct STOMP WebSocket teacher CONNECT authentication smoke test | Attachment byte checks, group member send/subscribe authorization, actual WebSocket message/broadcast delivery, and Kafka/notification behavior still require live fixtures/runtime; the added WebSocket smoke and gated workflows have not been live-verified. |
| Payment | Exact initiation response and owned list/detail/status checks; optional second-student read-denial; admin page/filter/result contracts; missing/malformed and valid-unknown course initiation errors (`400`/`404`); successful verification role checks; strict refund transition for a named disposable fixture; webhook signature/replay checks; separately gated signed `PENDING -> SUCCESS` and `PENDING -> FAILED` transitions with duplicate delivery | Both webhook transitions require separate dedicated disposable pending-payment fixtures and have not been live-verified; unpublished/free-course rejection and Stripe minor-unit/provider failures still need controlled course/Stripe fixtures. |
| Notification | Pagination/default metadata, unread-only records/count, idempotent mark-read, mark-all, deletion, exact `400`/`404` ID cases, and cross-user mutation denial | Added direct HTTP pagination-boundary checks; inspected shared exception handling has no explicit method-validation mapping, so this contract needs live verification and may require an unavailable backend handler change. Kafka ingestion and email delivery still need deterministic broker/mail-capture fixtures. |
| AI | Health/current-user routes; conversation CRUD with response-field checks and cleanup; optional distinct-owner denial; deterministic chat/text generation; fixture-gated lesson/material summaries and multipart quiz-draft success using the backend-supported `MULTIPLE_CHOICE_SINGLE_ANSWER`; conversation and public quiz-generation semantic validation (`400`) before provider call; opt-in direct STOMP WebSocket teacher CONNECT authentication probe | Provider-backed tests require deterministic provider mode plus course/content fixtures; multipart success, ownership, provider integrations, WebSocket chat destination authorization, and response delivery remain unverified live. |

## Changes made in this audit

- Added `CourseClient` calls for the six `/api/v1/teacher` read endpoints.
- Added read-only teacher dashboard/search success probes, unauthenticated
  checks for dashboard/search, and student-role denial checks for all six
  teacher portal routes.
- Added a role matrix for course-content writes, grade/monthly-fee
  administration, and student-only announcement/progress reads. Also corrected
  an obsolete test expectation: the current backend explicitly permits
  teachers to reach course creation; the test now uses the authenticated
  teacher's ID and only an unknown grade ID to avoid triggering the ownership
  guard before the course-service validation path.
- Added password-recovery privacy/error checks and a JWKS availability contract
  check for the auth service; a full reset happy path remains untested.
- Replaced vague User/Auth "under 500" assertions with explicit response codes
  where the inspected backend guarantees them; corrected malformed student-ID
  existence semantics to the backend's `200 false` contract.
- Tightened Material unknown-resource checks from permissive `400 or 404` to
  the exact `404` contract documented by `MaterialNotFoundException` handling.
- Replaced permissive Communication "under 500" assertions with explicit
  validation/not-found results based on its DTO constraints and exception
  handler; tightened successful group/message operations to controller status
  codes and added the message-history maximum page-size check.
- Replaced Exam "under 500" validation/unknown-ID checks with explicit `400`
  and `404` expectations based on the controller validation and exception
  handler contracts.
- Tightened AI validation checks to explicit `400` results and added
  `404` coverage for conversation read/messages/update/delete on an unknown ID;
  changed the public quiz-generation check to malformed input so it verifies
  unauthenticated access without invoking the provider or dependent services.
- Replaced broad Course lookup/create-boundary statuses with `404` assertions
  for known missing course/grade fixtures and tightened unknown Enrollment
  update/delete checks to the service's not-found contract.
- Tightened invalid password-reset OTP/token and Calendar boundary assertions
  to the handlers' exact `400`/`404` responses and event controller's `201`/`200`
  success statuses. Unknown Assignment, assignment attachment, and submission
  file identifiers now assert `404` rather than allowing `400` as well.
- Narrowed the remaining Security/Course authorization boundary expectations:
  malformed JWT responses require `401`, while a valid teacher token on the
  student-only progress controller requires `403`.
- Added a secondary-student-gated User lookup ownership test that verifies a
  student can batch-lookup their own summary but receives `403` for another
  student's summary through both GET and POST lookup routes.
- Strengthened User administration list checks to inspect result roles, not only
  HTTP success; student and teacher existence route checks now assert their
  boolean `false` payload for unknown IDs and malformed student identities.
- Asserted `404` for valid status-update and delete requests targeting an
  unknown user, alongside invitation resend; malformed status payload remains
  separately asserted as `400`.
- Added a separately gated Payment success-webhook integration case using a
  dedicated pending payment; it checks signature acceptance, transition to
  `SUCCESS`, `paidAt` persistence, and duplicate-delivery idempotency.
- Added an independently fixture-gated signed failure-webhook case for a
  separate pending payment; it checks transition to `FAILED`, absence of `paidAt`,
  and duplicate-delivery idempotency.
- Strengthened AI conversation lifecycle responses and added owner-side cleanup
  on failure paths; added an opt-in, distinct teacher/admin ownership test that
  expects `404` for another owner's conversation operations.
- Added a fixture-gated teacher portal data test for directory, academic
  summary, gradebook and analytics success responses with response-field checks;
  added wrong-course ownership denials and analytics date-range validation.
- Added a fixture-gated create/read/update/delete workflow for course modules
  and lessons with cleanup of resources created by the test.
- Added explicitly enabled admin CRUD coverage for grades and monthly fees;
  created records are removed in test cleanup.
- Added student announcement/progress reads, pagination checks, wrong-role and
  payload validation checks; course progress success is gated on an enrolled
  course fixture.
- Added explicit opt-in successful course-announcement creation and student
  lesson-progress update tests. Announcement creation is documented as
  persistent because the backend has no deletion route; progress restores its
  previous completion state (or leaves a new record incomplete).
- Added an explicitly enabled cross-service upload workflow for both
  module-level and lesson-level course materials. It verifies the Material
  Service records and cleans up course references and uploaded test files.
- Added current-user no-op profile update, teacher profile/guidance-note role
  checks, user lookup cases, and password-change/public registration validation.
- Added fixture-gated Material AI-source listing and unauthorized-role checks.
- Added opt-in byte-for-byte verification of Material's presigned HTTPS download
  URL, restricted to an explicitly configured exact object-storage host and
  disabled by default; the test never follows redirects or logs the signed URL.
- Added payment verification success/role checks, malformed signature rejection,
  and an opt-in signed unsupported-event replay check; payment status transitions
  still require a controlled PaymentIntent fixture.
- Strengthened deterministic AI checks to require successful status and response
  data; added fixture-gated lesson/material summary and quiz-draft assertions.
- Corrected the fixture-gated AI quiz-generation success test to use the
  backend's accepted `MULTIPLE_CHOICE_SINGLE_ANSWER` value, and assert that the
  response preserves that type. Added unauthenticated semantic-validation
  probes for invalid question count, difficulty and question type that stop
  before provider execution.
- Added calendar venue list authentication, create-role denial, and invalid
  input tests without creating persistent venue records.
- Added exam upcoming-read and attachment-route authentication/role probes.
- Added an opt-in exam attachment upload/list/download-metadata/delete workflow
  using a synthetic PDF and cleanup of the test-created Exam attachment and
  Material records; added scheduled-student checks for visible and hidden
  attachment listing/download authorization, and a `.txt` rejection case that
  confirms the production service's PDF-only attachment contract.
- Added student exam-detail route authorization/not-found assertions and a
  scheduled-exam read in the opt-in lifecycle using the configured enrolled
  student credentials.
- Added authentication probes for communication message attachment upload and
  download routes.
- Added an opt-in communication attachment workflow that uploads a synthetic
  file through Material Service, links it to a message, checks download
  metadata, then deletes the message and soft-deletes its Material record.
- Corrected stale course authorization documentation to match the inspected
  backend's current teacher course-create/update/read permissions.
- Added separate Super Admin credentials to test-role configuration and
  extended the user-management role checks to include the newly audited delete
  route and Super Admin-only account creation restriction.
- Added local required-parameter validation checks and catalogued these cases
  in `docs/course-test-cases.md`.
- Completed the public Calendar event-detail route probe: read-after-update
  checks event identity and title, unknown detail and update requests assert
  `404`, and the list-query workflow supplies its required date range and
  separately verifies missing-range validation.
- Corrected the Material audit against the actual controller source inventory,
  and strengthened the lifecycle workflow to verify purpose/status filters for
  both uploaded and archived materials; unknown-ID documentation now states
  the exact `404` contract.
- Added client methods and fixture-gated multipart workflows for Assignment
  attachments and Submission files, checking creation metadata, listing,
  download metadata, authorization, and deletion of only the test-created
  attachment/file and Material records.
- Added a secondary-student-gated submission-file ownership scenario: another
  student must be denied list, download, and delete access to the owner's
  submission files; the owner and test clean up only the synthetic file and
  Material created by the scenario.
- Added assignment DTO-boundary cases for allowed attempts, late-penalty
  percentage, blank rubric criteria and rubric item count; extended the gated
  assignment lifecycle to assert rubric order/marks and preservation across
  full update and partial PATCH.
- Added grading request validation for missing, negative and over-100 marks,
  using an unknown submission ID to verify malformed grade requests are rejected
  before any submission lookup or mutation.
- Added teacher Quiz results and closed-quiz gradebook route coverage, including
  latest submitted attempt aggregation, student metadata, route authorization,
  required course parameter validation, and explicit User Service readiness for
  the results workflow.

These checks add coverage of route access and role boundaries. Course-content
upload now has a fixture-gated cross-service workflow, but course ownership
boundaries and every field of teacher analytics are not exhaustively verified.

## Scope and execution caveats

- Backend internal/service-to-service endpoints are separated from user-facing
  route coverage. They should be exercised with service-specific credentials,
  contract tests, or component tests; ordinary user tokens should not be used
  to probe them.
- Provider callbacks, asynchronous messaging/WebSocket behavior beyond the
  opt-in communication STOMP CONNECT smoke test, and external AI/payment
  providers require controlled test doubles or dedicated non-prod environments.
- Live `*IT` results depend on service availability, credentials and fixtures.
  Unless the integration profile is run against those services, status is
  “implemented, not live-verified,” not “passed.”
- `docs/test-case-index.md`, service case catalogs, and generated run evidence
  should be updated whenever additional workflows are implemented or run.

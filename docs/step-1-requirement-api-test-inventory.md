# Step 1: Requirement, API, UI-flow, and existing-test inventory

Audit baseline: GitHub development branch commits queried on 2026-10-23: backend `6755c82d198f74180deb09523983ac6d9818e2ff`, frontend `b41fd3d6136b98293b722d4608f8ceecf4cdc9c3`, and tests `c51984f0f097a9eb39e2b2f91cc26987c9e731cb`. The working branch is `feature/TEST-029-development-inventory`, created from the fetched tests development commit. The backend and frontend exact commits were fetched and their source trees inspected without switching or modifying their working branches. Presence of a test class is not evidence that its live test passed.

## Scope from the course PDF

The PDF separates backend source, frontend source, infrastructure, and integration/E2E tests (section 3.1). Its testing requirements (chapter 6) specify unit, integration, and E2E layers; at least 80% line/branch coverage for new unit-tested code; integration coverage for all API endpoints; and 90% branch coverage for critical business logic. It also asks for documented test cases and CI-generated coverage evidence. Chapter 7 requires CI build/test/security stages; chapter 8 requires evidence against the OWASP risks; chapters 9-10 and Appendix A require staging demonstration, test register, coverage, security and performance evidence, and tests as part of definition of done.

Coverage ownership: backend JaCoCo measures backend code coverage. Frontend tests own UI behavior and browser workflows. This repository owns public API integration, cross-service workflows, and consolidated test evidence. Internal service-to-service routes are inventoried separately from user-facing APIs and need appropriate service identity/contract tests, not ordinary student tokens.

## Repository snapshot and current evidence

| Repository | Inspected checkout | Step 1 observation |
|---|---|---|
| tests | GitHub `development` `c51984f` baseline; working changes on `feature/TEST-029-development-inventory` | Baseline had 224 mappings and 81 integration source files. This branch adds 65 `TC-TRACE-*` traceability mappings and 15 route/feature cases, bringing the CSV to 304 unique case IDs. |
| backend | GitHub `development` `6755c82` | Exact remote tree inspected. Local working tree's untracked `frontend/` was preserved. |
| frontend | GitHub `development` `b41fd3d` | Student, teacher, admin, shared UI, and institute website trees inspected. |

The exact backend development source scan found 76 production controller files and 354 mapping annotations in those files; 453 annotations were found across production Java source including Feign clients. The 354 count includes class-level mappings and is not an endpoint count. The inventory below is route-family-level and includes internal routes where material; test-only probes and actuator/framework endpoints are excluded. Overloaded methods, implicit HTTP methods, and gateway rewrites still need effective-route reconciliation.

## Backend controller and route-family inventory

For each listed family, map every declared HTTP method/path in backend source to an exact test class and method in `docs/test-case-mappings.csv`. The family names below are not a claim that every individual route is covered.

| Backend service | Controller/route families observed | Existing test area | Assessment / work to verify |
|---|---|---|---|
| Authentication | `/api/v1/auth`: login, forgot-password/verify/reset, refresh, logout, JWKS; internal user/password operations | `auth`, `user/AuthenticationIT` | Reconcile public vs internal endpoints, token/error branches, and reset happy path with cases. |
| User | admin registration/invitations/status/delete; student/teacher registration and lists; internal auth profile activation/login; user analytics | `user` | Map each role and lifecycle operation; internal auth profile requires service identity. |
| Course | course CRUD/my-courses/lookup/bulk; modules, lessons, materials and upload; grades/monthly fees; progress; announcements; teacher dashboard/search/gradebook/directory/analytics/student summary; marketplace; internal lookup/scope/analytics | `course` | Large surface. Add route-level reconciliation and ownership checks; distinguish internal APIs and multipart routes. |
| Enrollment | create/list/update/delete; self-enrollment; student/course membership and unenrolled queries; internal analytics | `enrollment`, `e2e` | Verify each role, status transition, deletion effects, and course/communication dependencies. |
| Assignment | assignment CRUD/publish/close; student list; submissions/drafts/submit/list; grading/return; assignment attachments; submission files; internal analytics/reconciliation | `assignment`, `e2e` | Map each route and multipart/download path; verify fixture-gated writes and cleanup. |
| Quiz | quiz CRUD/publish/read/student projection/results/teacher list; questions; attempts/answers/submit; AI generation; teacher gradebook; analytics; internal reconciliation | `quiz`, `ai` | Reconcile route families across assignment service; include attempt state and role checks. |
| Material | upload/marketplace/AI sources/read/download/update/archive/delete; profile images; maintenance audit/cleanup; internal associations | `material`, `assignment` | Mark operational/internal APIs separately; verify signed-download and upload route coverage. |
| Attendance | session create/list/detail/records/complete/cancel/delete; student/course summaries; analytics; calendar reconciliation | `attendance` | Map all state transitions and internal projection route. |
| Calendar | event list/upcoming/detail/update/cancel; venues; internal assignment/quiz/class/attendance/exam projections and reconciliation | `calendar` | Several internal projection routes; verify authentication model and external-facing route coverage independently. |
| Communication | group detail/settings/membership/archive/unarchive; messages/attachments; current-user probe; WebSocket broker routes | `communication`, `websocket` | Verify member ownership, attachment lifecycle, and live broker message delivery. |
| Exam | exam lifecycle/schedule/marking/results/summary; student upcoming/detail/results; attachments; analytics; calendar reconciliation | `exam` | Multiple state routes and multipart files; map student visibility and teacher ownership. |
| Payment | course payment, self/detail/status, admin list/detail/refund, Stripe webhook, internal successful-payment lookup, analytics | `payment` | Test mode only; map webhook signature/replay/idempotency and internal service identity. |
| Notification | list/unread count/read/read-all/delete | `notification` | Map ownership and Kafka/email behavior; provider tests remain controlled. |
| AI | chat, conversations/history/update/delete, health/current user, lesson/material summaries, quiz generation, WebSocket | `ai`, `websocket` | Map deterministic provider, ownership, validation, SSE/WebSocket behavior. |
| Analytics | admin overview and service analytics projections | `course`, `user`, service analytics packages | Identify all gateway-exposed analytics routes versus internal aggregation clients; test authorization and aggregation contracts. |
| Gateway / discovery / actuator | Gateway route rewrites, public/protected probes, service readiness | `api`, `auth`, `environment`, `e2e`, `staging` | Compare gateway route configuration with controller paths and verify externally visible paths. |

## Frontend user-flow inventory

Frontend snapshot contains `student`, `teacher`, `admin`, shared UI, and an institute website. These are candidate workflows from inspected page/component/API filenames; verify the current routes and acceptance criteria against SRS before making them mandatory.

| App/role | User flows to cover in browser E2E |
|---|---|
| Public institute site | Home/localized pages, resource listing/detail/download, contact form, pre-registration, privacy/terms pages. |
| Student | Login/session/logout; dashboard; explore and enroll in courses; my courses and course workspace; modules/lessons/materials; assignments/submissions; quizzes/attempt/results; exams; attendance/calendar; grades; messages/realtime attachments; notifications; AI assistant; profile/settings. |
| Teacher | Login/session/logout; dashboard/search; course and curriculum/module/lesson management; student directory/details; materials; assignments/submissions/grading; quizzes/questions/AI quiz generation/results; attendance; exams/attachments; calendar; communication/realtime; notifications; analytics; profile/settings. |
| Admin | Login/session/logout; dashboard/analytics; students/teachers/admins and invitations; courses/enrollments; grades/results; assignments/quizzes/exams/attendance; materials; calendar; communication; notifications; AI assistant; audit/activity; settings/profile. |

The inspected frontend file list did not establish an existing Playwright/Cypress test suite. Check each app's `package.json`, CI workflows, and test directories in the exact development revisions. Browser E2E ownership (frontend repo vs this repo) must be recorded; API E2E alone does not verify UI interaction.

## Existing test-repository areas

| Test area | Package/classes | Step 1 status |
|---|---|---|
| API clients and static client tests | `api` | Present; client input/serialization tests do not replace live endpoint tests. |
| Auth/user | `auth`, `user` | Present; environment and account fixture dependent. |
| Feature integration | `course`, `enrollment`, `assignment`, `material`, `quiz`, `attendance`, `calendar`, `communication`, `exam`, `payment`, `notification`, `ai` | Present; exact route coverage must be reconciled, many tests gated. |
| Cross-service E2E | `e2e` | Gateway/API journeys present; browser journeys not established. |
| Quality layers | `contracts`, `security`, `resilience`, `performance`, `staging`, `environment` | Present as suites/docs/workflows; execution/evidence is conditional or manual for several layers. |
| Traceability/evidence | `docs/test-case-mappings.csv`, `tools/test-evidence.py` | 224 mapping rows in the exact tests development revision; reconcile against the current backend route set and all integration methods. |

## Existing mapped cases by test area

Counts below are CSV rows on `tests/development` at `c51984f` (224 total). These are case mappings, not endpoint counts: one route can have several cases and one case can cover multiple operations. The backend scan found 336 mapping annotations inside production `*Controller.java` files (class-level plus method-level); these figures cannot be compared as a coverage percentage without expanding class mappings and route composition.

| Mapping service key | Case rows | Source area / next reconciliation |
|---|---:|---|
| ai | 18 | AI HTTP, content, provider and WebSocket scenarios; map every public AI controller method. |
| assignment | 36 | Assignment, submissions, grading and files; reconcile all controller methods. |
| attendance | 7 | Sessions and summaries; reconciliation needed against all lifecycle routes. |
| auth | 18 | Authentication and token flows; separate user-facing auth from internal identity routes. |
| calendar | 8 | Calendar events and venues; internal projection routes are separate. |
| communication | 3 | HTTP messaging; group/membership and broker delivery need method-level reconciliation. |
| course | 41 | Course content, teacher portal, grades/progress/marketplace; largest broad surface. |
| enrollment | 24 | Membership and enrollment lifecycle; separate internal analytics. |
| exam | 7 | Lifecycle/results/attachments; reconcile each transition. |
| material | 7 | Upload/read/archive/download plus profile image, maintenance, internal links. |
| notification | 7 | Read/unread/ownership routes and event delivery. |
| payment | 18 | Student/admin, webhook and refund paths; provider writes remain controlled. |
| quiz | 13 | Management/attempt/questions/results/gradebook/analytics routes. |
| security | 3 | Cross-service authorization regressions. |
| staging | 4 | Read-only staging smoke routes. |
| user | 10 | Self-service, admin, lookup and internal identity operations. |

## Route-level comparison artifact

`backend-effective-route-inventory.csv` contains 235 HTTP controller route variants after expanding the two class-level base paths on `UserAnalyticsController`; the `HelloController.helloCourse` method is the service-root `GET /` route (its `@RestController("/hello")` value is a bean name, not a path). Two Exam controller operations share the same verb/path but are separate JSON and multipart mappings. `backend-gateway-route-definitions.csv` records the explicit predicates/filters inspected in backend `api-gateway.yml`; `backend-gateway-effective-route-matrix.csv` resolves every controller route to explicit Gateway ingress path(s) or a service-discovery fallback. `backend-route-test-matrix.csv` includes those Gateway columns alongside linked request/assertion evidence or an explicit gap status. `backend-websocket-route-test-matrix.csv` separately records the two STOMP handshake endpoints, Gateway predicates, broker destinations, exact test methods, and uncovered protocol behaviors. This is source-level reconciliation, not runtime Gateway verification: 175 routes have one explicit same-service rule, 19 match multiple same-service rules (AI compatibility alias and duplicate teacher route definitions included), 36 use discovery-fallback only, and 5 fallback routes collide with an explicit rule targeting another service. Those five path collisions are listed in the Gateway matrix. Fallback ingress includes the service-ID prefix; the controller path alone is not that Gateway URL.

`endpoint-test-matrix.csv` is the plan-facing matrix with one row per public/user-facing, admin-facing, teacher-facing, or service-root HTTP endpoint. It groups Quiz controllers under the plan's separate `quiz` category even though the backend hosts them in `assignment-service`, giving all 15 planned service categories. The matrix contains 187 public-facing endpoint rows from the 235 controller route variants; the remaining 48 internal/operational routes stay in `backend-internal-route-auth-matrix.csv`. `required_role` is taken from method/class annotations, effective service security rules, and source-verified domain ownership rules; no unresolved role-source rows remain. `missing-test-work.md` records category-level evidence gaps. `NOT EVIDENCED` means this route audit did not link a route-specific assertion for that category; it does not prove no related test exists, and linked response codes do not alone prove business-level payload/state correctness.

The HTTP route-test matrix records exact request verb/path, the JUnit case/class/method, and a linked response assertion. Positive, negative-only, and mixed rows are distinguished. The WebSocket matrix records only CONNECT authentication assertions; it does not treat handshake success as coverage for Gateway routing, subscription/send authorization, or message delivery. Statuses are based on source correlation, not a live integration run. A mapped assertion proves the test source exercises that request/handshake; it does not prove that the test passes against a deployed backend.

## Known gaps requiring later implementation

The following are scope findings, not assertions that the product behavior is broken:

| Area | Gap established by current evidence | Required follow-up |
|---|---|---|
| Test-to-case traceability | Baseline audit found 65 annotated integration methods without mappings. This branch assigns `TC-TRACE-001`–`TC-TRACE-065`, maps each exact class/method, and links each case to applicable SRS IDs or explicitly marks `SRS-NOT-EXPLICIT`. | Requirement traceability for these 65 cases is reconciled. Actual pass/fail status still requires JUnit execution; `SRS-NOT-EXPLICIT` cases remain API/security test scope unless the SRS is revised. |
| HTTP endpoint coverage | The source audit expands 235 controller mapping variants; all 235 now have linked request/assertion evidence and zero unmatched routes. 135 have both positive and negative assertions, 47 positive-only, and 53 negative-only (including opt-in unauthenticated probes for the 26 formerly unmatched internal routes). Gateway reconciliation records 175 single same-service rules, 19 multiple same-service rules, 36 fallback-only routes, and 5 paths that explicitly match a different service. | Live endpoint execution is still unverified. Add safe positive workflows for internal mutation/projection routes only after approved service credentials and test-owned fixtures are available; add positive cases for negative-only routes where the contracts permit it. Review the five cross-service Gateway path collisions before claiming runtime end-to-end coverage. See the route, Gateway, and internal-auth matrices. |
| WebSocket coverage | Both configured STOMP endpoints (`/ws`, `/ws/ai`) and Gateway routes are reconciled in `backend-websocket-route-test-matrix.csv`. Opt-in source tests now cover direct/Gateway handshake, anonymous/role denials, destination authorization, echo and safe AI error delivery; Communication has an active-member persisted-message workflow with cleanup. | Run the opt-in probes on approved non-production services. Provider-backed successful AI chat and communication group delivery require their specified deterministic provider and owned membership fixtures; these source tests are not live-pass evidence. |
| Internal endpoints | All internal HTTP routes now have source-linked request/assertion evidence. The 26 formerly unmatched routes have opt-in unauthenticated probes asserting `401`/`403`; existing analytics positives cover overview and configured course-summary routes. Shared-secret and service-token positive workflows and data-changing reconciliation/projection success paths still need approved credentials and owned fixtures. | Use the endpoint-specific model and current status in `backend-internal-route-auth-matrix.csv`; only token-protected contracts require internal secrets. Do not treat every `/internal/` endpoint as service-token-only. |
| Frontend browser E2E | `student`, `teacher`, `admin`, and public institute site have feature flows, but no browser test framework/files were found in frontend development. | Team must assign Playwright/Cypress ownership; add critical student, teacher, admin and public-site journeys. |
| Live execution evidence | Many API tests require services, accounts, fixtures, and opt-in flags; test inventory does not establish a successful live run. | Run approved non-production integrations, preserve JUnit evidence, and distinguish pass/fail/skip/not-run. |
| Branch/version provenance | Backend/frontend/tests development commits were inspected separately; no release/deployment manifest proves they are one compatible deployment set. | Record the exact three SHAs and staging deployment revision for each integrated run. |

The exact 65 methods were recomputed by comparing annotated JUnit methods with the baseline mapping CSV. Every method has a stable ID (`TC-TRACE-001`–`TC-TRACE-065`), exact class/method mapping, and a requirement field reconciled against the complete 63-page Mentify SRS (`SENG-34213/documents`, `main` commit `e504b3067d7e817f381a5bb388a392ffae633082`); every cited SRS requirement ID was checked against the document (zero unknown IDs). Cases with no corresponding SRS feature are explicitly marked `SRS-NOT-EXPLICIT` (25 cases). The cases are documented in [step-1-traceability-cases.md](step-1-traceability-cases.md), and the before-change list remains in [step-1-unmapped-integration-methods.csv](step-1-unmapped-integration-methods.csv). This closes metadata traceability only, not test execution or API route coverage.

## Step 1 acceptance checklist

- [x] PDF test requirements extracted and mapped to ownership/layers.
- [x] Backend controller families inventoried from local snapshot.
- [x] Frontend app roles and major flow candidates inventoried.
- [x] Existing test packages, mapping file, and known audit status inspected.
- [x] Confirm remote backend and frontend `development` SHAs and inspect those exact source trees.
- [x] Check frontend development tree for Playwright/Cypress/Jest/Vitest test files; none were found by repository file-name inventory. Browser-test coverage is therefore an open implementation gap, and ownership must be assigned.
- [x] Assign IDs and exact class/method mappings to all 65 previously unmapped methods; document the cases and retain the before-change list.
- [x] Expand class-level route aliases, resolve the HelloController service-root path, separate JSON and multipart Exam mappings, and reconcile Gateway explicit routes and discovery fallback.
- [x] Map each of the 235 controller route variants to JUnit request/assertion evidence; zero routes remain unmatched or assertion-review-pending. Internal denial probes remain opt-in and source-linked, not live-run evidence.
- [x] Add fixture-gated or read-only integration requests for all 15 previously unmatched user-facing routes; confirm each is linked in the current HTTP matrix.
- [x] Reconcile all 235 controller routes against explicit Gateway rules, compatibility rewrites, and service-discovery fallback ingress paths.
- [x] Inventory both configured STOMP endpoints and their Gateway routes separately; map the opt-in CONNECT assertions and record protocol-level coverage gaps.
- [x] Validate every SRS ID cited by the 65 newly mapped methods against the full SRS; all cited IDs exist and unsupported mappings are marked `SRS-NOT-EXPLICIT`.
- [x] Generate the plan-required `docs/endpoint-test-matrix.csv` and `docs/missing-test-work.md`; cover the 15 plan categories, link each public endpoint to existing request/assertion evidence, record required access rules, and expose unproven test categories without marking them covered.
- [x] Inspect the exact frontend development tree and record the candidate role-based flows. The repository file inventory found no Playwright/Cypress/Jest/Vitest tests; browser E2E coverage therefore remains implementation work.

## Step 1 closeout decision

The Step 1 source inventory and route reconciliation are complete: all 65 previously unmapped methods have unique IDs, exact JUnit class/method mappings, and validated SRS IDs or explicit `SRS-NOT-EXPLICIT`; all 235 HTTP controller variants have Gateway resolution and linked request/assertion evidence; zero route rows are unmatched or awaiting assertion review. Current source-status split is 135 positive+negative, 47 positive-only, and 53 negative-only. Follow-up WebSocket coverage has been added in source and the opt-in integration classes compile, but the tests have not been run against deployed services. Live Gateway route verification, internal positive workflows requiring approved service secrets/fixtures, provider-backed AI success chat, frontend browser E2E, and a deployed integration run remain outstanding. The Maven unit suite passed.

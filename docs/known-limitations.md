# Known Limitations

## Environment and fixtures

- Static tests do not start the backend, Auth Service, databases, Kafka, Mailpit,
  or external providers.
- Live tests need a running, approved non-production environment. Readiness
  fails clearly for a required service that is unavailable.
- E2E tests require `TEST_E2E_ENABLED=true`; the course journey also requires
  `TEST_E2E_COURSE_ID`.
- The complete Step 10 API journey is gated by write opt-in, role credentials,
  a grade fixture, and all required services. Assignment deletion is soft and
  no submission-delete endpoint exists, so one synthetic graded submission
  remains on its archived assignment after cleanup.
- Frontend `development` currently posts a singular `courseId` to student
  self-enrollment; the backend contract requires a `courseIds` list. Resolve
  this payload mismatch before frontend browser-flow verification.
- Provider journeys and writes are disabled unless explicitly approved.
- Some workflows require existing non-production IDs because shared data must
  not be deleted by tests.
- Cleanup uses only supported backend endpoints. Where no endpoint exists,
  cleanup remains disabled or dry-run and the limitation is documented.

## Stability review

- No arbitrary `Thread.sleep` synchronization was found in the reviewed test
  sources; readiness uses bounded polling and configured timeouts.
- No intermittent failure was reproduced during TEST-023 static verification.
  Live-environment flakiness requires the services and fixtures to assess.
- Assertion retries are not used to hide product failures.

## Security

- Credentials, tokens, provider keys, and reset links are environment/CI-secret
  inputs. `.env.example` contains placeholders only.
- Provider tests are opt-in and must use test mode. Production infrastructure
  and real personal data are out of scope.
- Logs and reports must not expose passwords, refresh tokens, authorization
  headers, or sensitive response bodies.

## CI

- Default CI verifies the static suite with live flags disabled; it does not
  prove that every backend service is healthy. `mvn verify -Pintegration` now
  marks all live test classes skipped unless `TEST_ENV_ENABLED=true`.
- Live integration execution needs separately configured services and secrets.
- JUnit tags are reserved vocabulary, but class-pattern selection is the
  currently verified Maven mechanism. Tag filtering must not be claimed until
  it is configured and tested.
- The static tests-repository workflow does not instrument backend services;
  its generated evidence therefore reports backend code coverage as unavailable.
- Per-service backend JaCoCo artifacts are produced by the separate backend
  workflow. A combined report requires downloading the artifacts from the same
  backend run and passing them to the evidence generator. Coverage gates may
  differ by module and source scope; the report does not infer gate outcomes.
- Automated backend-artifact download is available only on manual tests-CI runs
  and requires the selected backend run ID, commit SHA, and the
  `BACKEND_ARTIFACTS_TOKEN` repository secret. Routine static CI does not use it.
- The test-case mapping CSV currently has 472 rows, but the latest evidence
  register still has 197 documented cases marked `Not Mapped`. Executed methods
  have mappings in the recorded run; CI now fails if a future run executes an
  unmapped method. Reconcile the remaining catalog entries against the actual
  test source or move checklist-only entries out of functional case catalogs.
- Integration tests absent from a run are reported as not run; generated
  registers are run-specific evidence and do not by themselves constitute a
  complete sprint test register.
- TEST-028 staging checks are manual-only and require repository administrators
  to configure/protect the `staging` GitHub Environment, its host-suffix and URL
  variables, and required reviewers. No live staging run has been performed or
  claimed by this change. Authenticated identity probes, deployment, write
  workflows, production verification, and full release certification remain out
  of scope.

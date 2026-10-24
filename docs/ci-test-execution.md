# CI test execution

The repository workflow `.github/workflows/test.yml` runs on pushes and pull requests. It uses Java 17, Maven's
dependency cache, and `mvn clean verify` with `TEST_ENV_ENABLED=false` and `TEST_E2E_ENABLED=false`, so static tests
do not require live backend services or secrets.

## Reports

The workflow uploads:

- `target/surefire-reports/` for unit/static tests.
- `target/failsafe-reports/` for integration-profile tests when they are executed.
- `target/test-evidence/` with the executed register, evidence manifest, and an
  explicit coverage availability summary.

These artifacts are available to pull-request reviewers even when a later step fails.

The static tests-repository workflow does not instrument backend services, so
its coverage summary states that backend coverage was not supplied. It must not
report the test repository's code coverage as backend coverage. The backend
repository's CI publishes product JaCoCo reports and applies only the coverage
checks configured for the relevant module and source scope; download those
artifacts from the corresponding backend workflow run to produce a combined
local evidence bundle. See
[`test-execution.md`](test-execution.md#test-register-and-backend-coverage-evidence).

To attach backend reports to a CI evidence artifact, manually run the
tests-repository “Test Repository CI” workflow with
`include_backend_coverage=true`, the backend Actions run ID, and the backend
commit SHA. Configure the repository secret `BACKEND_ARTIFACTS_TOKEN` with
read-only access to backend Actions artifacts. The workflow downloads the
unit and integration JaCoCo artifacts from that exact backend run and records
both revisions in `evidence.json`. Leave the option disabled for normal pushes
and pull requests; those runs require no backend token or live backend. When
enabled, the workflow retains the downloaded JaCoCo inputs as a separate
artifact alongside the generated summary and test register.

Detailed test-case IDs are read from `docs/*-test-cases.md`. Until mapped in
`docs/test-case-mappings.csv`, they appear as `Not Mapped` and their SRS
requirement is `UNMAPPED`. The CSV currently contains only its header, so
complete requirement traceability is not yet claimed. JUnit methods without a
case mapping appear with an `UNMAPPED-*` identifier. Mapped cases with no result
stay `Not Run`; failed and skipped cases remain distinct. Reports omit
exception bodies to reduce the risk of including sensitive response data.

## Live execution

Live gateway journeys should run only in a separately controlled environment with test-only application accounts,
approved course/enrollment fixtures, safe payment/AI providers, and all required services reachable. Configure secrets
through the CI secret store; never put values in YAML, `.env.example`, logs, or command output. Set
`TEST_E2E_ENABLED=true` only for that controlled job and include the exact required service list.

## Staging release smoke

`.github/workflows/staging-smoke.yml` is a separate `workflow_dispatch`-only
workflow bound to the protected GitHub Environment named `staging`. It runs
only `StagingSmokeIT`, requires the operator to type the exact confirmation,
and does not deploy anything. Configure the environment variables and required
reviewers described in [staging-release-smoke.md](staging-release-smoke.md).
The Maven run receives no credentials and performs GET-only probes; it validates
the HTTPS host against the configured staging DNS suffix and never follows
redirects. JUnit and `target/staging-evidence/summary.md` are uploaded even when
the smoke checks fail. A successful workflow is only bounded staging smoke
evidence, not a release certification.

## Failure diagnosis

1. Check the readiness message and required-service list.
2. Inspect Surefire/Failsafe XML and text reports.
3. Confirm the gateway URL rather than an internal service URL was used.
4. Confirm the test account role and fixture ownership.
5. Confirm provider dependencies are in safe test mode.
6. Re-run the focused journey locally with the same non-secret configuration.

Unavailable optional providers should produce documented skips. Required static test failures must fail CI.

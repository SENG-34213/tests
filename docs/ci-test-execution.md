# CI test execution

The repository workflow `.github/workflows/test.yml` runs on pushes and pull requests. It uses Java 17, Maven's
dependency cache, and `mvn clean verify` with `TEST_ENV_ENABLED=false` and `TEST_E2E_ENABLED=false`, so static tests
do not require live backend services or secrets.

## Reports

The workflow uploads:

- `target/surefire-reports/` for unit/static tests.
- `target/failsafe-reports/` for integration-profile tests when they are executed.

These artifacts are available to pull-request reviewers even when a later step fails.

## Live execution

Live gateway journeys should run only in a separately controlled environment with test-only Keycloak accounts,
approved course/enrollment fixtures, safe payment/AI providers, and all required services reachable. Configure secrets
through the CI secret store; never put values in YAML, `.env.example`, logs, or command output. Set
`TEST_E2E_ENABLED=true` only for that controlled job and include the exact required service list.

## Failure diagnosis

1. Check the readiness message and required-service list.
2. Inspect Surefire/Failsafe XML and text reports.
3. Confirm the gateway URL rather than an internal service URL was used.
4. Confirm the test account role and fixture ownership.
5. Confirm provider dependencies are in safe test mode.
6. Re-run the focused journey locally with the same non-secret configuration.

Unavailable optional providers should produce documented skips. Required static test failures must fail CI.

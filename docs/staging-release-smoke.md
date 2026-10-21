# Staging release smoke (TEST-028)

This is a bounded, manual-only, read-only smoke suite for an explicitly
approved Mentify staging deployment. It is not a deployment workflow, full
regression run, performance test, penetration test, production test, or release
certification. Production targets are forbidden.

## Repository administrator setup

Create a GitHub Environment named `staging`, restrict which branches may use
it, and configure required reviewers so approval is required before the job can
read its environment variables. Set these environment variables (never commit
their real values):

| Variable | Required value |
|---|---|
| `STAGING_BASE_URL` | HTTPS origin/base path of the staging API Gateway |
| `STAGING_KEYCLOAK_BASE_URL` | HTTPS origin/base path of the staging Keycloak endpoint |
| `STAGING_ALLOWED_HOST_SUFFIX` | DNS suffix shared by both staging hosts, without scheme or leading dot (for example, `staging.example.test`) |
| `KEYCLOAK_REALM` | Staging realm name; defaults to `mentify` if unset |
| `STAGING_READINESS_TIMEOUT_SECONDS` | Optional overall readiness deadline, 1–600 seconds; default 90 |
| `STAGING_POLL_INTERVAL_SECONDS` | Optional readiness retry interval, 1–600 seconds; default 3 |
| `STAGING_REQUEST_TIMEOUT_SECONDS` | Optional per-request timeout, 1–600 seconds; default 8 |

Do not configure a production URL or production-like DNS suffix. The suffix
check must be narrow enough to identify only the approved staging domain. The
workflow has no deployment permissions, no production environment, and no
credential requirement. If future smoke tests need authentication, first design
an explicit least-privilege identity and secret-handling review; do not add a
developer/admin token to this workflow.

## Operator procedure

1. Confirm that the target is the approved, non-production staging deployment
   and uses synthetic data. Confirm the backend commit deployed there.
2. From the repository Actions tab, manually dispatch **Staging Read-only
   Release Smoke** on an approved feature/release candidate branch.
3. Enter the backend deployment commit SHA (7–40 hexadecimal characters).
4. Type exactly `RUN STAGING READ-ONLY SMOKE` in the confirmation input.
5. Wait for the required `staging` Environment reviewer approval. If the
   environment or target cannot be verified, reject/cancel the run.
6. Inspect the workflow result, Failsafe JUnit report, and
   `staging-evidence-<run>-<sha>` artifact. Link the workflow run and deployed
   revision in the release review; do not copy credentials or raw payloads.

The run validates the configured HTTPS targets and allowlisted DNS suffix
before sending requests. It checks gateway `/actuator/health`, the public
`/api/v1/auth/public-test` read-only probe (HTTP 200), required OpenID discovery
fields and issuer/endpoint hosts, and that `/api/v1/auth/protected-test` rejects
an anonymous request with 401 or 403. Readiness retries have a finite deadline.
All requests are GET requests; redirects are not followed and response bodies
are discarded except the bounded (1 MiB) OpenID document, which is parsed in
memory and never included in evidence. No authenticated
probe, business write, fixture cleanup, upload, payment, email, or provider call
is performed.

## Evidence and outcomes

The workflow run and backend deployment SHA bind the evidence to the tested
candidate. The markdown summary records start time, run ID, test-repository
revision, deployment revision, and check outcomes. It intentionally omits
target URLs, request/response bodies, and credentials. Failsafe XML and the
summary are uploaded even on test failure for 30 days. JUnit test failure is
authoritative; a missing/blocked check is not a pass. A successful smoke means
only that these three bounded probes completed successfully at that time.

If readiness times out, check the named dependency, staging DNS/certificate,
gateway routing, Keycloak realm configuration, and whether the protected probe
is still meant to require authentication. Do not work around a failed probe by
pointing the run at production or weakening the host check. Follow the team's
staging incident/escalation process and stop the release acceptance if the
deployment identity is uncertain.

## Local policy tests

The target-policy and HTTP-probe unit tests run in ordinary static CI and need
no live services:

```powershell
mvn --batch-mode clean verify
```

Do not run `StagingSmokeIT` locally unless an authorized staging environment
has been explicitly approved and all confirmation/target settings are supplied.
No live staging run is implied by passing static CI.

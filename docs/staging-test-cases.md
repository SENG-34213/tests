# Staging release-smoke test cases (TEST-028)

These checks run only from the protected, manual `staging-smoke.yml` workflow.
Static policy/probe tests run in ordinary CI; no case in this table implies a
live staging execution.

| ID | Scenario | Expected result | Evidence |
|---|---|---|---|
| TC-TEST-028-01 | Confirmation missing or incorrect | Configuration fails before any request | `StagingTargetPolicyTest` |
| TC-TEST-028-02 | Target is HTTP, contains credentials/query/fragment, uses a non-443 port, or is outside the DNS suffix | Target is rejected before network access | `StagingTargetPolicyTest` |
| TC-TEST-028-03 | Host suffix is an IP address or deployment revision is malformed | Configuration fails closed | `StagingTargetPolicyTest` |
| TC-TEST-028-04 | HTTP response redirects to another host | Client returns the redirect status without following it | `StagingHttpProbeTest` |
| TC-TEST-028-05 | Probe server exceeds request timeout | Probe fails within the configured timeout | `StagingHttpProbeTest` |
| TC-TEST-028-06 | Staging API Gateway health endpoint is ready | HTTP 2xx within the bounded readiness deadline | `StagingSmokeIT` |
| TC-TEST-028-07 | JWT signing key set is available | HTTP 200 JSON contains a public RSA RS256 signing key with `kid`, `n`, and `e` | `StagingSmokeIT` |
| TC-TEST-028-08 | Auth Service health endpoint is ready | HTTP 2xx within the bounded readiness deadline | `StagingSmokeIT.authServiceHealthIsReadyWithinBoundedDeadline` |
| TC-TEST-028-09 | Protected API probe is requested without a token | HTTP 401 or 403; no token is sent | `StagingSmokeIT` |
| TC-TEST-028-12 | Default static CI runs without staging settings | Static suite passes and makes no live-environment claim | Surefire and CI artifacts |
| TC-TEST-028-13 | Non-staging environment | Configuration fails before any request | `StagingTargetPolicyTest` |
| TC-TEST-028-14 | IP address is rejected as the allowed staging suffix | Configuration fails closed | `StagingTargetPolicyTest` |

The live rows remain unverified until an authorized reviewer approves and runs
the workflow against the configured non-production staging deployment. The
summary is not full release certification.

## Manual workflow evidence

- **TC-TEST-028-10 — Incomplete smoke run:** During a staging workflow run, confirm a failed required check fails the JUnit job and records remaining checks as not run/blocked.
- **TC-TEST-028-11 — Sanitized summary:** After an approved staging run, confirm the generated summary contains only run/revision/check status and no target URL, request body, response body, or credential.

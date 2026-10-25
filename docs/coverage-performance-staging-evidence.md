# Coverage, Performance, and Staging Evidence

## Backend coverage configuration review

The backend `development` source checked for this work has JaCoCo agent and
report executions in the root Maven build. The workflow declares per-service
`jacoco-unit-*` and `jacoco-integration-*` artifact uploads from
`target/site/jacoco/`.

The backend POM's visible `check-auth-coverage` rule requires 80% line and
branch coverage for the configured authentication/security class includes
(`common/security`, `AuthTestController`, and `GatewaySecurityConfig`). The
review found no 90% branch rule for critical business logic and no repository
configuration identifying which business classes are in that critical scope.
Therefore the existing backend rule does not establish either a global 80%
target for all new code or the 90% critical-logic target. Backend owners must
define the intended changed-code/critical-class scope and enforce those rules
in backend CI.

The latest successful backend `development` run inspected for this task,
[run 40108590104](https://github.com/SENG-34213/Mentify-backend/actions/runs/40108590104)
at SHA `9b7ae670e22d38ee6be0ffc166e9a55f65b677ab`, actually contains only
`surefire-unit-*` artifacts; it has no `jacoco-unit-*` artifacts. The unit CI
job runs Maven `test`, while the JaCoCo `report` execution is bound to `verify`,
so that report is not generated in the unit job. The development push also did
not run the PR/main-only integration jobs. The backend workflow must generate
and upload JaCoCo XML in the unit job and produce a matching integration run
before this repo can import complete coverage evidence.

The test repository can summarize JaCoCo XML; it cannot instrument backend
classes or independently prove that the backend's Maven gate passed. Its manual
coverage path now checks that the selected backend Actions run succeeded, that
the supplied SHA matches that run, and that the artifacts contain JaCoCo XML.
The generated summary and evidence manifest record the backend run ID and SHA.
Coverage percentages remain measurements; the backend CI check is authoritative
for gate outcomes.

To produce this evidence after backend CI publishes JaCoCo XML, dispatch **Test
Repository CI** with backend unit coverage enabled, the successful backend
Actions run ID, and its commit SHA. Select integration coverage only when that
same backend run includes integration JaCoCo artifacts. The repository must
have `BACKEND_ARTIFACTS_TOKEN` with read access to backend workflow artifacts.
Reports must come from the same backend run and commit.

## Performance evidence

The bounded performance suite and metric assertions exist. The manual workflow
now requires the protected `dedicated-performance` environment, a confirmation
phrase, and the deployed backend SHA. Configure required reviewers and the
`PERFORMANCE_BASE_URL`, `PERFORMANCE_ALLOWED_HOST_SUFFIX`, and optional
`PERFORMANCE_COURSE_ID` environment variables. Configure a synthetic student
account with `STUDENT_TEST_USERNAME` and `STUDENT_TEST_PASSWORD` secrets.

The workflow rejects non-HTTPS, credential-bearing, out-of-suffix, and
production-looking targets before requests. Readiness and measured API calls
use the same configured Gateway. It publishes JUnit reports and a sanitized
summary with request counts, error rate, latency percentiles, timeouts, and
throughput, tied to the test commit and backend deployment revision.

No performance run was performed as part of this change because no approved
dedicated target or scoped test credentials are configured in this workspace.
The workflow result and its artifact are still required for actual performance
evidence. Run the bounded baseline at least twice before adopting a baseline;
the suite is not a load/stress or capacity test.

## Staging evidence

The staging smoke remains a separate manual, read-only workflow. Before use,
administrators must configure and protect the `staging` GitHub Environment,
require reviewer approval, and set the documented Gateway/Auth URLs and narrow
host suffix. Dispatch it with the exact confirmation phrase and deployed
backend SHA. Its uploaded JUnit report and sanitized summary are the staging
evidence; a configured workflow without a successful run is not a pass.

No staging run was performed as part of this change because no approved staging
target was configured here. The smoke verifies bounded health, JWKS, and
anonymous-access behavior only; it is not a full authenticated E2E or release
certification.

# Test Execution

## Test levels

| Level | Naming | Command | Backend required |
|---|---|---|---|
| Static/unit support | `*Test` | `mvn clean test` | No |
| Service integration | `*IT`, `*IntegrationTest` | `mvn verify -Pintegration` | Yes |
| Gateway E2E | `e2e/*IT` | `mvn -Dtest="*Gateway*IT,*JourneyIT" verify -Pintegration` | Yes |

Surefire runs static `*Test` classes. Failsafe in the `integration` profile
runs `*IT` and `*IntegrationTest` classes.

## Static verification

```powershell
mvn clean test
mvn verify
```

The project compiles, static tests pass, Checkstyle runs, and no backend service
or secret is required.

## Live integration verification

```powershell
$env:TEST_ENV_ENABLED="true"
$env:TEST_REQUIRED_SERVICES="API_GATEWAY,AUTH_SERVICE,USER_SERVICE"
mvn verify -Pintegration
```

Run only against an approved non-production environment. A missing required
service produces a readiness failure naming that service. Optional fixture tests
skip with their configuration reason.

## Service selection

```powershell
mvn -Dtest="*User*IT" verify -Pintegration
mvn -Dtest="*Course*IT" verify -Pintegration
mvn -Dtest="*Enrollment*IT" verify -Pintegration
mvn -Dtest="*Payment*IT" verify -Pintegration
mvn -Dtest="*Notification*IT" verify -Pintegration
mvn -Dtest="*Ai*IT" verify -Pintegration
mvn -Dtest="*Gateway*IT,*JourneyIT" verify -Pintegration
```

Class suffix and package conventions are the supported selection mechanism.

## E2E configuration

```powershell
$env:TEST_E2E_ENABLED="true"
$env:TEST_E2E_COURSE_ID="approved-non-production-course-id"
$env:TEST_E2E_PROVIDER_JOURNEYS_ENABLED="false"
$env:TEST_REQUIRED_SERVICES="API_GATEWAY,AUTH_SERVICE,USER_SERVICE,COURSE_SERVICE,ENROLLMENT_SERVICE,ASSIGNMENT_SERVICE,MATERIAL_SERVICE,PAYMENT_SERVICE,NOTIFICATION_SERVICE"
mvn -Pintegration verify "-Dit.test=*Gateway*IT,*JourneyIT,*CrossService*IT"
```

The complete student/teacher/admin write journey is separately gated by
`TEST_E2E_WRITE_ENABLED=true` and `TEST_E2E_COURSE_GRADE_ID`; provide dedicated
student, teacher, and admin credentials. Run it only in a disposable
non-production environment because the backend has no submission deletion API.
Provider journeys remain disabled unless provider test mode and write flags are
explicitly approved. Never put credentials or provider keys in source files.

## Security and resilience regression tests

```powershell
$env:TEST_ENV_ENABLED="true"
$env:TEST_SECURITY_ENABLED="true"
mvn -Dtest="*Security*IT" verify -Pintegration

$env:TEST_RESILIENCE_ENABLED="true"
$env:FAULT_INJECTION_ENABLED="true"
mvn -Dtest="*FailureIT,*TimeoutIT,*ResilienceIT,*IdempotencyIT" verify -Pintegration
```

Security tests skip when security execution is disabled. Resilience and
provider-failure tests skip unless controlled fault-injection infrastructure is
enabled. They never target production and never use real provider credentials.

## Performance baseline

Performance tests are disabled by default and are separate from functional CI:

```powershell
$env:PERFORMANCE_TEST_ENABLED="true"
$env:PERFORMANCE_ENVIRONMENT="dedicated-local"
$env:PERFORMANCE_MEASURED_REQUESTS="30"
$env:PERFORMANCE_CONCURRENCY="2"
mvn -Dtest="*PerformanceTest,*ConcurrencyTest" test
```

They use bounded requests, configurable p95/p99/error thresholds, synthetic
data, and safe provider settings. See [docs/performance-testing.md](performance-testing.md).

## API contract tests

Contract checks are disabled by default and can be run against the Gateway:

```powershell
$env:CONTRACT_TEST_ENABLED="true"
$env:CONTRACT_SCHEMA_STRICT="true"
mvn -Dtest="*ContractTest" test
```

Set `CONTRACT_COURSE_ID` for optional Course and Enrollment contracts. Contract
failures identify the affected field and do not print complete response bodies.

## Repeatability and reports

```powershell
mvn -Dtest="*Gateway*IT" verify -Pintegration
mvn -Dtest="*Gateway*IT" verify -Pintegration
mvn -Dtest="*Gateway*IT" verify -Pintegration
```

Reports are written to `target/surefire-reports/` and
`target/failsafe-reports/`. CI uploads both directories when present.

## Test register and backend coverage evidence

Generate a run-specific test register locally after running the desired test
profiles. Python 3.12 or later is required for the evidence generator (CI pins
Python 3.12):

```powershell
python tools/test-evidence.py `
  --junit-dir target/surefire-reports `
  --junit-dir target/failsafe-reports `
  --metadata docs/test-case-mappings.csv `
  --output-dir target/test-evidence
```

The output contains `test-register.csv`, `test-register.md`,
`coverage-summary.md`, and `evidence.json`. Documented case IDs are discovered
from `docs/*-test-cases.md`. Until a case has a mapping row, it appears as
`Not Mapped` with its documentation source and an unmapped SRS requirement.
The mapping CSV contains stable IDs and requirement links for mapped cases.
It currently has 663 case rows targeting 406 class/method pairs; Notification
Kafka event ingestion and local Mailpit email delivery have an exact gated
JUnit mapping. Static runs skip those live scenarios unless their explicit
non-production broker and local Mailpit configuration is enabled. Every executed method must map to an exact JUnit class/method and
requirement. CI uses `--fail-on-unmapped-tests` so newly executed unmapped
methods fail evidence generation. A mapped case absent from a run is `Not Run`;
a skipped result remains `Skipped`.
Do not fill actual results from memory or convert skipped/not-run cases to
passed.

Backend coverage must come from the backend JaCoCo reports. This repository
contains API-test code, so its own JaCoCo result would not measure backend
product code. The backend CI publishes per-service `jacoco-unit-*` and
`jacoco-integration-*` artifacts. Download the relevant artifacts from the
same backend workflow run, extract them into a directory, and pass that
directory as another input:

```powershell
python tools/test-evidence.py `
  --junit-dir target/surefire-reports `
  --junit-dir target/failsafe-reports `
  --jacoco-dir artifacts/backend-jacoco `
  --metadata docs/test-case-mappings.csv `
  --output-dir target/test-evidence
```

The summarizer aggregates module counters within each category while keeping
unit and integration results separate. Duplicate module reports in one
category are treated as invalid input. The optional manual GitHub Actions path
downloads unit and integration artifacts from a selected backend CI run; set
the backend run ID and commit SHA together so the evidence manifest identifies
its source. `coverage-summary.md` reports measured line,
branch, and instruction counters; it does not claim a threshold passed. The
Any backend coverage gate result remains the result of the backend checks
actually configured for the measured module and source scope. The summary does
not infer that a gate ran or passed. If no JaCoCo XML was supplied, it says
coverage is unavailable, not zero.

## Troubleshooting

1. Confirm Java 17 and Maven 3.9 or later.
2. Confirm the gateway and required services are reachable.
3. Confirm `TEST_ENV_ENABLED=true` for live execution.
4. Confirm `TEST_REQUIRED_SERVICES` includes required services.
5. Confirm approved non-production users and fixture IDs.
6. Read the readiness failure and Failsafe report for service, endpoint, role,
   scenario, expected result, and actual result.
7. Remove generated `target/` output and rerun before classifying a failure as
   repeatability or data contamination.

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
$env:TEST_REQUIRED_SERVICES="API_GATEWAY,KEYCLOAK,USER_SERVICE"
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
mvn -Dtest="*Gateway*IT,*JourneyIT" verify -Pintegration
```

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

## Repeatability and reports

```powershell
mvn -Dtest="*Gateway*IT" verify -Pintegration
mvn -Dtest="*Gateway*IT" verify -Pintegration
mvn -Dtest="*Gateway*IT" verify -Pintegration
```

Reports are written to `target/surefire-reports/` and
`target/failsafe-reports/`. CI uploads both directories when present.

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

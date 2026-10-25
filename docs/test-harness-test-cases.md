# Test harness test cases

| ID | Scenario | Expected result |
| --- | --- | --- |
| TC-TEST-038-01 | Live integration tests are disabled by default | Integration classes are disabled when the environment flag is missing or false. |
| TC-TEST-038-02 | Unit tests stay enabled with live integration off | The integration execution condition does not disable static tests. |
| TC-TEST-038-03 | Explicit integration opt-in | Setting `TEST_ENV_ENABLED=true` enables live integration classes. |
| TC-TEST-038-04 | Staging test target guard | Staging smoke is enabled only when `STAGING_ENVIRONMENT=staging`. |

These are deterministic tests of repository execution safeguards and use no backend service or credentials.

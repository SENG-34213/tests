# Negative-Path Test Cases

| ID | Area | Scenario | Expected result | Test location | Environment |
|---|---|---|---|---|---|
| SEC-001 | Authentication | Missing bearer token | Protected endpoint returns 401 | `security/SecurityRegressionIT` | Gateway and Keycloak |
| SEC-002 | Authentication | Empty bearer token | Protected endpoint returns 401 | `security/SecurityRegressionIT` | Gateway and Keycloak |
| SEC-003 | Authentication | Invalid or malformed token | Protected endpoint returns 401 | `security/SecurityRegressionIT` | Gateway and Keycloak |
| SEC-004 | Authorization | Student accesses admin probe | Request returns 403 | `security/SecurityRegressionIT` | Student fixture |
| SEC-005 | Authorization | Teacher accesses admin probe | Request returns 403 | `security/SecurityRegressionIT` | Teacher fixture |
| SEC-006 | Sensitive data | Authentication error body inspection | No password, token, auth header, or stack trace | `security/SecurityRegressionIT` | Gateway |
| RES-001 | Downstream failure | Controlled dependency failure | Test skips without injector; controlled error when enabled | `resilience/DownstreamFailureIT` | Fault injector |
| RES-002 | Provider failure | Controlled provider failure | Test skips without provider double | `resilience/ProviderFailureIT` | Provider double |
| RES-003 | Configuration | All fault modes default off | Configuration is safe by default | `security/SecurityResilienceTestConfigurationTest` | None |

Detailed service-level negative-path coverage remains in the existing service
test-case documents. This register records the new focused TEST-024 layer.

# Negative-Path Test Cases

| ID | Area | Scenario | Expected result | Test location | Environment |
|---|---|---|---|---|---|
| SEC-001 | Authentication | Missing bearer token | Protected endpoint returns 401 | `security/SecurityRegressionIT` | Gateway and Auth Service |
| SEC-002 | Authentication | Empty bearer token | Protected endpoint returns 401 | `security/SecurityRegressionIT` | Gateway and Auth Service |
| SEC-003 | Authentication | Invalid or malformed token | Protected endpoint returns 401 | `security/SecurityRegressionIT` | Gateway and Auth Service |
| SEC-004 | Authorization | Student accesses admin probe | Request returns 403 | `security/SecurityRegressionIT` | Student fixture |
| SEC-005 | Authorization | Teacher accesses admin probe | Request returns 403 | `security/SecurityRegressionIT` | Teacher fixture |
| SEC-006 | Sensitive data | Authentication error body inspection | No password, token, auth header, or stack trace | `security/SecurityRegressionIT` | Gateway |
| RES-002 | Provider failure | Controlled provider failure | Controlled provider returns 5xx without leaking its sentinel | `ai/AiProviderFailureIT` | AI Service configured with controlled failing provider double |
| RES-003 | Configuration | All fault modes default off | Configuration is safe by default | `security/SecurityResilienceTestConfigurationTest` | None |

Detailed service-level negative-path coverage remains in the existing service
test-case documents. This register records the new focused TEST-024 layer.

RES-001 remains an infrastructure-gated manual verification item: the repository
does not provide a controlled downstream fault injector, and the current guard
class does not issue a request or assert an outage/recovery response.

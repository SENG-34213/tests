# Resilience Regression Testing

Resilience tests verify controlled behavior when a dependency fails or times
out. They do not perform load testing or destructive failure injection.

## Configuration

```powershell
$env:TEST_ENV_ENABLED="true"
$env:TEST_RESILIENCE_ENABLED="true"
$env:FAULT_INJECTION_ENABLED="true"
$env:TEST_PROVIDER_FAILURE_MODE="true"
mvn -Dtest="*FailureIT,*TimeoutIT,*ResilienceIT,*IdempotencyIT" verify -Pintegration
```

The fault-injection flags are false by default. The tests skip unless a
controlled non-production fault injector or provider double is available.
Never point these settings at production services.

## Required behavior

Controlled failure scenarios must have bounded client timeouts, safe error
responses, no leaked internal hostnames or credentials, and no partial data
corruption. Retries must not hide assertion failures or create duplicate
records. Duplicate and idempotency behavior is tested only where the backend
contract supports it.

## Current limitations

This repository does not provision a broker, provider double, or service fault
proxy. `AiProviderFailureIT` exercises the API error contract only when the AI
Service is configured to use a controlled provider double that returns a
synthetic failure containing `CONTROLLED_PROVIDER_ERROR_SENTINEL`;
`DownstreamFailureIT` remains a configuration gate because no downstream fault
proxy is provisioned here. A skipped test is not a passing product result; it
records unavailable test infrastructure.

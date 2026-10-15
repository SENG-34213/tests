# Security Regression Testing

TEST-024 adds focused negative-path checks without replacing professional
penetration testing. The tests use the API Gateway and dedicated synthetic
accounts; they must never target production systems.

## Covered checks

- Missing, empty, invalid, and malformed bearer tokens.
- Student/teacher access to administrator-only endpoints.
- Safe authentication error responses.
- Sensitive-data checks for passwords, tokens, authorization values, and stack
  traces in negative responses.
- Existing service-specific validation, authorization, ownership, and file
  boundary tests remain the detailed coverage for each service.

## Run

```powershell
$env:TEST_ENV_ENABLED="true"
$env:TEST_SECURITY_ENABLED="true"
$env:TEST_REQUIRED_SERVICES="API_GATEWAY,KEYCLOAK"
mvn -Dtest="*Security*IT" verify -Pintegration
```

Security tests skip with a clear reason when `TEST_SECURITY_ENABLED` is false.
Credentials are supplied through environment variables and are never printed.

## Test accounts and isolation

Use separate synthetic student, teacher, and administrator accounts. Ownership
tests must use two distinct users and approved non-production fixtures. Tests
must clean up through supported APIs and must not modify databases directly.

## Out of scope

Production penetration testing, denial-of-service testing, brute force, real
personal data, real payment cards, provider keys, and third-party library
vulnerability testing are excluded.

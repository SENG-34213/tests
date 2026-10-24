# Security Regression Testing

TEST-024 adds focused negative-path checks without replacing professional
penetration testing. The tests use the API Gateway and dedicated synthetic
accounts; they must never target production systems.

## Covered checks

- Missing, empty, invalid, and malformed bearer tokens.
- Expired access token with a valid signature.
- Student/teacher access to administrator-only endpoints.
- Safe authentication error responses.
- Sensitive-data checks for passwords, tokens, authorization values, and stack
  traces in negative responses.
- Existing service-specific validation, authorization, ownership, and file
  boundary tests remain the detailed coverage for each service.

| Case ID | Scenario | Expected result | Test method |
|---|---|---|---|
| TC-TEST-024-01 | Protected route with missing, empty, invalid, or malformed bearer token | Every request returns 401 | `SecurityRegressionIT.protectedEndpointsRejectMissingInvalidAndMalformedTokens` |
| TC-TEST-024-02 | Student and Teacher request administrator endpoint | Both roles receive 403 | `SecurityRegressionIT.studentAndTeacherTokensCannotCrossAdministratorBoundary` |
| TC-TEST-024-03 | Protected error body checked for sensitive values | 401 body does not disclose passwords, tokens, bearer values, or stack traces | `SecurityRegressionIT.protectedErrorResponsesDoNotExposeSensitiveValues` |
| TC-TEST-024-04 | Correctly signed but expired access token | Protected request returns 401 | `SecurityRegressionIT.expiredSignedAccessTokenIsRejected` |

## Run

```powershell
$env:TEST_ENV_ENABLED="true"
$env:TEST_SECURITY_ENABLED="true"
$env:TEST_REQUIRED_SERVICES="API_GATEWAY,AUTH_SERVICE,USER_SERVICE"
$env:AUTH_JWT_PRIVATE_KEY_BASE64="<local backend signing key>"
mvn -Dtest="*Security*IT" verify -Pintegration
```

The expired-token test copies a valid local access token's claims, sets its
expiration in the past, and signs it with the same local Auth Service key. Set
`AUTH_JWT_PRIVATE_KEY_BASE64` from the backend's local `.env` for this test.
Never use a production signing key. Security tests skip with a clear reason
when `TEST_SECURITY_ENABLED` is false. Credentials and signing keys are not
printed by the tests.

## Test accounts and isolation

Use separate synthetic student, teacher, and administrator accounts. Ownership
tests must use two distinct users and approved non-production fixtures. Tests
must clean up through supported APIs and must not modify databases directly.

## Out of scope

Production penetration testing, denial-of-service testing, brute force, real
personal data, real payment cards, provider keys, and third-party library
vulnerability testing are excluded.

# User Service integration testing

TEST-021 verifies User Service authentication, identity, registration, administration, and security behavior through
the API Gateway. Tests use the existing Keycloak authentication utilities and never access the User Service database.

## Scope

Coverage includes public/protected role probes, login request validation, refresh/logout/forgot-password contracts,
current-user retrieval, administrator listings and analytics, registration/status/invitation validation, existence
checks, role authorization, and sensitive-field protection. Account-creating, status-changing, email, and
super-administrator scenarios require explicitly approved isolated fixtures and are not enabled by default.

## Configuration and security

```properties
TEST_ENV_ENABLED=true
TEST_REQUIRED_SERVICES=API_GATEWAY,KEYCLOAK,USER_SERVICE
USER_SERVICE_BASE_URL=http://localhost:8081
TEST_USER_WRITE_ENABLED=false
TEST_SUPER_ADMIN_SUPPORTED=false
USER_EMAIL_TEST_MODE=false
```

Use external synthetic accounts for Student, Teacher, and Administrator roles. Add a Super Administrator only when
the environment explicitly supports it. Never commit passwords, access/refresh tokens, Keycloak credentials, reset
links, invitation links, or real personal data. Password-reset and invitation tests are contract-only unless a safe
mail-capture service is configured.

## Endpoint notes

The backend exposes duplicate administrative routes under `/api/v1/users` and `/api/v1/admin/users`; the client
supports both listing/registering paths only where required, while core tests avoid duplicating the same assertions.
Student and teacher list endpoints return Spring pageable responses. The existence endpoints have no method-level
authorization annotation in the inspected backend, so their effective gateway/security behavior must be verified in
the live environment before final role assertions are expanded.

## Test data and cleanup

Use unique synthetic identities for registration tests and approved accounts for status tests. Do not delete shared
users. Account creation, status changes, invitations, and reset requests remain disabled unless the environment
provides safe cleanup and mail capture. Existing authentication sessions are logged out or cleared after each test.

## Running

```powershell
mvn test
mvn verify -Pintegration
mvn -Pintegration verify -Dtest="*User*IT,*AuthenticationIT"
```

The normal test phase runs `UserClientTest`. Integration classes use the repository `*IT` convention and require API
Gateway, Keycloak, and User Service readiness.

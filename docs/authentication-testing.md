# Authentication testing

The reusable client exercises the backend's application-issued JWT flow through the API Gateway. The Auth Service handles login, refresh, logout, password reset, and the JWKS endpoint; the test repository does not call an identity-provider admin API or copy backend authentication code.

## Supported flow

- `POST /api/v1/auth/login` with `{ "identifier": "...", "password": "..." }`.
- `POST /api/v1/auth/refresh` with the current refresh token.
- `POST /api/v1/auth/logout` with the current refresh token.
- Authenticated API calls through `AuthenticationClient.authenticatedApiClient(session)`.

Authorization coverage uses the public Auth Service login route, the authenticated
`/api/v1/users/me` profile endpoint, and the administrator-only
`/api/v1/admin/users?role=STUDENT` endpoint through the API Gateway.

Detailed traceability is available in [authentication-test-cases.md](authentication-test-cases.md).

The session object keeps access and refresh tokens in memory only. Its `toString()` and all API logging omit token and password values. Logout clears the session after a successful backend response.

The expired-token security case signs a synthetic token with the local Auth
Service private key after moving its expiration into the past. Set
`AUTH_JWT_PRIVATE_KEY_BASE64` from the local backend `.env` only when running
that case; never use a production signing key or commit the value.

## Local configuration

Use dedicated local test accounts from the backend guide. Configure either the generic pair or a role-specific pair in the shell running Maven:

```powershell
$env:TEST_USERNAME = "student@mentify.com"
$env:TEST_PASSWORD = "<local-test-password>"
$env:TEST_ENV_ENABLED = "true"
$env:TEST_REQUIRED_SERVICES = "API_GATEWAY,AUTH_SERVICE"
mvn verify -Pintegration
```

Role-specific variables are `STUDENT_TEST_USERNAME`, `STUDENT_TEST_PASSWORD`, optional distinct `SECONDARY_STUDENT_TEST_USERNAME` and `SECONDARY_STUDENT_TEST_PASSWORD`, `TEACHER_TEST_USERNAME`, `TEACHER_TEST_PASSWORD`, optional distinct `SECONDARY_TEACHER_TEST_USERNAME` and `SECONDARY_TEACHER_TEST_PASSWORD`, `ADMIN_TEST_USERNAME`, `ADMIN_TEST_PASSWORD`, `SUPER_ADMIN_TEST_USERNAME`, and `SUPER_ADMIN_TEST_PASSWORD`. Secondary student and teacher credentials enable cross-user ownership checks; use approved fixtures assigned to only one account. Do not commit values or use production accounts.

Authentication integration tests also check User Service readiness because the Auth Service retrieves the user's active profile while issuing a session. When the API Gateway, Auth Service, or User Service is not running, the integration profile is expected to fail during readiness checks. Unit tests and `mvn clean verify` do not require the backend environment.

## Backend contract references

The endpoint and response assumptions are based on the backend `development` branch's Auth Service `AuthController`, `AuthenticationService`, JWT configuration, and Gateway resource-server configuration.

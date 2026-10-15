# Authentication testing

TEST-004 adds a reusable client for the backend authentication bridge. The client calls the public gateway endpoints implemented by the user service; it does not call Keycloak's admin API and it does not copy backend authentication code.

## Supported flow

- `POST /api/v1/auth/login` with `{ "identifier": "...", "password": "..." }`.
- `POST /api/v1/auth/refresh` with the current refresh token.
- `POST /api/v1/auth/logout` with the current refresh token.
- Authenticated API calls through `AuthenticationClient.authenticatedApiClient(session)`.

The session object keeps access and refresh tokens in memory only. Its `toString()` and all API logging omit token and password values. Logout clears the session after a successful backend response.

## Local configuration

Use dedicated local test accounts from the backend guide. Configure either the generic pair or a role-specific pair in the shell running Maven:

```powershell
$env:TEST_USERNAME = "student@mentify.com"
$env:TEST_PASSWORD = "<local-test-password>"
$env:TEST_ENV_ENABLED = "true"
$env:TEST_REQUIRED_SERVICES = "API_GATEWAY,KEYCLOAK"
mvn verify -Pintegration
```

Role-specific variables are `STUDENT_TEST_USERNAME`, `STUDENT_TEST_PASSWORD`, `TEACHER_TEST_USERNAME`, `TEACHER_TEST_PASSWORD`, `ADMIN_TEST_USERNAME`, and `ADMIN_TEST_PASSWORD`. Do not commit values, use production accounts, or use a Keycloak administrator account as an application test user.

When the backend or Keycloak is not running, the integration profile is expected to fail during readiness checks. Unit tests and `mvn clean verify` do not require the backend environment.

## Backend contract references

The endpoint and response assumptions are based on the backend `development` branch documents `docs/AUTHENTICATION_SESSION_FLOW.md` and `docs/KEYCLOAK_SETUP.md`, plus `UserLoginController` and `LoginRequest`.

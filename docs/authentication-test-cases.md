# TEST-005 Authentication and authorization test cases

These integration test cases use the backend `development` branch contract and execute through the API Gateway.

| Test Case ID | Test Case Name | Related Requirement | Related Test File | Test Type | Priority |
|---|---|---|---|---|---|
| TC-AUTH-LOGIN-001 | Login with valid Student credentials | Valid authentication | `AuthenticationLoginIT` | Integration | P1 |
| TC-AUTH-LOGIN-002 | Reject invalid password | Invalid credentials return 400 | `AuthenticationLoginIT` | Integration | P1 |
| TC-AUTH-LOGIN-003 | Reject unknown user | Unknown credentials return 400 | `AuthenticationLoginIT` | Integration | P1 |
| TC-AUTH-LOGIN-004 | Reject invalid login request | Login validation returns 400 | `AuthenticationLoginIT` | Integration | P1 |
| TC-AUTH-REFRESH-001 | Refresh valid token | Valid refresh token is accepted | `AuthenticationRefreshIT` | Integration | P1 |
| TC-AUTH-REFRESH-002 | Reject invalid refresh token | Invalid refresh token returns 400 | `AuthenticationRefreshIT` | Integration | P1 |
| TC-AUTH-LOGOUT-001 | Clear session after logout | Logout terminates local session | `AuthenticationLogoutIT` | Integration | P1 |
| TC-AUTH-ACCESS-001 | Allow public login route without token | Login route accepts unauthenticated requests and validates the login payload | `AuthorizationIT` | Integration | P1 |
| TC-AUTH-ACCESS-002 | Reject protected endpoint without token | Protected API requires authentication | `AuthorizationIT` | Integration | P1 |
| TC-AUTH-ACCESS-003 | Reject expired signed access token | Expired JWT returns 401 | `SecurityRegressionIT` | Security integration | P1 |
| TC-AUTH-ROLE-001 | Allow Student resource access | Student role authorization | `AuthorizationIT` | Integration | P1 |
| TC-AUTH-ROLE-002 | Reject Student admin access | Student cannot use Admin resource | `AuthorizationIT` | Integration | P1 |
| TC-AUTH-ROLE-003 | Verify Teacher authorization | Teacher access is authenticated and restricted | `AuthorizationIT` | Integration | P1 |
| TC-AUTH-ROLE-004 | Allow Admin resource access | Admin role authorization | `AuthorizationIT` | Integration | P1 |
| TC-AUTH-USER-001 | Read authenticated user profile | Authenticated user can read `/users/me` | `AuthorizationIT` | Integration | P1 |
| TC-AUTH-CLIENT-001 | Create an authenticated API client after login | Successful login produces a usable authenticated client and logout clears the session | `AuthenticationClientIT` | Integration | P1 |
| TC-AUTH-RECOVERY-001 | Request reset for unknown account without account disclosure | Generic recovery response | `AuthenticationRecoveryIT` | Security integration | P1 |
| TC-AUTH-RECOVERY-002 | Reject invalid reset OTP and token | Recovery cannot proceed without valid OTP/token | `AuthenticationRecoveryIT` | Validation integration | P1 |
| TC-AUTH-JWKS-001 | Read public signing-key document | JWKS is publicly available | `AuthenticationRecoveryIT` | Contract integration | P1 |

## Backend endpoints verified

- `GET /api/v1/auth/.well-known/jwks.json`: public signing-key endpoint.
- `GET /api/v1/users/me`: requires an authenticated user.
- `GET /api/v1/admin/users?role=STUDENT`: requires `ADMIN` or `SUPER_ADMIN`.
- `POST /api/v1/auth/login`: public login endpoint; malformed credentials are rejected with HTTP 400.
- `POST /api/v1/auth/login`: login bridge.
- `POST /api/v1/auth/refresh`: refresh-token bridge.
- `POST /api/v1/auth/logout`: logout bridge.

These endpoints exercise the effective Gateway routes used by the application.

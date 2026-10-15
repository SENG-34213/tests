# TEST-005 Authentication and authorization test cases

These integration test cases use the backend `development` branch contract and execute through the API Gateway.

| Test Case ID | Test Case Name | Related Requirement | Related Test File | Test Type | Priority |
|---|---|---|---|---|---|
| TC-AUTH-LOGIN-001 | Login with valid Student credentials | Valid authentication | `AuthenticationLoginIT` | Integration | P1 |
| TC-AUTH-LOGIN-002 | Reject invalid password | Invalid credentials return 401 | `AuthenticationLoginIT` | Integration | P1 |
| TC-AUTH-LOGIN-003 | Reject unknown user | Unknown credentials return 401 | `AuthenticationLoginIT` | Integration | P1 |
| TC-AUTH-LOGIN-004 | Reject invalid login request | Login validation returns 400 | `AuthenticationLoginIT` | Integration | P1 |
| TC-AUTH-REFRESH-001 | Refresh valid token | Valid refresh token is accepted | `AuthenticationRefreshIT` | Integration | P1 |
| TC-AUTH-REFRESH-002 | Reject invalid refresh token | Invalid refresh token returns 401 | `AuthenticationRefreshIT` | Integration | P1 |
| TC-AUTH-LOGOUT-001 | Clear session after logout | Logout terminates local session | `AuthenticationLogoutIT` | Integration | P1 |
| TC-AUTH-ACCESS-001 | Allow public endpoint without token | Public API remains accessible | `AuthorizationIT` | Integration | P1 |
| TC-AUTH-ACCESS-002 | Reject protected endpoint without token | Protected API requires authentication | `AuthorizationIT` | Integration | P1 |
| TC-AUTH-ROLE-001 | Allow Student resource access | Student role authorization | `AuthorizationIT` | Integration | P1 |
| TC-AUTH-ROLE-002 | Reject Student admin access | Student cannot use Admin resource | `AuthorizationIT` | Integration | P1 |
| TC-AUTH-ROLE-003 | Verify Teacher authorization | Teacher access is authenticated and restricted | `AuthorizationIT` | Integration | P1 |
| TC-AUTH-ROLE-004 | Allow Admin resource access | Admin role authorization | `AuthorizationIT` | Integration | P1 |
| TC-AUTH-USER-001 | Read authenticated user profile | Authenticated user can read `/users/me` | `AuthorizationIT` | Integration | P1 |

## Backend endpoints verified

- `GET /api/v1/auth/public-test`: public endpoint.
- `GET /api/v1/auth/protected-test`: authenticated endpoint.
- `GET /api/v1/auth/admin-test`: requires `ADMIN` or `SUPER_ADMIN`.
- `GET /api/v1/auth/student-test`: requires `STUDENT`.
- `GET /api/v1/users/me`: requires an authenticated user.
- `POST /api/v1/auth/login`: login bridge.
- `POST /api/v1/auth/refresh`: refresh-token bridge.
- `POST /api/v1/auth/logout`: logout bridge.

The first four endpoints are read-only backend authentication test endpoints and are used to keep authorization tests deterministic.

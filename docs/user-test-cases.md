# TEST-021 user test cases

| ID | Scenario | Expected result |
| --- | --- | --- |
| TC-TEST-021-01 | User-service readiness | API Gateway, Keycloak, and User Service are checked. |
| TC-TEST-021-02 | Public probe | Public authentication probe returns success without a token. |
| TC-TEST-021-03 | Protected probe | Missing authentication is rejected. |
| TC-TEST-021-04 | Role probes | Student/admin role access follows the backend contract. |
| TC-TEST-021-05 | Login validation | Missing and malformed credentials are rejected. |
| TC-TEST-021-06 | Refresh validation | Missing/invalid refresh tokens are rejected. |
| TC-TEST-021-07 | Logout validation | Missing/invalid logout requests are rejected safely. |
| TC-TEST-021-08 | Forgot password | Invalid email is rejected; generic response behavior is documented. |
| TC-TEST-021-09 | Current user | Authenticated user can retrieve own profile. |
| TC-TEST-021-10 | Sensitive fields | Passwords, tokens, and secrets are absent from profile responses. |
| TC-TEST-021-11 | Admin listing | Administrator can list users by role. |
| TC-TEST-021-12 | Student listing | Administrator can list students with pagination. |
| TC-TEST-021-13 | Teacher listing | Administrator can list teachers with pagination. |
| TC-TEST-021-14 | Analytics | Administrator can retrieve registration overview metrics. |
| TC-TEST-021-15 | Admin validation | Invalid roles, IDs, status, and registration fields are handled safely. |
| TC-TEST-021-16 | Student authorization | Student cannot perform admin operations. |
| TC-TEST-021-17 | Teacher authorization | Teacher cannot perform admin operations. |
| TC-TEST-021-18 | Registration | Unique user registration is enabled only with approved isolated fixtures. |
| TC-TEST-021-19 | Super-admin registration | Scenario is enabled only when Super Admin support is confirmed. |
| TC-TEST-021-20 | Invitation/reset email | Safe mail capture is required; otherwise contract tests are used. |
| TC-TEST-021-21 | Status lifecycle | Deactivation/reactivation is enabled only with safe account fixtures. |
| TC-TEST-021-22 | Existence checks | Student/teacher existence behavior is validated without private data leakage. |
| TC-TEST-021-23 | Security | No passwords, tokens, reset links, invitation links, or personal data are committed. |
| TC-TEST-021-24 | Documentation | Keycloak, roles, fixtures, cleanup, and skip behavior are documented. |

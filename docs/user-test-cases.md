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
| TC-TEST-021-11 | Admin listing | Administrator can list users by role and every returned record matches the requested role. |
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
| TC-TEST-021-22 | Existence checks | Known malformed and unknown student/teacher IDs return `false` without private data leakage. |
| TC-TEST-021-23 | Security | No passwords, tokens, reset links, invitation links, or personal data are committed. |
| TC-TEST-021-24 | Documentation | Keycloak, roles, fixtures, cleanup, and skip behavior are documented. |
| TC-TEST-021-25 | User-management role matrix | Student/teacher cannot list users, register accounts, or delete users; non-super-admin cannot create an admin. |
| TC-TEST-021-26 | Self-service profile read and no-op update | Authenticated student reads own profile and submits an empty update without changing data. |
| TC-TEST-021-27 | Teacher profile and guidance-note authorization | Teacher route allows teacher role; student role is denied; invalid note input is rejected. |
| TC-TEST-021-28 | Teacher/student lookup routes | Unknown teacher returns not-found; unknown student list is empty; empty batch lookup is rejected. |
| TC-TEST-021-29 | Self-service and protected lookup authentication | Missing credentials are rejected on protected profile, password, lookup and guidance-note routes. |
| TC-TEST-021-30 | Password-change and public student-registration validation | Empty password update and registration payloads return validation errors. |
| TC-TEST-021-31 | Student lookup ownership | With a distinct optional secondary student, a student can retrieve their own summary but cannot retrieve another student's summary through either lookup endpoint. |
| TC-TEST-021-32 | Admin role-filter integrity | User listing and paginated student/teacher directories contain only records matching the requested role. |
| TC-TEST-021-33 | Existence response values | Unknown student and teacher IDs, including malformed student identities, return an explicit `false` response. |
| TC-TEST-021-34 | Unknown administration targets | A valid status update, invitation resend, and delete for an unknown user return `404`; malformed status input remains `400`. |

# Course Service Test Cases

| ID | Test case | Endpoint | Expected result | Type |
| --- | --- | --- | --- | --- |
| TC-COURSE-001 | Course Service readiness | Actuator readiness endpoint | Service is ready before tests execute | Integration |
| TC-COURSE-002 | Teacher retrieves assigned courses | `GET /api/v1/course/my-courses` | `200` with successful response and data | Integration |
| TC-COURSE-003 | Student performs course lookup | `GET /api/v1/course/{courseId}/lookup` | Authorized request with backend-defined result | Authorization |
| TC-COURSE-004 | Admin retrieves all courses | `GET /api/v1/course` | `200` with successful response and data | Integration |
| TC-COURSE-005 | Unauthenticated course list | `GET /api/v1/course` | `401 Unauthorized` | Security |
| TC-COURSE-006 | Unauthenticated teacher courses | `GET /api/v1/course/my-courses` | `401 Unauthorized` | Security |
| TC-COURSE-007 | Student reads all courses | `GET /api/v1/course` | `403 Forbidden` | Authorization |
| TC-COURSE-008 | Teacher reads all courses | `GET /api/v1/course` | `403 Forbidden` | Authorization |
| TC-COURSE-009 | Student creates a course | `POST /api/v1/course` | `403 Forbidden` | Authorization |
| TC-COURSE-010 | Teacher creates a course | `POST /api/v1/course` | `403 Forbidden` | Authorization |
| TC-COURSE-011 | Student deletes a course | `DELETE /api/v1/course/{courseId}` | `403 Forbidden` | Authorization |
| TC-COURSE-012 | Student performs bulk lookup | `POST /api/v1/course/bulk` | `403 Forbidden` | Authorization |
| TC-COURSE-013 | Course client configuration | Course client construction | Client requires a valid API client | Unit |
| TC-COURSE-014 | Course environment configuration | `COURSE_SERVICE_BASE_URL` | URL is parsed correctly | Unit |

## Preconditions

- Java 17 and Maven are installed.
- The API Gateway, Keycloak, and Course Service are running for live integration tests.
- Dedicated Student, Teacher, and Admin credentials are configured.
- `TEST_ENV_ENABLED=true` is set for live integration tests.

## Isolation and safety

- Read-only tests do not create persistent courses.
- Authorization-only create requests must be rejected before persistence.
- Unknown course identifiers may be used for lookup and deletion checks.
- Tests do not access the Course Service database.
- Tests do not use production course identifiers.

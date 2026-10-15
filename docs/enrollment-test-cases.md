# Enrollment Service Test Cases

| ID | Scenario | Endpoint | Expected result | Type |
| --- | --- | --- | --- | --- |
| TC-TEST-010-01 | Student self-enrolls | `POST /api/v1/enrollments/me` | `201` or documented business response; membership is true | CRUD integration |
| TC-TEST-010-02 | Student reads active course IDs | `GET /api/v1/enrollments/students/{studentId}/courses` | `200` with course data | Integration |
| TC-TEST-010-03 | Membership check | `GET /api/v1/enrollments/students/{studentId}/courses/{courseId}/exists` | Boolean result | Integration |
| TC-TEST-010-04 | Admin creates enrollment | `POST /api/v1/enrollments` | `201` or documented business response | CRUD integration |
| TC-TEST-010-05 | Admin lists active enrollments | `GET /api/v1/enrollments` | `200` with data | Integration |
| TC-TEST-010-06 | Admin updates enrollment | `PUT /api/v1/enrollments/{enrollmentId}` | `200` with updated course IDs | CRUD integration |
| TC-TEST-010-07 | Teacher lists course students | `GET /api/v1/enrollments/courses/{courseId}/students` | `200` with student IDs | Authorization |
| TC-TEST-010-08 | Admin lists unenrolled students | `GET /api/v1/enrollments/students/unenrolled` | `200` with data | Integration |
| TC-TEST-010-09 | Restricted management access | Enrollment management endpoints | `403` for student/teacher | Security |
| TC-TEST-010-10 | Unauthenticated access | All protected endpoints | `401 Unauthorized` | Security |
| TC-TEST-010-11 | Invalid request bodies and UUIDs | Create, self-enroll, update | `400 Bad Request` | Validation |
| TC-TEST-010-12 | Unknown enrollment | `PUT /api/v1/enrollments/{id}` | `404` or documented `400`, never unexpected `500` | Error integration |
| TC-TEST-010-13 | Unknown membership | Membership endpoint | `200 false` where supported | Error integration |
| TC-TEST-010-14 | Enrollment client configuration | Client construction and paths | Valid client; blank IDs rejected | Unit |
| TC-TEST-010-15 | Enrollment environment configuration | `ENROLLMENT_SERVICE_BASE_URL` | URL is parsed correctly | Unit |

Write tests are opt-in because the backend exposes no enrollment deletion endpoint. They require explicitly approved non-production fixtures and are not run against production data.

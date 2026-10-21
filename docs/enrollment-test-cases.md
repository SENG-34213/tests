# Enrollment Service Test Cases

| ID | Scenario | Endpoint | Expected result | Type |
| --- | --- | --- | --- | --- |
| TC-TEST-010-01 | Student self-enrolls | `POST /api/v1/enrollments/me` | `201`, status is `PENDING`, membership is true; test-created enrollment is deleted afterward | CRUD integration |
| TC-TEST-010-02 | Student reads active course IDs | `GET /api/v1/enrollments/students/{studentId}/courses` | `200` with course data | Integration |
| TC-TEST-010-03 | Membership check | `GET /api/v1/enrollments/students/{studentId}/courses/{courseId}/exists` | Boolean result | Integration |
| TC-TEST-010-04 | Admin creates enrollment | `POST /api/v1/enrollments` | `201` with requested student, course, and status; test-created record is deleted afterward | CRUD integration |
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
| TC-TEST-010-16 | Active enrollments by course | `GET /api/v1/enrollments/courses/{courseId}` | Teacher/admin receives active enrollment records for that course | Integration |
| TC-TEST-010-17 | Enrollment status and course transitions | `PUT /api/v1/enrollments/{id}` | Admin changes status `PENDING` to `APPROVED` to `REJECTED` and changes the course; membership follows the updated courses | Fixture-gated integration |
| TC-TEST-010-18 | Delete enrollment | `DELETE /api/v1/enrollments/{id}` | Admin deactivates the created record; membership becomes false and it disappears from active course results | Fixture-gated integration |
| TC-TEST-010-19 | Enrollment cleanup | Self-enrollment and admin lifecycle workflows | Tests delete only records they created, including on assertion failure | Integration safety |

| TC-TEST-010-20 | Role matrix | Student, teacher, admin, and super admin route access | Roles are allowed or denied according to controller rules | Security integration |
| TC-TEST-010-21 | Teacher course ownership | Teacher create/update/delete for owned and other-teacher courses | Owned course management succeeds; create/update/delete for another teacher's course is denied | Fixture-gated integration |
| TC-TEST-010-22 | Students cannot query teacher/admin enrollment directories | Course student/enrollment lists and unenrolled students | Student receives `403`; teacher also receives `403` for admin-only unenrolled-student listing | Security integration |
| TC-TEST-010-23 | Teacher cannot update or delete another teacher's enrollment | Update/delete by enrollment ID | Both requests return `403`; foreign record remains unchanged | Fixture-gated security integration |
| TC-TEST-010-24 | Teacher manages enrollment in an owned course | Create, approve, delete and verify membership | Created enrollment is deleted and membership is cleared | Fixture-gated integration |

Write tests are opt-in and require explicitly approved non-production fixtures. The backend has a soft-delete endpoint; tests clean up only enrollment records they create. Self-enrollment success requires a course verified as free so it does not depend on a payment provider. Teacher ownership tests require one teacher-owned course and one course owned by a different teacher.

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
| TC-COURSE-010 | Teacher reaches course creation | `POST /api/v1/course` | Not denied by role; request uses the authenticated teacher ID and an unknown grade fixture | Authorization |
| TC-COURSE-011 | Student deletes a course | `DELETE /api/v1/course/{courseId}` | `403 Forbidden` | Authorization |
| TC-COURSE-012 | Student performs bulk lookup | `POST /api/v1/course/bulk` | `403 Forbidden` | Authorization |
| TC-COURSE-013 | Course client configuration | Course client construction | Client requires a valid API client | Unit |
| TC-COURSE-014 | Course environment configuration | `COURSE_SERVICE_BASE_URL` | URL is parsed correctly | Unit |
| TC-COURSE-015 | Admin creates a course | `POST /api/v1/course` | `201 Created` with a course ID | CRUD integration |
| TC-COURSE-016 | Admin reads and updates a course | `GET/PUT /api/v1/course/{courseId}` | `200 OK` and updated course data | CRUD integration |
| TC-COURSE-017 | Student and teacher lookup | `GET /api/v1/course/{courseId}/lookup` | `200 OK` for both roles | CRUD integration |
| TC-COURSE-018 | Admin bulk lookup | `POST /api/v1/course/bulk` | `200 OK` containing the created course | CRUD integration |
| TC-COURSE-019 | Admin deletes a course | `DELETE /api/v1/course/{courseId}` | `200 OK`, followed by `404` on read | CRUD integration |
| TC-COURSE-020 | Admin submits invalid course data | `POST /api/v1/course` | `400 Bad Request` | Validation integration |
| TC-COURSE-021 | Admin reads an unknown course | `GET /api/v1/course/{courseId}` | `404 Not Found` | Error integration |
| TC-COURSE-022 | Teacher reads dashboard | `GET /api/v1/teacher/dashboard` | `200` with successful response and data | Integration |
| TC-COURSE-023 | Teacher searches portal records | `GET /api/v1/teacher/search?q=...` | `200` with successful response and data | Integration |
| TC-COURSE-024 | Unauthenticated teacher dashboard/search | `GET /api/v1/teacher/dashboard`, `/search` | `401 Unauthorized` | Security |
| TC-COURSE-025 | Student cannot access teacher portal reads | Dashboard, search, student directory/summary, gradebook, analytics | `403 Forbidden` | Authorization |
| TC-COURSE-026 | Teacher portal client validates required identifiers | Course/student IDs and search query | Blank required values are rejected locally | Unit |
| TC-COURSE-027 | Student cannot mutate course modules, lessons, or learning materials | Module, lesson, and material write routes | `403 Forbidden` | Authorization |
| TC-COURSE-028 | Only administrators can manage grades and monthly fees | Grade and monthly-fee routes | `403 Forbidden` for teacher role | Authorization |
| TC-COURSE-029 | Course content and grade administration require authentication | Representative protected GET/write routes | `401 Unauthorized` | Security |
| TC-COURSE-030 | Student can read own announcements and progress; teacher cannot | `GET /api/v1/announcements/me`, `/api/v1/progress/me` | Student succeeds; teacher is denied | Authorization |
| TC-COURSE-031 | Teacher reads student directory, summary, gradebook, and analytics | Teacher portal routes with owned-course/enrolled-student fixture | `200` successful response with data | Fixture-gated integration |
| TC-COURSE-032 | Teacher creates, reads, updates, and deletes a module and lesson | Module/lesson routes with owned-course fixture | Full lifecycle succeeds and created resources are cleaned up | Fixture-gated integration |
| TC-COURSE-033 | Teacher creates, reads, updates, and deletes a learning-material reference | Learning-material routes with owned-course fixture | Full lifecycle succeeds and created records are cleaned up | Fixture-gated integration |
| TC-COURSE-034 | Admin creates, reads, updates, and deletes grade and monthly-fee records | Grade and monthly-fee routes | Full lifecycle succeeds; records created by the test are deleted | Explicitly enabled integration |
| TC-COURSE-035 | Student reads own announcements and lesson progress | Announcement/progress self-service routes | `200` with successful data for student; teacher is denied | Integration/security |
| TC-COURSE-036 | Student reads course progress | `GET /api/v1/progress/courses/{courseId}` | `200` with course progress for enrolled student fixture | Fixture-gated integration |
| TC-COURSE-037 | Teacher creates a course announcement | `POST /api/v1/announcements/courses/{courseId}` | `201` with the created announcement; synthetic record persists because there is no delete route | Explicitly enabled integration |
| TC-COURSE-038 | Student updates lesson completion | `PUT /api/v1/progress/lessons/{lessonId}` | `200` with the correct lesson/course; previous completion state is restored | Explicitly enabled integration |
| TC-COURSE-039 | Teacher uploads module and lesson materials | Course content upload routes plus Material Service read/delete routes | Both uploads are linked to the expected course/lesson and synthetic references/materials are cleaned up | Explicitly enabled cross-service integration |
| TC-COURSE-040 | Teacher portal enforces course ownership | Teacher analytics, gradebook, student directory/summary | A teacher cannot read another teacher's course data | Fixture-gated integration |
| TC-COURSE-041 | Teacher analytics validates period and date ranges | `GET /api/v1/teacher/analytics` | Valid bounded range succeeds; unsupported period and inverted range are rejected | Fixture-gated integration |
| TC-COURSE-042 | Teacher cannot mutate another teacher's course content | Module, lesson, and learning-material create/update/delete routes | All nine operations return `403 Forbidden`; foreign fixtures remain unchanged | Explicitly enabled ownership regression |
| TC-COURSE-043 | Announcement and progress role/validation boundaries | Announcement and lesson-progress routes | Wrong roles receive `403`; malformed teacher announcement and student progress payloads receive `400` | Security/validation integration |
| TC-COURSE-044 | Announcement pagination client bounds | Student announcement client page and size | Negative page and size above 100 are rejected locally before HTTP | Unit/integration |

## Preconditions

- Java 17 and Maven are installed.
- The API Gateway, Keycloak, and Course Service are running for live integration tests.
- Dedicated Student, Teacher, and Admin credentials are configured.
- `TEST_TEACHER_PORTAL_COURSE_ID` is owned by the test teacher and
  `TEST_TEACHER_PORTAL_STUDENT_ID` is enrolled in that course for teacher data
  success-path tests.
- `TEST_TEACHER_PORTAL_UNOWNED_COURSE_ID` identifies a different teacher's
  course for teacher analytics/gradebook ownership-denial tests.
- Set `TEST_COURSE_CONTENT_OWNERSHIP_WRITE_ENABLED=true` only in a disposable
  non-production environment, and configure the unowned course/module/lesson/
  learning-material reference IDs below. These IDs must all belong to the same
  other teacher's course; the test expects every write to be denied and creates
  no records.
- Set `TEST_COURSE_GRADE_WRITE_ENABLED=true` only against an isolated
  non-production backend to run disposable grade/monthly-fee write tests.
- `TEST_STUDENT_PROGRESS_COURSE_ID` must identify a course enrolled by the
  configured Student test account for course progress success coverage.
- `TEST_STUDENT_PROGRESS_LESSON_ID` must identify an active lesson in that
  enrolled course. Set `TEST_STUDENT_PROGRESS_WRITE_ENABLED=true` only for an
  approved non-production fixture; the test restores prior completion state,
  or leaves the newly created record incomplete.
- Set `TEST_COURSE_ANNOUNCEMENT_WRITE_ENABLED=true` only when the configured
  teacher-owned course is suitable for persistent synthetic announcements.
  The backend exposes no announcement-delete route.
- Set `TEST_COURSE_CONTENT_UPLOAD_WRITE_ENABLED=true` only against isolated
  Course and Material services using a teacher-owned test course. The test
  creates module/lesson fixtures, verifies both module-level and lesson-level
  upload associations, then removes course references and soft-deletes the
  synthetic Material Service files.
- `TEST_ENV_ENABLED=true` is set for live integration tests.

## Isolation and safety

- Read-only tests do not create persistent courses.
- Authorization-only create requests must be rejected before persistence.
- Unknown course identifiers may be used for lookup and deletion checks.
- Tests do not access the Course Service database.
- Tests do not use production course identifiers.
- Teacher portal authorization tests use an unknown UUID only after verifying that the request is rejected by role; success-path directory, gradebook, analytics, and summary fixtures are still required.

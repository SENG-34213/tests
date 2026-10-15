# Attendance Service Testing

TEST-015 covers the Attendance Service contract at `/api/v1/attendance`.
The service is expected at `ATTENDANCE_SERVICE_BASE_URL` (default `http://localhost:8087`).

## Covered operations

- Teacher/admin session creation, course listing, and session retrieval.
- Marking one or more records with `PRESENT`, `ABSENT`, `LATE`, or `NOT_MARKED`.
- Completing and cancelling sessions, including illegal state transitions.
- Student self summaries and teacher/admin student summaries.
- Admin session creation and retrieval when write fixtures are enabled.
- Authentication, role authorization, validation, unknown identifiers, and isolation.

## Configuration

Set `TEST_ENV_ENABLED=true` and include `ATTENDANCE_SERVICE`,
`COURSE_SERVICE`, and `ENROLLMENT_SERVICE` in `TEST_REQUIRED_SERVICES`.
Provide non-production credentials for the existing student, teacher, and admin variables.
Write tests additionally require `TEST_ATTENDANCE_COURSE_ID`,
`TEST_ATTENDANCE_STUDENT_ID`, and `TEST_ATTENDANCE_WRITE_ENABLED=true`.

Run unit tests with `mvn test`. Run live integration tests with
`mvn -Pintegration verify` only against an approved non-production environment.

## Fixtures and cleanup

Read-only tests use approved existing course and student IDs. Write tests create
unique session titles. The Attendance API has no delete operation, so cancelled
or completed test sessions may remain as non-production audit data; use a dedicated
test course or an approved cleanup process. No direct database access is used.

## Contract state

Attendance sessions start as `OPEN` and may transition to `COMPLETED` or
`CANCELLED`. Records use the four enum values documented above. The tests assert
the public HTTP contract and avoid relying on internal implementation details.

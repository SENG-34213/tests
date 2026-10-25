# TEST-015 Attendance Service Test Cases

| ID | Scenario | Expected result |
| --- | --- | --- |
| TC-TEST-015-01 | Create a session as teacher | `201`, an `OPEN` session is returned |
| TC-TEST-015-02 | Create a session as admin | `201` or the documented success response |
| TC-TEST-015-03 | List course sessions | `200`, only sessions for the course are returned |
| TC-TEST-015-04 | Retrieve a session | `200`, session fields and status are returned |
| TC-TEST-015-05 | Mark one enrolled student | `200`, the attendance record is persisted |
| TC-TEST-015-06 | Mark multiple enrolled students | `200`, all supplied records are returned |
| TC-TEST-015-07 | Complete an open session | `200`, status becomes `COMPLETED` |
| TC-TEST-015-08 | Cancel an open session | `200`, status becomes `CANCELLED` |
| TC-TEST-015-09 | Mark a completed session | `409 Conflict`; state is unchanged |
| TC-TEST-015-10 | Complete or mark a cancelled session | `409 Conflict`; state is unchanged |
| TC-TEST-015-11 | Complete while records remain `NOT_MARKED` | `409 Conflict`; session remains `OPEN` |
| TC-TEST-015-12 | Student outside the session is marked | `404 Not Found`; no record is added |
| TC-TEST-015-13 | Duplicate student or `NOT_MARKED` submitted in a batch | `400 Bad Request`; batch is rejected |
| TC-TEST-015-14 | Student reads own summary | `200`, course scope and aggregate arithmetic are correct |
| TC-TEST-015-15 | Teacher reads student summary | `200`, requested student/course and aggregate arithmetic are correct |
| TC-TEST-015-16 | Admin reads student summary | `200`, when an admin fixture is configured |
| TC-TEST-015-17 | Unauthenticated session request | `401` |
| TC-TEST-015-18 | Student uses management endpoint | `403` |
| TC-TEST-015-19 | Student reads another student summary | `403` |
| TC-TEST-015-20 | Missing session fields | `400` |
| TC-TEST-015-21 | Invalid UUID input | `400` |
| TC-TEST-015-22 | Empty attendance records | `400` |
| TC-TEST-015-23 | Missing student or status | `400` |
| TC-TEST-015-24 | Unknown session ID | `404 Not Found` |
| TC-TEST-015-25 | Unknown course ID | `404 Not Found` |
| TC-TEST-015-26 | Student with no records in a known course receives a zero-valued summary | `200`; session and attendance counts and percentage are zero |
| TC-TEST-015-27 | Overlong title or remarks | `400` |
| TC-TEST-015-28 | Invalid attendance status | `400` |
| TC-TEST-015-29 | Session course isolation | A course cannot read another course's sessions |
| TC-TEST-015-32 | Delete a newly created open attendance session | `200`; subsequent read returns `404` |
| TC-TEST-015-33 | Unknown course or student summary identifiers | `404 Not Found` |

## Manual quality checks (not JUnit test cases)

- **TC-TEST-015-30 - Fixture cleanup review:** Review that attendance writes use approved fixtures and document any service behavior that prevents cleanup.
- **TC-TEST-015-31 - Regression verification:** Run the unit suite and Checkstyle in CI before merging.

These are review and CI checks, not runtime scenarios, so they are not included
in the generated JUnit case register.

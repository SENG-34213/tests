# Analytics integration test cases

| ID | Scenario | Expected result |
| --- | --- | --- |
| TC-ANALYTICS-006 | Administrator reads course-scoped analytics summaries | Six configured course summary routes return HTTP 200 through the API Gateway. |

This read-only integration test needs an approved non-production course ID, the API Gateway, administrator credentials, and Assignment, Attendance, Course, Enrollment, Exam, Payment, and User services.

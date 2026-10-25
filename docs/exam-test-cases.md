# TEST-017 exam test cases

| ID | Scenario | Expected result |
| --- | --- | --- |
| TC-TEST-017-01 | Service readiness | API Gateway, Auth Service, Course, Enrollment, and Exam services are checked. |
| TC-TEST-017-02 | Create valid exam | Authorized teacher creates an exam and receives an ID. |
| TC-TEST-017-03 | Retrieve exam | Authorized management user retrieves the created exam. |
| TC-TEST-017-04 | List course exams | Exams are returned for the requested course. |
| TC-TEST-017-05 | Update exam | Editable title, description, date, time, marks, and type update successfully. |
| TC-TEST-017-06 | Course protection | Update cannot change the owning course through the request body. |
| TC-TEST-017-07 | Schedule exam | Eligible exam moves to the scheduled lifecycle state. |
| TC-TEST-017-08 | Start marking | Scheduled exam moves to the marking state. |
| TC-TEST-017-09 | Enter marks | Valid enrolled-student marks are accepted. |
| TC-TEST-017-10 | Complete exam | Marking exam moves to completed state. |
| TC-TEST-017-11 | Result sheet | Authorized user can retrieve the result sheet. |
| TC-TEST-017-12 | Summary | Authorized user can retrieve aggregate summary information. |
| TC-TEST-017-13 | Cancel exam | Eligible exam can be cancelled. |
| TC-TEST-017-14 | Student results | Student can retrieve own results through `/api/v1/exams/me`. |
| TC-TEST-017-15 | Unauthenticated access | Protected endpoints return 401. |
| TC-TEST-017-16 | Student management access | Student management requests return 403. |
| TC-TEST-017-17 | Exam validation | Missing and malformed exam fields are rejected. |
| TC-TEST-017-18 | Marks validation | Empty, malformed, negative, or over-precision marks are rejected. |
| TC-TEST-017-19 | Unknown IDs | Unknown exam/course IDs do not produce 5xx responses. |
| TC-TEST-017-22 | Student upcoming exams | Student can retrieve upcoming exams; unauthenticated access is rejected. |
| TC-TEST-017-23 | Exam attachment listing/download | Unauthenticated attachment reads are rejected. |
| TC-TEST-017-24 | Exam attachment deletion | Student cannot delete attachments; unauthenticated access is rejected. |
| TC-TEST-017-25 | Exam attachment lifecycle | With opt-in fixtures, a teacher uploads a synthetic file, verifies visibility and metadata through list/download, optionally checks exact stored bytes, then deletes it. |
| TC-TEST-017-26 | Student exam detail | A student enrolled in the configured fixture course can retrieve a scheduled exam; anonymous and non-student callers are denied, and an unknown exam returns `404`. |
| TC-TEST-017-27 | Student attachment visibility | On a scheduled exam, the enrolled student sees/downloads a `studentVisible=true` attachment, while a hidden attachment is omitted from the list and returns `403` on direct download. |
| TC-TEST-017-28 | Exam attachment file type | A `.txt` upload is rejected with `400`; a synthetic `.pdf` upload succeeds and is cleaned up. |
| TC-TEST-017-30 | Exam attachment byte integrity | When signed-download verification is enabled, teacher and enrolled-student downloads of visible files exactly match the synthetic upload using an explicitly approved storage host. |

## Manual safeguards

- **TC-TEST-017-20 — Isolation:** Before live execution, confirm fixtures are approved and synthetic exam titles are unique; verify cleanup is enabled for created exams and attachments.
- **TC-TEST-017-21 — Documentation:** Review this document, environment configuration, lifecycle, cleanup, and documented commands when the exam test suite changes.

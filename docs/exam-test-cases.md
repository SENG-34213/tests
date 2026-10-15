# TEST-017 exam test cases

| ID | Scenario | Expected result |
| --- | --- | --- |
| TC-TEST-017-01 | Service readiness | API Gateway, Keycloak, Course, Enrollment, and Exam services are checked. |
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
| TC-TEST-017-20 | Isolation | Unique titles and approved fixtures prevent uncontrolled shared-environment data. |
| TC-TEST-017-21 | Documentation | Configuration, lifecycle, cleanup, and commands are documented. |

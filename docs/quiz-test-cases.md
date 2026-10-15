# TEST-014 Quiz and Assessment Test Cases

| ID | Scenario | Expected result |
|---|---|---|
| TC-TEST-014-01 | Teacher creates a quiz | 201, quiz ID and submitted values are returned |
| TC-TEST-014-02 | Teacher retrieves a quiz | 200 and matching quiz details are returned |
| TC-TEST-014-03 | Teacher lists quizzes | The created quiz appears in the teacher list |
| TC-TEST-014-04 | Teacher updates a quiz | 200 and updated values are persisted |
| TC-TEST-014-05 | Teacher creates a question | 201, question ID, and four options are returned |
| TC-TEST-014-06 | Teacher updates a question | 200 and updated question text is returned |
| TC-TEST-014-07 | Teacher deletes a question | The removable question is no longer active |
| TC-TEST-014-08 | Teacher publishes a quiz | Status changes to `PUBLISHED` |
| TC-TEST-014-09 | Student lists course quizzes | Published quiz is returned for an enrolled student |
| TC-TEST-014-10 | Student retrieves a quiz | Student-safe quiz and question data are returned |
| TC-TEST-014-11 | Student starts an attempt | Attempt ID, quiz ID, and state are returned |
| TC-TEST-014-12 | Student saves an answer | Answer is associated with the question and attempt |
| TC-TEST-014-13 | Student submits an attempt | Attempt transitions to a submitted state |
| TC-TEST-014-14 | Maximum attempts | Additional attempt is rejected according to backend rules |
| TC-TEST-014-15 | Student creates a quiz | HTTP 403 |
| TC-TEST-014-16 | Student manages a question | HTTP 403 |
| TC-TEST-014-17 | Teacher starts an attempt | HTTP 403 |
| TC-TEST-014-18 | Unauthenticated request | HTTP 401 on protected endpoints |
| TC-TEST-014-19 | Blank quiz title | HTTP 400 and no quiz is created |
| TC-TEST-014-20 | Invalid duration | HTTP 400 |
| TC-TEST-014-21 | Invalid maximum attempts | HTTP 400 |
| TC-TEST-014-22 | Missing question options | HTTP 400 |
| TC-TEST-014-23 | Too few question options | HTTP 400 |
| TC-TEST-014-24 | Missing answer identifiers | HTTP 400 |
| TC-TEST-014-25 | Unknown quiz ID | Documented client error, never unexpected 500 |
| TC-TEST-014-26 | Unknown attempt ID | Documented client error, never unexpected 500 |
| TC-TEST-014-27 | Disabled environment | Readiness stops before live requests |
| TC-TEST-014-28 | Client unit tests | All unit tests pass |
| TC-TEST-014-29 | Checkstyle | Zero violations |
| TC-TEST-014-30 | Maven reports | Surefire/Failsafe reports are generated |

Live workflow tests require a synthetic course and an enrolled test student. They create unique quizzes and keep
created IDs in memory; cleanup attempts delete only records created by the test.

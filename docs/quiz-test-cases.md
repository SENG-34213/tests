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
| TC-TEST-014-10 | Student retrieves a quiz | Student-safe quiz and question data are returned without correctness flags |
| TC-TEST-014-11 | Student starts an attempt | Attempt ID, quiz ID, `IN_PROGRESS` state, and attempt number are returned |
| TC-TEST-014-12 | Student saves an answer | Answer is associated with the question, selected option, and attempt |
| TC-TEST-014-13 | Student submits an attempt | Score, total marks, rounded percentage, pass result and submission timestamp are correct |
| TC-TEST-014-14 | Maximum attempts | Starting beyond configured attempt limit returns `409 Conflict` |
| TC-TEST-014-31 | Duplicate active attempt | Starting another attempt while one is active returns `409 Conflict` |
| TC-TEST-014-32 | Submitted attempt is immutable | Resubmitting or changing an answer returns `409 Conflict` |
| TC-TEST-014-33 | Unanswered submission | Valid unanswered attempt submits with zero score and fails the configured pass mark |
| TC-TEST-014-34 | Teacher quiz results | After multiple student attempts, teacher results expose the latest submitted result once per student, with marks, percentage, pass state, student name and submission time. |
| TC-TEST-014-35 | Teacher course quiz gradebook | Teacher can retrieve a configured closed quiz and its result list for an assigned course. |
| TC-TEST-014-36 | Teacher quiz-results/gradebook access | Anonymous requests return `401`, student requests return `403`, unknown quiz returns `404`, and missing/malformed gradebook `courseId` returns `400`. |
| TC-TEST-014-37 | AI quiz generation input validation | Multipart generation with question count zero or unsupported question type returns `400` before the AI provider is called. |
| TC-TEST-014-38 | Student role cannot manage quizzes | Student requests to teacher quiz-management routes return `403`. |
| TC-TEST-014-39 | Teacher gradebook quiz route validates course ID | Missing or malformed `courseId` returns `400`. |
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
| TC-TEST-014-25 | Unknown quiz ID on read/update/delete/publish | `404 Not Found` |
| TC-TEST-014-26 | Unknown attempt ID | `404 Not Found` |
| TC-TEST-014-27 | Disabled environment | Readiness stops before live requests |
| TC-TEST-014-28 | Client unit tests | All unit tests pass |
| TC-TEST-014-29 | Checkstyle | Zero violations |
| TC-TEST-014-30 | Maven reports | Surefire/Failsafe reports are generated |

Live workflow tests require a synthetic course and an enrolled test student. They create unique quizzes and keep
created IDs in memory; cleanup attempts delete only records created by the test.

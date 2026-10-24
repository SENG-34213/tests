# TEST-011 Assignment Service test cases

| Test ID | Scenario | Expected result | Type |
|---|---|---|---|
| TC-TEST-011-01 | Teacher creates an assignment | `201`, response contains an ID and request values | Integration |
| TC-TEST-011-02 | Teacher retrieves an assignment | `200`, correct assignment data | Integration |
| TC-TEST-011-03 | Authorized user lists course assignments | `200`, created assignment is listed | Integration |
| TC-TEST-011-04 | Teacher updates an assignment | `200`, updated values persist | Integration |
| TC-TEST-011-19 | Teacher patches one assignment field | `200`, changed field persists and omitted fields remain unchanged | Integration |
| TC-TEST-011-05 | Teacher publishes an assignment | `200`, status is `PUBLISHED` | Workflow |
| TC-TEST-011-06 | Teacher closes an assignment | `200`, status is `CLOSED` | Workflow |
| TC-TEST-011-07 | Teacher deletes a test-created assignment | `200`, record is removed | Cleanup |
| TC-TEST-011-08 | Student creates a submission draft | `201`, status is `DRAFT` | Integration |
| TC-TEST-011-09 | Student updates a draft | `200`, content is updated | Integration |
| TC-TEST-011-10 | Student submits a draft | `200`, status is `SUBMITTED` | Workflow |
| TC-TEST-011-11 | Student lists own submissions | `200`, own submission is present | Authorization |
| TC-TEST-011-12 | Teacher lists assignment submissions | `200`, authorized list is returned | Authorization |
| TC-TEST-011-13 | Teacher grades a submission | `200`, marks and feedback persist | Grading |
| TC-TEST-011-14 | Teacher returns a submission | `200`, status is `RETURNED` | Grading |
| TC-TEST-011-15 | Unauthenticated protected request | `401` | Security |
| TC-TEST-011-16 | Invalid assignment body | `400` after authentication | Validation |
| TC-TEST-011-17 | Unknown assignment ID | `404 Not Found` | Error |
| TC-TEST-011-18 | Client path validation | Blank IDs are rejected before HTTP | Unit |
| TC-TEST-011-20 | Student lists own eligible course assignments | `200`, page metadata is valid, draft assignments are excluded, course filter is honored | Integration |
| TC-TEST-011-21 | Student assignment query validates page and date ranges | Invalid page size or `dueFrom > dueTo` returns `400` | Validation |
| TC-TEST-011-22 | Assignment PATCH with no fields | `400`; no assignment lookup or mutation is performed | Validation |
| TC-TEST-011-23 | Assignment create rejects invalid allowed-attempts, late-penalty, and rubric DTO bounds | `400` for attempts below one, penalty above 100, blank rubric criterion, and more than 50 rubric items | Validation |
| TC-TEST-011-24 | Rubric items persist through create, full update without a rubric, and partial PATCH | Rubric criteria, marks, and order are returned; omitted rubric remains unchanged | Workflow |
| TC-TEST-011-25 | Grading requires marks within the DTO range | Missing, negative, or greater-than-100 marks return `400` before submission lookup | Validation |
| TC-TEST-011-26 | Student cannot manage assignments | Student assignment creation and PATCH return `403`; own assignment listing remains available | Security |
| TC-TEST-011-27 | Teacher cannot create student submissions or use student assignment listing | Teacher receives `403` for submission creation and student-only assignment listing | Security |
| TC-TEST-011-28 | Assignment due date and attempt limit are returned by the lifecycle | Created assignment returns the configured due date and allowed-attempt count | Workflow |
| TC-TEST-011-29 | Late submission is rejected when late work is disabled | Past-due draft remains DRAFT; submit returns 409 and does not set submittedAt | Fixture-gated security regression |
| TC-TEST-011-30 | Submission beyond the configured attempt limit is rejected | Create returns 409; the student's submission count for the assignment does not increase | Fixture-gated security regression |
| TC-TEST-011-31 | Rubric marks cannot exceed assignment maximum | Creating a rubric totaling more than maxMarks returns 400 before a record is created | Fixture-gated validation |

Assignment attachment and submission file-storage scenarios are covered separately in
`assignment-attachment-test-cases.md` and remain fixture-gated.

# TEST-011 Assignment Service test cases

| Test ID | Scenario | Expected result | Type |
|---|---|---|---|
| TC-TEST-011-01 | Teacher creates an assignment | `201`, response contains an ID and request values | Integration |
| TC-TEST-011-02 | Teacher retrieves an assignment | `200`, correct assignment data | Integration |
| TC-TEST-011-03 | Authorized user lists course assignments | `200`, created assignment is listed | Integration |
| TC-TEST-011-04 | Teacher updates an assignment | `200`, updated values persist | Integration |
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
| TC-TEST-011-17 | Unknown assignment ID | Documented `404` or `400`, never unexpected `500` | Error |
| TC-TEST-011-18 | Client path validation | Blank IDs are rejected before HTTP | Unit |

Attachment and file-storage scenarios are intentionally reserved for a later ticket.

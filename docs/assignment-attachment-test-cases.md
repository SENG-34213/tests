# TEST-012 Assignment Attachment Test Cases

This document maps the automated TEST-012 coverage to the assignment-service
attachment and submission-file API. Integration tests run only with the
`integration` Maven profile and a reachable backend environment.

| Case | Coverage | Expected result |
| --- | --- | --- |
| TC-TEST-012-01 | Unauthenticated assignment attachment list/create/download/delete | 401 |
| TC-TEST-012-02 | Unauthenticated submission-file list/create/download/delete | 401 |
| TC-TEST-012-03 | Teacher attaches an existing material to an assignment | 201 |
| TC-TEST-012-04 | Teacher lists and downloads an assignment attachment | 200 |
| TC-TEST-012-05 | Enrolled student lists and downloads a published assignment attachment | 200 |
| TC-TEST-012-06 | Teacher deletes an assignment attachment | 200 |
| TC-TEST-012-07 | Student cannot create assignment attachments | 403 |
| TC-TEST-012-08 | Student attaches an existing material to their draft submission | 201 |
| TC-TEST-012-09 | Student and teacher list/download/delete permissions for submission files | 200 for owner and authorized teacher; owner delete succeeds |
| TC-TEST-012-10 | Teacher cannot create or delete submission files | 403 |
| TC-TEST-012-11 | Empty attachment/file request is rejected | 400 or 403, depending on authorization |
| TC-TEST-012-12 | Unknown assignment attachment ID is handled | 404, never 500 |
| TC-TEST-012-13 | Teacher multipart-uploads a synthetic assignment attachment, verifies metadata/list/download, then deletes the attachment and its test-created Material | 201/200; all test-created remote records are cleaned up |
| TC-TEST-012-14 | Student multipart-uploads a synthetic file to an owned draft submission, verifies metadata/list/download, then deletes the file and its test-created Material | 201/200; all test-created remote records are cleaned up |
| TC-TEST-012-15 | Multipart upload routes enforce authentication and role restrictions | Anonymous 401; student cannot upload assignment attachment (403); teacher cannot upload submission file (403) |
| TC-TEST-012-16 | A different student cannot list, download, or delete files on the fixture owner's submission | 403 for each cross-student operation; owner deletes the synthetic file and its test-created Material is cleaned up |
| TC-TEST-012-17 | Unknown submission-file ID is handled | 404, never 500 |

The workflow tests delete only attachment/file records and Material objects
created during that test. They do not delete assignments, submissions, or
materials supplied as fixtures. Multipart writes require
`TEST_ATTACHMENT_WRITE_ENABLED=true` and approved assignment/submission
fixtures; submission upload cleanup uses an administrator solely to remove the
test-created Material object after the student deletes its submission-file link.
`TC-TEST-012-16` additionally requires distinct `SECONDARY_STUDENT_TEST_USERNAME` and
`SECONDARY_STUDENT_TEST_PASSWORD` credentials. It creates only a synthetic
submission file and never attempts a cross-user mutation.

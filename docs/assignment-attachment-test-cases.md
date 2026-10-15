# TEST-012 Assignment Attachment Test Cases

This document maps the automated TEST-012 coverage to the assignment-service
attachment and submission-file API. Integration tests run only with the
`integration` Maven profile and a reachable backend environment.

| Case | Coverage | Expected result |
| --- | --- | --- |
| AT-01 | Unauthenticated attachment list/create/download/delete | 401 |
| AT-02 | Unauthenticated submission file list/create/download/delete | 401 |
| AT-03 | Teacher attaches an existing material to an assignment | 201 |
| AT-04 | Teacher lists and downloads an assignment attachment | 200 |
| AT-05 | Enrolled student lists and downloads a published assignment attachment | 200 |
| AT-06 | Teacher deletes an assignment attachment | 200 |
| AT-07 | Student cannot create or delete assignment attachments | 403 |
| AT-08 | Student attaches an existing material to their draft submission | 201 |
| AT-09 | Student lists, downloads, and deletes a submission file | 200 |
| AT-10 | Teacher cannot create or delete submission files | 403 |
| AT-11 | Empty attachment/file request is rejected | 400 or 403, depending on authorization |
| AT-12 | Unknown assignment, attachment, submission, or file ID is handled | 400 or 404, never 500 |

The workflow tests delete only the attachment or submission file created during
that test. They do not delete assignments, submissions, or materials supplied
as fixtures.

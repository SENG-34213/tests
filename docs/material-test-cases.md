# TEST-013 Material Service Test Cases

These cases cover the Material Service through its public API. They use synthetic
temporary files and approved non-production fixture IDs only.

| ID | Area | Expected result |
|---|---|---|
| TEST-013-01 | Student multipart upload | An enrolled student can upload a `SUBMISSION_ATTACHMENT` when the backend permits that purpose. |
| TEST-013-02 | Teacher multipart upload | A teacher can upload a learning material for an owned course. |
| TEST-013-03 | Admin upload | An administrator can upload a learning material. |
| TEST-013-04 | Upload response | The response contains the material ID, filename, content type, size, purpose, and status. |
| TEST-013-05 | Material retrieval | An authorized user can retrieve a material by ID. |
| TEST-013-06 | List materials | An authorized user can list materials. |
| TEST-013-07 | Pagination | List results honor page and size parameters. |
| TEST-013-08 | Search | Search returns the uploaded material by title. |
| TEST-013-09 | Course filtering | Filtering by course ID returns matching material records. |
| TEST-013-10 | Download metadata | Download returns a usable download URL and expiry metadata. |
| TEST-013-11 | Storage isolation | Download metadata does not expose storage keys or provider credentials. |
| TEST-013-12 | Teacher update | A teacher can update title and description for a managed material. |
| TEST-013-13 | Teacher archive | A teacher can archive a managed material. |
| TEST-013-14 | Teacher delete | A teacher can delete an archived test material. |
| TEST-013-15 | Admin audit | An administrator can run the maintenance audit. |
| TEST-013-16 | Safe cleanup | An administrator can run cleanup with `dryRun=true`. |
| TEST-013-17 | Cleanup safety | Tests never call destructive cleanup with `dryRun=false`. |
| TEST-013-18 | Missing file | Upload without a file is rejected with a client error. |
| TEST-013-19 | Empty file | Empty uploads are rejected. |
| TEST-013-20 | Unsupported file | Unsupported file types are rejected. |
| TEST-013-21 | Missing title | Uploads without a title are rejected. |
| TEST-013-22 | Missing course | Uploads without a course ID are rejected. |
| TEST-013-23 | Invalid update | Empty update payloads are rejected. |
| TEST-013-24 | Unknown retrieval | Unknown IDs return 400/404, never an unexpected server error. |
| TEST-013-25 | Unknown download | Unknown download IDs return 400/404. |
| TEST-013-26 | Unknown update | Unknown update IDs return 400/404. |
| TEST-013-27 | Unknown archive/delete | Unknown lifecycle IDs return 400/404. |
| TEST-013-28 | Unauthenticated access | Protected endpoints return 401 without a token. |
| TEST-013-29 | Role restrictions | Student management and teacher maintenance requests return 403. |
| TEST-013-30 | Temporary-file cleanup | Generated test files are deleted after each test. |

The executable integration classes are `MaterialLifecycleIT` and
`MaterialAuthenticationIT`. Tests requiring writes are skipped unless
`TEST_MATERIAL_WRITE_ENABLED=true` and a valid course fixture are configured.

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
| TEST-013-18 | Missing file | Upload without a file is rejected with a client error. |
| TEST-013-19 | Empty file | Empty uploads are rejected. |
| TEST-013-20 | Unsupported file | Unsupported file types are rejected. |
| TEST-013-21 | Missing title | Uploads without a title are rejected. |
| TEST-013-22 | Missing course | Uploads without a course ID are rejected. |
| TEST-013-23 | Invalid update | Empty update payloads are rejected. |
| TEST-013-24 | Unknown retrieval | Unknown IDs return `404 Not Found`. |
| TEST-013-25 | Unknown download | Unknown download IDs return `404 Not Found`. |
| TEST-013-26 | Unknown update | Unknown update IDs return `404 Not Found`. |
| TEST-013-27 | Unknown archive/delete | Unknown lifecycle IDs return `404 Not Found`. |
| TEST-013-28 | Unauthenticated access | Protected endpoints return 401 without a token. |
| TEST-013-29 | Student role restrictions | Student material-management and maintenance requests return 403. |
| TEST-013-30 | Temporary-file cleanup | Generated test files are deleted after each test. |
| TEST-013-31 | Verified AI sources | Teachers/admins can list AI source materials for a course; students and unauthenticated callers are denied. |
| TEST-013-32 | Purpose and status filters | List filters return the test upload under `LEARNING_MATERIAL`/`UPLOADED` and after transition under `LEARNING_MATERIAL`/`ARCHIVED`. |
| TEST-013-33 | Signed download returns uploaded bytes | With explicit byte-check opt-in and approved storage host, presigned HTTPS GET returns exactly the uploaded synthetic file bytes. |
| TEST-013-34 | Signed URL target safety | URL policy accepts only HTTPS with the exact configured host, no credentials/fragments, and no nonstandard port. |
| TEST-013-35 | Signed URL alternate target rejection | Alternate hosts and non-HTTPS schemes are rejected before any network request. |
| TEST-013-36 | Signed URL unsafe component rejection | User-info, fragments, and explicit ports other than 443 are rejected. |
| TEST-013-37 | Byte probe requires explicit configuration | Missing download URL or approved host is rejected before network access. |
| TEST-013-39 | A student outside the course cannot read or download its material | A distinct, unenrolled secondary student receives 403 for material retrieval and download metadata. |
| TEST-013-40 | Teacher maintenance restrictions | Teacher audit and cleanup requests return 403. |
| TEST-013-41 | Student profile image upload | Student uploads a synthetic PNG through a local Gateway configured to use the same confirmed local test bucket; the API returns its storage key and `image/png`, then exact-key cleanup removes it. |
| TEST-013-42 | Student downloads own profile image | Download returns the exact uploaded bytes and content type before exact-key cleanup. |

## Manual cleanup safeguard (not a JUnit case)

- **TEST-013-17 - Cleanup safety:** Review material cleanup calls and require `dryRun=true`; destructive cleanup must not be used by test automation.

This source-review safeguard is not a JUnit outcome and is excluded from the
generated case register. The executable cleanup test asserts dry-run behavior.

The executable integration classes are `MaterialLifecycleIT` and
`MaterialAuthenticationIT`. Tests requiring writes are skipped unless
`TEST_MATERIAL_WRITE_ENABLED=true` and a valid course fixture are configured.

Profile image tests additionally require
`TEST_PROFILE_IMAGE_WRITE_ENABLED=true` and
`TEST_PROFILE_IMAGE_STORAGE_CLEANUP_ENABLED=true`. They accept only a local
S3-compatible endpoint and a bucket whose name contains `test` or `local`,
matching `TEST_PROFILE_IMAGE_TEST_BUCKET`. The test removes only the exact
object key returned by its upload. AWS cloud endpoints and the backend's default
development bucket are rejected before upload.

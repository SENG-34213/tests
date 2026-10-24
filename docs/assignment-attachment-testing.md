# TEST-012 Assignment Attachment Testing

## Purpose

TEST-012 verifies the assignment-service integration with material-service for
assignment attachments and submission files. The tests use the public API
through the configured gateway. Step 6's late/attempt-limit contract fix is in
the companion backend branch `feature/TEST-034-step6-backend-contract`.

## Required environment

Copy `.env.example` to a local environment file or export equivalent values.
For live tests, include:

```text
TEST_ENV_ENABLED=true
TEST_REQUIRED_SERVICES=API_GATEWAY,AUTH_SERVICE,COURSE_SERVICE,ENROLLMENT_SERVICE,ASSIGNMENT_SERVICE,MATERIAL_SERVICE
TEST_ATTACHMENT_ASSIGNMENT_ID=<published assignment fixture>
TEST_ATTACHMENT_SUBMISSION_ID=<draft submission owned by the student fixture>
TEST_ATTACHMENT_MATERIAL_ID=<material in the assignment course>
TEST_ATTACHMENT_SUBMISSION_MATERIAL_ID=<SUBMISSION_ATTACHMENT material owned by the student fixture>
TEST_ATTACHMENT_WRITE_ENABLED=true
```

The configured student must be enrolled in the assignment course. The teacher
must own the assignment and submission's assignment. The assignment attachment
material must belong to the assignment course. Submission-file linking uses a
separate `SUBMISSION_ATTACHMENT` material uploaded by the configured student,
because the backend enforces both purpose and ownership. Use only approved
non-production fixtures.

`TEST_ATTACHMENT_WRITE_ENABLED` defaults to `false`. This prevents accidental
creation or deletion of records when a developer only wants readiness or read
coverage. Writes are performed only by the workflow tests and each created
record is deleted in a `finally` block. Existing fixtures are never deleted.

The upload workflows verify metadata and, when
`TEST_MATERIAL_DOWNLOAD_BYTES_ENABLED=true` and
`TEST_MATERIAL_DOWNLOAD_ALLOWED_HOST` names the exact approved storage host,
download the signed URL and compare the returned bytes with the temporary
synthetic file. Remote URLs require HTTPS. An explicitly enabled local MinIO
check may use HTTP only for its exact loopback host and port. Redirects are not
followed.

The assignment-attachment lifecycle also checks that a distinct secondary
student who is not enrolled in the assignment course receives 403 for list
and download. Submission-file ownership checks continue to verify that a
different student cannot list, download, or delete another student's file.

## Commands

```text
mvn clean verify
mvn verify -Pintegration -Dtest="*Attachment*IT,*File*IT"
mvn verify -Pintegration -Dtest="Assignment*IT,Submission*IT,*Attachment*IT,*File*IT"
```

Run the live commands only after the gateway, Auth Service, course, enrollment,
assignment, and material services are available. With `TEST_ENV_ENABLED=false`,
integration tests stop during readiness and do not issue business requests;
this mode is useful for confirming the guard is active, not for proving API
behavior.

## API behavior covered

- Assignment material attachment create/list/download/delete.
- Submission material file create/list/download/delete.
- Authentication failures, role restrictions, malformed request bodies, and
  unknown resource IDs.
- Student access to published assignment attachments and teacher access to
  submission files when the fixture ownership rules are satisfied.

The API accepts a `materialId`; these tests do not upload or delete material
records. Download responses are validated as API responses and verify the
returned attachment/file and material IDs; the material service owns the
actual download URL and content.

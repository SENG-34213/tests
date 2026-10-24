# TEST-013 Material Service Testing

## Purpose

This ticket verifies the Material Service through the API gateway and its public
REST contract. It does not modify the backend, access the database directly, or
test a production object-storage account.

## Covered API

- `POST /api/v1/materials` multipart upload (`title`, `description`, `purpose`, `courseId`, `file`)
- `GET /api/v1/materials` list, search, pagination, and course filtering
- `GET /api/v1/materials/{id}` retrieval
- `GET /api/v1/materials/{id}/download` download metadata
- `PUT /api/v1/materials/{id}` metadata update
- `POST /api/v1/materials/{id}/archive` archive
- `DELETE /api/v1/materials/{id}` delete
- `POST /api/v1/materials/maintenance/audit` maintenance audit
- `POST /api/v1/materials/maintenance/cleanup` non-destructive cleanup preview

## Environment

The integration profile requires the gateway, Auth Service, Course Service,
Enrollment Service, and Material Service. Configure these values outside source
control:

```text
TEST_ENV_ENABLED=true
TEST_REQUIRED_SERVICES=API_GATEWAY,AUTH_SERVICE,COURSE_SERVICE,ENROLLMENT_SERVICE,MATERIAL_SERVICE
MATERIAL_SERVICE_BASE_URL=http://localhost:8091
COURSE_SERVICE_BASE_URL=http://localhost:8082
ENROLLMENT_SERVICE_BASE_URL=http://localhost:8083
TEST_MATERIAL_COURSE_ID=<approved non-production course UUID>
TEST_MATERIAL_WRITE_ENABLED=false
```

Configure `STUDENT_TEST_*`, `TEACHER_TEST_*`, and `ADMIN_TEST_*` with dedicated
non-production users. Do not commit credentials or storage-provider secrets.
The optional cross-student material ownership check also requires distinct
SECONDARY_STUDENT_TEST_USERNAME and SECONDARY_STUDENT_TEST_PASSWORD values;
the secondary student must not be enrolled in TEST_MATERIAL_COURSE_ID.
The read-fixture ID is required for read-only fixture checks; write tests need a
course ID and explicit `TEST_MATERIAL_WRITE_ENABLED=true`.

## File and storage isolation

Tests create small unique `.txt` files in the operating system temporary
directory. The `AutoCloseable` test-file wrapper deletes each file after use.
By default the suite checks download metadata only. An optional byte-for-byte
check follows the presigned URL only when
`TEST_MATERIAL_DOWNLOAD_BYTES_ENABLED=true` and
`TEST_MATERIAL_DOWNLOAD_ALLOWED_HOST` is set to the exact approved storage host.
HTTPS is required for remote storage. A local MinIO check may opt into HTTP by
also setting `TEST_MATERIAL_DOWNLOAD_LOCAL_HTTP_ENABLED=true` and approving the
exact loopback origin including its port, such as `localhost:9000`; other hosts,
ports, and HTTP origins remain rejected. Redirects are never followed, and the
test never prints the signed URL or storage credentials.

## Cleanup behavior

The lifecycle test deletes the material it creates. Maintenance cleanup is
always called with `{"dryRun":true}` so it cannot delete unrelated records.
The suite does not configure or test destructive retention cleanup, S3/MinIO
infrastructure, or production storage.

## Verification commands

From the test repository:

```powershell
mvn clean verify
mvn verify -Pintegration -Dtest="Material*IT"
mvn verify -Pintegration -Dtest="Assignment*IT,Submission*IT,Material*IT"
```

With `TEST_ENV_ENABLED=false`, the integration tests must make no API request;
the readiness guard stops them before endpoint execution. Live integration runs
also require the backend services, test users, approved fixtures, and the
configured non-production storage provider to be available.

# API Contract Testing

TEST-026 protects representative API contracts without copying backend
implementation. It checks stable response shape, status codes, authentication
requirements, safe error structure, sensitive-field absence, and compatible
identifiers through the public API Gateway.

## Contract levels

- Common success and error response checks.
- Gateway authentication and routing checks.
- User login and current-user checks.
- Optional Course and Enrollment checks using `CONTRACT_COURSE_ID`.
- Existing service test classes remain the detailed endpoint coverage for
  Assignment, Material, Calendar, Quiz, Attendance, Communication, Exam,
  Payment, Notification, and AI behavior.

Contract tests do not assert unstable generated IDs or exact timestamps.

## Run locally

```powershell
$env:CONTRACT_TEST_ENABLED="true"
$env:CONTRACT_SCHEMA_STRICT="true"
$env:CONTRACT_BASE_URL="http://localhost:8080"
mvn -Dtest="*ContractTest" test
```

Contract tests skip clearly when disabled. A live run requires the API Gateway,
Auth Service, and dedicated synthetic credentials. Course and Enrollment checks also
require `CONTRACT_COURSE_ID`.

## Failure reporting

Failures identify the contract category and field without printing the full
response body. The assertion layer checks JSON object shape, stable field types,
error status, date-time format when present, and sensitive field absence.

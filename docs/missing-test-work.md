# Endpoint test evidence review

This is a source-level link audit generated from `backend-route-test-matrix.csv`. A linked assertion proves a request and response check are present in test source; it does not prove the test passed against a live deployment or that payload/state semantics are fully asserted.

The public/user-facing, admin-facing, teacher-facing, and service-root matrix has **187 rows** out of **235 HTTP route rows**. The other **48 internal/operational rows** are tracked in `backend-internal-route-auth-matrix.csv`; WebSocket routes are tracked separately.

Every public row has at least one linked JUnit request/case in this source audit: **187/187**. This is the project-guide endpoint-presence check, not live-run evidence.

## Assertion-category review flags

A count below means the matrix does not link that assertion category to the route. Categories are not blanket requirements for every route: use the route's SRS/backend contract to decide whether a success, unauthenticated, wrong-role, validation, or ownership case applies. `endpoint-test-matrix.csv` retains the exact route row and available test evidence.

- Missing linked success assertion: **4** route rows
- Missing linked unauthenticated assertion: **81** route rows
- Missing linked wrong role assertion: **99** route rows
- Missing linked validation assertion: **138** route rows
- Missing linked ownership assertion: **174** route rows
- Missing linked role contract review assertion: **0** route rows

## Required follow-up

1. Review rows with a missing linked success assertion against the SRS. Add a safe positive-path test where the endpoint contract defines success and the route currently has only negative evidence.
2. Review authorization, validation, and ownership flags against the effective Gateway and service contracts. Add only the applicable missing scenarios; do not infer public access or ownership coverage from a generic status code.
3. Keep internal routes in `backend-internal-route-auth-matrix.csv`; service-to-service identities and mutation effects need their specific contracts and approved fixtures.
4. Execute the live suites against the approved non-production deployment and retain JUnit evidence before claiming runtime coverage.

## Scope and status

A zero category flag means the audit could not link a matching request/assertion string; it is not a product failure by itself. A nonzero flag is a review item, not proof that related behavior has no test. This matrix is static source evidence only and does not certify all business semantics, Gateway behavior, or deployed service health.

# Remaining endpoint test work

This report is generated from `backend-route-test-matrix.csv` and linked assertion text. A linked status-code assertion is evidence of a response check, not proof that the full business contract, payload, persistence, or security semantics are correct. Do not treat `NOT EVIDENCED` as proof that no test exists; it means this route audit does not link one.

Public/user-facing, admin-facing, teacher-facing and service-root rows audited: **187** of 235 source route rows. Internal/operational routes excluded from this public endpoint matrix: **48**; track them in `backend-internal-route-auth-matrix.csv`.

## Route-by-route gap counts

- no linked case: **0** route rows
- success: **31** route rows
- unauthenticated: **84** route rows
- wrong role: **117** route rows
- validation: **139** route rows
- ownership: **177** route rows
- role contract review: **0** route rows

## Required follow-up

1. Review each `required_role` entry against the effective service security filter chain, method/class annotations, Gateway rules, and SRS. Entries explicitly asking for security-chain review are unresolved, not assumed public.
2. For every `NOT EVIDENCED` cell in the CSV, inspect the referenced test code and either link the exact request plus assertion or add a test. A mere case name is insufficient.
3. Add explicit ownership/isolation checks for resource routes (foreign user/course/assignment/enrollment/etc.); generic 403/404 checks are not ownership evidence.
4. Confirm success payload/schema and state changes, validation field/path, and role matrix per SRS; the matrix intentionally does not infer these from status codes.
5. Review every endpoint row and every service-root/Gateway mapping for effective ingress path correctness. WebSocket routes are tracked separately in `backend-websocket-route-test-matrix.csv`.

## Scope and caveats

This artifact is a traceable audit baseline, not a declaration that Step 1 is complete: every row retains source and test evidence, while unproven categories remain visibly open. The 48 internal/operational route mappings are listed separately because they are not public product endpoints; their authentication/role evidence remains in the internal auth matrix.

# TEST-020 AI test cases

| ID | Scenario | Expected result |
| --- | --- | --- |
| TC-TEST-020-01 | AI readiness | AI Service is reachable through the configured environment. |
| TC-TEST-020-02 | AI health | Health returns `service=ai-service` and `status=UP`. |
| TC-TEST-020-03 | Current user | Authenticated user ID and roles are returned. |
| TC-TEST-020-04 | Conversation create | Authorized teacher can create a synthetic conversation. |
| TC-TEST-020-05 | Conversation list | User receives only own conversations. |
| TC-TEST-020-06 | Conversation detail | Owner can retrieve details and messages. |
| TC-TEST-020-07 | Conversation update | Owner can update a valid title. |
| TC-TEST-020-08 | Conversation delete | Owner can delete a created conversation. |
| TC-TEST-020-09 | Conversation ownership | User cannot read or mutate another user's conversation. |
| TC-TEST-020-10 | Chat authorization | Teacher/admin chat access follows the backend contract. |
| TC-TEST-020-11 | Chat validation | Missing/blank/oversized chat fields are rejected. |
| TC-TEST-020-12 | Content generation | Administrator generation is tested with a deterministic provider. |
| TC-TEST-020-13 | Generation authorization | Student/teacher generation access is rejected. |
| TC-TEST-020-14 | Lesson summary | Accessible lesson summary is tested when fixture/provider exists. |
| TC-TEST-020-15 | Material summary | Accessible material summary is tested when fixture/provider exists. |
| TC-TEST-020-16 | Quiz draft | Multipart request validation and effective public security are tested. |
| TC-TEST-020-17 | Quiz boundaries | Question count, course, difficulty, and question type validation is covered. |
| TC-TEST-020-18 | Unauthenticated access | Protected AI endpoints reject missing authentication. |
| TC-TEST-020-19 | Invalid resources | Invalid conversation, lesson, and material IDs are handled safely. |
| TC-TEST-020-20 | Provider failures | Deterministic provider failures are tested when available. |
| TC-TEST-020-21 | Sensitive data | No secrets, private prompts, or real user data are committed or exposed. |
| TC-TEST-020-22 | Documentation | Provider mode, fixtures, cleanup, and skip behavior are documented. |

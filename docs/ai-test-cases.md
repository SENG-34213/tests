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
| TC-TEST-020-09 | Conversation ownership | With distinct teacher/admin identities, a different owner receives `404` for conversation read, messages, update, and delete; the fixture is cleaned up by its owner. |
| TC-TEST-020-10 | Chat authorization | Teacher/admin chat access follows the backend contract. |
| TC-TEST-020-11 | Chat validation | Missing/blank/oversized chat fields are rejected. |
| TC-TEST-020-12 | Content generation | Administrator generation is tested with a deterministic provider. |
| TC-TEST-020-13 | Generation authorization | Student/teacher generation access is rejected. |
| TC-TEST-020-14 | Lesson summary | Accessible lesson summary is tested when fixture/provider exists. |
| TC-TEST-020-15 | Material summary | Accessible material summary is tested when fixture/provider exists. |
| TC-TEST-020-16 | Quiz draft | Multipart request validation, deterministic provider success, requested question count, and effective public security are tested. |
| TC-TEST-020-17 | Quiz boundaries | Question count, course, difficulty, and supported question type are validated before provider invocation. |
| TC-TEST-020-18 | Unauthenticated access | Protected AI endpoints reject missing authentication. |
| TC-TEST-020-19 | Invalid resources | Invalid conversation, lesson, and material IDs are handled safely. |
| TC-TEST-020-20 | Provider failures | Deterministic provider failures are tested when available. |
| TC-TEST-020-21 | Sensitive data | No secrets, private prompts, or real user data are committed or exposed. |
| TC-TEST-020-22 | Documentation | Provider mode, fixtures, cleanup, and skip behavior are documented. |
| TC-TEST-020-23 | Deterministic chat and text generation | Enabled provider returns `200` and response data, not merely a non-server-error status. |
| TC-TEST-020-24 | Deterministic lesson summary | Configured lesson ID produces a non-empty summary. |
| TC-TEST-020-25 | Deterministic material summary | Configured material ID produces a non-empty summary. |
| TC-TEST-020-26 | Deterministic quiz draft | Configured course produces the requested number of questions using the backend-supported `MULTIPLE_CHOICE_SINGLE_ANSWER` type. |
| TC-TEST-020-27 | Conversation ownership isolation | A configured admin cannot read or mutate a teacher-owned conversation, and the disposable conversation is removed by the teacher. |
| TC-TEST-020-28 | Public quiz-generation semantic validation | Valid UUID with zero questions, invalid difficulty, or unsupported question type returns `400` before the AI provider is called. |
| TC-TEST-020-29 | AI WebSocket authentication | With explicit opt-in, a teacher token authenticates STOMP CONNECT at `/ws/ai`. |
| TC-TEST-020-30 | AI WebSocket unauthenticated access | Missing bearer authentication is rejected at both the AI Service and Gateway. |
| TC-TEST-020-31 | AI WebSocket role authorization | A student token is rejected at CONNECT; teacher/admin roles remain the allowed identities. |
| TC-TEST-020-32 | AI WebSocket destination authorization | A teacher cannot subscribe to an unapproved destination or send to an unapproved application destination. |
| TC-TEST-020-33 | AI WebSocket response delivery | An unknown synthetic conversation produces PROCESSING followed by a safe not-found error on the authenticated user's queue, without calling the provider. |

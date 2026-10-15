# AI Service integration testing

TEST-020 verifies the AI Service through the API Gateway with real test-user tokens and synthetic learning data.

## Scope

Coverage includes public AI health, authenticated user context, conversation lifecycle and ownership, role-based chat
and content generation, lesson/material summarization, quiz-generation request validation, and invalid-resource
handling. Tests do not judge AI factual quality, access the database, use production providers, or send confidential
or real student data.

## Security and provider safety

Use a deterministic mock provider or an explicitly approved AI test mode. Keep `AI_TEST_MODE=false` by default and
never commit API keys, prompts containing personal data, JWTs, provider responses with sensitive data, or real
student information. Provider failures, rate limits, retries, and circuit breakers are tested only when deterministic
fixtures exist. The provider test class requires a configured conversation ID and skips otherwise.

## Configuration

```properties
TEST_ENV_ENABLED=true
TEST_REQUIRED_SERVICES=API_GATEWAY,KEYCLOAK,AI_SERVICE
AI_SERVICE_BASE_URL=http://localhost:8088
AI_TEST_MODE=true
AI_PROVIDER=mock
AI_PROVIDER_API_KEY=<test-only-key>
TEST_AI_WRITE_ENABLED=true
TEST_AI_CONVERSATION_ID=<synthetic-conversation-uuid>
```

Summarization and quiz scenarios additionally require the relevant Course, Lesson, Material, and Quiz dependencies
and approved synthetic fixture IDs. Quiz generation is intentionally tested according to the backend's effective
security configuration: `/api/ai/quizzes/generate` is publicly permitted in the inspected backend branch, while
conversation/chat/generation endpoints have method-level role restrictions.

## Endpoints covered

- `GET /api/ai/health`
- `GET /api/ai/me`
- Conversation create/list/detail/messages/update/delete
- `POST /api/ai/chat`
- `POST /api/ai/generate`
- Lesson and material summarization endpoints
- Multipart `POST /api/ai/quizzes/generate`

## Cleanup and limitations

Conversations created by enabled tests are deleted through the supported API. No direct database cleanup is used.
Lesson/material/quiz and external-provider workflows skip when approved fixtures or deterministic dependencies are
unavailable. WebSocket chat, provider billing, quality evaluation, and production email/data are out of scope.

## Running

```powershell
mvn test
mvn verify -Pintegration
mvn -Pintegration verify -Dtest="Ai*IT"
```

The normal test phase runs `AiClientTest`. Integration classes use the repository `*IT` convention and require the
AI Service and configured authentication/readiness environment.

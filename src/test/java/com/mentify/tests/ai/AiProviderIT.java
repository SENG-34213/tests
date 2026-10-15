package com.mentify.tests.ai;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiProviderIT extends AiIntegrationTestSupport {

    @Test
    void deterministicProviderSupportsSafeChatAndGenerationTests() {
        AiTestData data = AiTestData.fromEnvironment();
        assumeTrue(data.writeEnabled() && data.providerEnabled() && data.conversationId() != null,
                "Enable TEST_AI_WRITE_ENABLED and AI_TEST_MODE with a deterministic provider");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            assertTrue(ai.chat(Map.of("conversationId", data.conversationId().toString(),
                    "clientMessageId", "test-020-" + System.nanoTime(),
                    "message", "Explain inheritance in one sentence."), teacher.accessToken()).statusCode() < 500);
            assertTrue(ai.generate(Map.of("prompt", "Give one synthetic Java inheritance example."),
                    admin.accessToken()).statusCode() < 500);
        } finally {
            logout(teacher);
            logout(admin);
        }
    }
}

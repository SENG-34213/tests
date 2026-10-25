package com.mentify.tests.environment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class IntegrationTestExecutionConditionTest {

    @Test
    void shouldDisableLiveTestClassesByDefault() {
        assertTrue(IntegrationTestExecutionCondition.disabledReason("example.CourseLifecycleIT", Map.of()).isPresent());
        assertTrue(IntegrationTestExecutionCondition.disabledReason("example.ServiceIntegrationTest", Map.of()).isPresent());
    }

    @Test
    void shouldLeaveUnitTestsEnabledWhenIntegrationIsDisabled() {
        assertFalse(IntegrationTestExecutionCondition.disabledReason("example.ClientTest", Map.of()).isPresent());
    }

    @Test
    void shouldEnableLiveTestClassWhenEnvironmentIsExplicitlyEnabled() {
        assertFalse(IntegrationTestExecutionCondition.disabledReason("example.CourseLifecycleIT",
                Map.of("TEST_ENV_ENABLED", "true")).isPresent());
    }

    @Test
    void shouldRequireStagingTargetForStagingSmokeClass() {
        assertTrue(IntegrationTestExecutionCondition.disabledReason("com.mentify.tests.staging.StagingSmokeIT",
                Map.of("TEST_ENV_ENABLED", "true")).isPresent());
        assertFalse(IntegrationTestExecutionCondition.disabledReason("com.mentify.tests.staging.StagingSmokeIT",
                Map.of("TEST_ENV_ENABLED", "true", "STAGING_ENVIRONMENT", "staging")).isPresent());
    }
}

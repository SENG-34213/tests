package com.mentify.tests.environment;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Safely disables live integration tests until their target is explicitly enabled. */
public final class IntegrationTestExecutionCondition implements ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(
            "Integration test environment is explicitly enabled");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        String className = context.getTestClass().map(Class::getName).orElse("");
        return disabledReason(className, System.getenv())
                .map(ConditionEvaluationResult::disabled)
                .orElse(ENABLED);
    }

    static Optional<String> disabledReason(String className, Map<String, String> environment) {
        if (!isIntegrationClass(className)) {
            return Optional.empty();
        }
        if (!Boolean.parseBoolean(environment.getOrDefault("TEST_ENV_ENABLED", "false"))) {
            return Optional.of("Live integration tests are disabled; set TEST_ENV_ENABLED=true for an approved target");
        }
        if (className.endsWith(".StagingSmokeIT")
                && !"staging".equals(environment.get("STAGING_ENVIRONMENT"))) {
            return Optional.of("Staging smoke tests require STAGING_ENVIRONMENT=staging");
        }
        return Optional.empty();
    }

    private static boolean isIntegrationClass(String className) {
        return className.endsWith("IT") || className.endsWith("IntegrationTest");
    }
}

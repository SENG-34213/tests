package com.mentify.tests.quiz;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import com.mentify.tests.auth.AuthenticationClient;
import com.mentify.tests.auth.AuthenticationConfiguration;
import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import org.junit.jupiter.api.BeforeAll;

abstract class QuizIntegrationTestSupport {

    protected static ApiClient apiClient;
    protected static AuthenticationClient authenticationClient;
    protected static QuizClient quizClient;
    protected static QuizQuestionClient questionClient;
    protected static QuizAttemptClient attemptClient;

    @BeforeAll
    static void requireQuizEnvironment() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        BackendReadinessClient readiness = new BackendReadinessClient(environment);
        readiness.awaitRequiredServices();
        readiness.awaitService(RequiredService.ASSIGNMENT_SERVICE);
        readiness.awaitService(RequiredService.COURSE_SERVICE);
        readiness.awaitService(RequiredService.ENROLLMENT_SERVICE);
        apiClient = new ApiClient(ApiClientConfiguration.fromEnvironment(environment, false));
        authenticationClient = new AuthenticationClient(apiClient, AuthenticationConfiguration.fromEnvironment());
        quizClient = new QuizClient(apiClient);
        questionClient = new QuizQuestionClient(apiClient);
        attemptClient = new QuizAttemptClient(apiClient);
    }

    protected static AuthenticationSession login(TestRole role) {
        return authenticationClient.login(role);
    }

    protected static void logout(AuthenticationSession session) {
        if (session != null && session.refreshable()) {
            authenticationClient.logout(session);
        } else if (session != null) {
            session.clear();
        }
    }
}

package com.mentify.tests.assignment;

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

abstract class AssignmentIntegrationTestSupport {

    protected static ApiClient apiClient;
    protected static AuthenticationClient authenticationClient;
    protected static AssignmentClient assignmentClient;
    protected static SubmissionClient submissionClient;

    @BeforeAll
    static void requireAssignmentEnvironment() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        BackendReadinessClient readinessClient = new BackendReadinessClient(environment);
        readinessClient.awaitRequiredServices();
        readinessClient.awaitService(RequiredService.ASSIGNMENT_SERVICE);
        readinessClient.awaitService(RequiredService.COURSE_SERVICE);
        readinessClient.awaitService(RequiredService.ENROLLMENT_SERVICE);

        apiClient = new ApiClient(ApiClientConfiguration.fromEnvironment(environment, false));
        authenticationClient = new AuthenticationClient(
                apiClient, AuthenticationConfiguration.fromEnvironment());
        assignmentClient = new AssignmentClient(apiClient);
        submissionClient = new SubmissionClient(apiClient);
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

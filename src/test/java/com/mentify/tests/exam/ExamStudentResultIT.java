package com.mentify.tests.exam;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.junit.jupiter.api.Test;

class ExamStudentResultIT {

    private static ExamClient exams;
    private static AuthenticationClient authentication;

    @BeforeAll
    static void requireExamEnvironment() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        BackendReadinessClient readiness = new BackendReadinessClient(environment);
        readiness.awaitRequiredServices();
        readiness.awaitService(RequiredService.EXAM_SERVICE);
        ApiClient api = new ApiClient(ApiClientConfiguration.fromEnvironment(environment, false));
        authentication = new AuthenticationClient(api, AuthenticationConfiguration.fromEnvironment());
        exams = new ExamClient(api);
    }

    @Test
    void enrolledStudentCanRetrieveOwnExamResults() {
        AuthenticationSession student = authentication.login(TestRole.STUDENT);
        try {
            assertEquals(200, exams.myResults(student.accessToken()).statusCode());
        } finally {
            if (student.refreshable()) {
                authentication.logout(student);
            } else {
                student.clear();
            }
        }
    }
}

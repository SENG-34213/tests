package com.mentify.tests.exam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import com.mentify.tests.auth.AuthenticationClient;
import com.mentify.tests.auth.AuthenticationConfiguration;
import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ExamLifecycleIT {

    private static ExamClient exams;
    private static AuthenticationClient authentication;

    @BeforeAll
    static void requireExamEnvironment() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        BackendReadinessClient readiness = new BackendReadinessClient(environment);
        readiness.awaitRequiredServices();
        readiness.awaitService(RequiredService.EXAM_SERVICE);
        readiness.awaitService(RequiredService.COURSE_SERVICE);
        readiness.awaitService(RequiredService.ENROLLMENT_SERVICE);
        ApiClient api = new ApiClient(ApiClientConfiguration.fromEnvironment(environment, false));
        authentication = new AuthenticationClient(api, AuthenticationConfiguration.fromEnvironment());
        exams = new ExamClient(api);
    }

    @Test
    void teacherCanRunTheExamLifecycleAndReadResults() {
        ExamTestData data = ExamTestData.fromEnvironment();
        assumeTrue(data.writeEnabled() && data.fixturesConfigured(),
                "Set TEST_EXAM_WRITE_ENABLED=true and approved exam fixtures to run write tests");
        AuthenticationSession teacher = authentication.login(TestRole.TEACHER);
        try {
            String token = teacher.accessToken();
            Response created = exams.create(data.createRequest(), token);
            assertEquals(201, created.statusCode());
            String examId = created.jsonPath().getString("data.id");
            assertTrue(examId != null && !examId.isBlank());
            assertEquals(200, exams.get(examId, token).statusCode());
            assertEquals(200, exams.listByCourse(data.courseId().toString(), token).statusCode());
            assertEquals(200, exams.update(examId, data.updateRequest(), token).statusCode());
            assertEquals(200, exams.schedule(examId, token).statusCode());
            assertEquals(200, exams.startMarking(examId, token).statusCode());
            assertEquals(200, exams.enterMarks(examId, data.marksRequest(), token).statusCode());
            assertEquals(200, exams.complete(examId, token).statusCode());
            assertEquals(200, exams.results(examId, token).statusCode());
            assertEquals(200, exams.summary(examId, token).statusCode());
        } finally {
            logout(teacher);
        }
    }

    private static void logout(AuthenticationSession session) {
        if (session != null && session.refreshable()) {
            authentication.logout(session);
        } else if (session != null) {
            session.clear();
        }
    }
}

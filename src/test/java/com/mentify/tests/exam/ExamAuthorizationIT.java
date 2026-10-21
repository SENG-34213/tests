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

class ExamAuthorizationIT {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";
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
    void unauthenticatedExamRequestsReturnUnauthorized() {
        assertEquals(401, exams.get(UNKNOWN_ID, null).statusCode());
        assertEquals(401, exams.listByCourse(UNKNOWN_ID, null).statusCode());
        assertEquals(401, exams.myResults(null).statusCode());
        assertEquals(401, exams.myUpcoming(null).statusCode());
        assertEquals(401, exams.studentExam(UNKNOWN_ID, null).statusCode());
        assertEquals(401, exams.attachments(UNKNOWN_ID, null).statusCode());
        assertEquals(401, exams.downloadAttachment(UNKNOWN_ID, UNKNOWN_ID, null).statusCode());
        assertEquals(401, exams.deleteAttachment(UNKNOWN_ID, UNKNOWN_ID, null).statusCode());
    }

    @Test
    void studentCannotUseManagementEndpointsButCanUseOwnResultsEndpoint() {
        AuthenticationSession student = authentication.login(TestRole.STUDENT);
        try {
            assertEquals(403, exams.create(java.util.Map.of(), student.accessToken()).statusCode());
            assertEquals(403, exams.get(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, exams.schedule(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(200, exams.myResults(student.accessToken()).statusCode());
            assertEquals(200, exams.myUpcoming(student.accessToken()).statusCode());
            assertEquals(404, exams.studentExam(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, exams.deleteAttachment(UNKNOWN_ID, UNKNOWN_ID, student.accessToken()).statusCode());
        } finally {
            if (student.refreshable()) {
                authentication.logout(student);
            } else {
                student.clear();
            }
        }
    }

    @Test
    void nonStudentsCannotUseStudentExamDetailRoute() {
        AuthenticationSession teacher = authentication.login(TestRole.TEACHER);
        try {
            assertEquals(403, exams.studentExam(UNKNOWN_ID, teacher.accessToken()).statusCode());
        } finally {
            if (teacher.refreshable()) {
                authentication.logout(teacher);
            } else {
                teacher.clear();
            }
        }
    }
}

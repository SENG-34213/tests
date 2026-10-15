package com.mentify.tests.exam;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import com.mentify.tests.auth.AuthenticationClient;
import com.mentify.tests.auth.AuthenticationConfiguration;
import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ExamValidationIT {

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
    void invalidExamAndMarksPayloadsReturnClientErrors() {
        AuthenticationSession teacher = authentication.login(TestRole.TEACHER);
        try {
            String token = teacher.accessToken();
            assertTrue(exams.create(Map.of(), token).statusCode() < 500);
            assertTrue(exams.create(Map.of("courseId", "not-a-uuid", "title", " ", "type", "INVALID"), token)
                    .statusCode() < 500);
            assertTrue(exams.enterMarks(UNKNOWN_ID, Map.of("results", java.util.List.of()), token).statusCode() < 500);
            assertTrue(exams.enterMarks(UNKNOWN_ID,
                    Map.of("results", java.util.List.of(Map.of("studentId", "not-a-uuid",
                            "attendanceStatus", "INVALID", "marksObtained", new BigDecimal("1.234")))), token)
                    .statusCode() < 500);
            assertTrue(exams.update(UNKNOWN_ID, Map.of("title", "bad", "examDate", LocalDate.now().toString(),
                    "startTime", LocalTime.NOON.toString(), "endTime", LocalTime.of(11, 0).toString()), token)
                    .statusCode() < 500);
        } finally {
            logout(teacher);
        }
    }

    @Test
    void unknownExamIdentifiersNeverProduceServerErrors() {
        AuthenticationSession teacher = authentication.login(TestRole.TEACHER);
        try {
            String token = teacher.accessToken();
            assertTrue(exams.get(UNKNOWN_ID, token).statusCode() < 500);
            assertTrue(exams.listByCourse(UNKNOWN_ID, token).statusCode() < 500);
            assertTrue(exams.cancel(UNKNOWN_ID, token).statusCode() < 500);
            assertTrue(exams.complete(UNKNOWN_ID, token).statusCode() < 500);
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

package com.mentify.tests.course;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import io.restassured.response.Response;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InternalRouteAuthorizationIT {

    private static InternalRouteAuthorizationClient internal;

    @BeforeAll
    static void requireInternalRouteEnvironment() {
        assumeTrue(Boolean.parseBoolean(System.getenv("TEST_INTERNAL_ROUTE_AUTH_ENABLED")),
                "Set TEST_INTERNAL_ROUTE_AUTH_ENABLED=true to probe internal-route authorization");
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        BackendReadinessClient readiness = new BackendReadinessClient(environment);
        readiness.awaitRequiredServices();
        for (RequiredService service : new RequiredService[]{
                RequiredService.API_GATEWAY, RequiredService.ASSIGNMENT_SERVICE, RequiredService.ATTENDANCE_SERVICE,
                RequiredService.CALENDAR_SERVICE, RequiredService.COURSE_SERVICE,
                RequiredService.EXAM_SERVICE, RequiredService.MATERIAL_SERVICE, RequiredService.USER_SERVICE}) {
            readiness.awaitService(service);
        }
        ApiClient api = new ApiClient(ApiClientConfiguration.fromEnvironment(environment, false));
        internal = new InternalRouteAuthorizationClient(api);
    }

    @Test
    void unauthenticatedRequestsAreRejectedAcrossInternalRoutes() {
        assertDenied(internal.assignmentReconcile("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.quizReconcile("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.attendanceReconcile("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.authProvision());
        assertDenied(internal.authChangePassword("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.authDelete("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.calendarUpsertAssignment());
        assertDenied(internal.calendarCancelAssignment("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.calendarUpsertQuiz());
        assertDenied(internal.calendarCancelQuiz("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.calendarUpsertClass());
        assertDenied(internal.calendarCancelClass("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.calendarUpsertAttendance());
        assertDenied(internal.calendarCancelAttendance("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.calendarUpsertExam());
        assertDenied(internal.calendarCancelExam("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.courseInternalLesson("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.courseMaterialScope("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.moduleMaterialScope("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.lessonMaterialScope("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.examReconcile("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.materialAssociate("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.materialGetAssociation("00000000-0000-0000-0000-000000000001"));
        assertDenied(internal.userAuthProfile());
        assertDenied(internal.userActivate());
        assertDenied(internal.userRecordLastLogin());
    }

    private static void assertDenied(Response response) {
        assertTrue(Set.of(401, 403).contains(response.statusCode()),
                "Expected an authentication/authorization rejection, got " + response.statusCode());
    }
}

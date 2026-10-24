package com.mentify.tests.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InternalAnalyticsIT {

    private static AuthenticationClient authentication;
    private static InternalAnalyticsClient analytics;
    private static AuthenticationSession admin;

    @BeforeAll
    static void requireAnalyticsServices() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        BackendReadinessClient readiness = new BackendReadinessClient(environment);
        readiness.awaitRequiredServices();
        for (RequiredService service : new RequiredService[]{
                RequiredService.ASSIGNMENT_SERVICE, RequiredService.ATTENDANCE_SERVICE,
                RequiredService.COURSE_SERVICE, RequiredService.ENROLLMENT_SERVICE,
                RequiredService.EXAM_SERVICE, RequiredService.PAYMENT_SERVICE,
                RequiredService.USER_SERVICE}) {
            readiness.awaitService(service);
        }
        ApiClient api = new ApiClient(ApiClientConfiguration.fromEnvironment(environment, false));
        authentication = new AuthenticationClient(api, AuthenticationConfiguration.fromEnvironment());
        analytics = new InternalAnalyticsClient(api);
        admin = authentication.login(TestRole.ADMIN);
    }

    @AfterAll
    static void logoutAdmin() {
        if (admin != null && admin.refreshable()) authentication.logout(admin);
        else if (admin != null) admin.clear();
    }

    @Test
    void administratorCanReadInternalAnalyticsOverviewsThroughTheirGatewayRoutes() {
        String token = admin.accessToken();
        assertEquals(200, analytics.assignmentOverview(token).statusCode());
        assertEquals(200, analytics.quizContractOverview(token).statusCode());
        assertEquals(200, analytics.quizOverview(token).statusCode());
        assertEquals(200, analytics.quizTodayPerformance(token).statusCode());
        assertEquals(200, analytics.attendanceOverview(token).statusCode());
        assertEquals(200, analytics.courseOverview(token).statusCode());
        assertEquals(200, analytics.enrollmentOverview(token).statusCode());
        assertEquals(200, analytics.examOverview(token).statusCode());
        assertEquals(200, analytics.paymentOverview(token).statusCode());
        assertEquals(200, analytics.userRegistrationOverview(token).statusCode());
        assertEquals(200, analytics.userOverview(token).statusCode());
        assertEquals(200, analytics.userRegistrationOverviewDiscovery(token).statusCode());
        assertEquals(200, analytics.userOverviewDiscovery(token).statusCode());
    }

    @Test
    void administratorCanReadCourseAnalyticsSummariesForConfiguredCourse() {
        String courseId = System.getenv("TEST_INTERNAL_ANALYTICS_COURSE_ID");
        assumeTrue(courseId != null && !courseId.isBlank(),
                "Set TEST_INTERNAL_ANALYTICS_COURSE_ID to an approved course fixture");
        String token = admin.accessToken();
        assertEquals(200, analytics.assignmentCourseSummary(courseId, token).statusCode());
        assertEquals(200, analytics.quizContractCourseSummary(courseId, token).statusCode());
        assertEquals(200, analytics.quizCourseSummary(courseId, token).statusCode());
        assertEquals(200, analytics.attendanceCourseSummary(courseId, token).statusCode());
        assertEquals(200, analytics.enrollmentCourseSummary(courseId, token).statusCode());
        assertEquals(200, analytics.examCourseSummary(courseId, token).statusCode());
    }
}

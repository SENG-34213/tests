package com.mentify.tests.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import com.mentify.tests.auth.AuthenticationClient;
import com.mentify.tests.auth.AuthenticationConfiguration;
import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AdminAnalyticsIT {

    private static AuthenticationClient authentication;
    private static AnalyticsClient analytics;
    private static AuthenticationSession admin;
    private static AuthenticationSession student;

    @BeforeAll
    static void requireAnalyticsEnvironment() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        BackendReadinessClient readiness = new BackendReadinessClient(environment);
        readiness.awaitRequiredServices();
        ApiClient api = new ApiClient(ApiClientConfiguration.fromEnvironment(environment, false));
        authentication = new AuthenticationClient(api, AuthenticationConfiguration.fromEnvironment());
        analytics = new AnalyticsClient(api);
        admin = authentication.login(TestRole.ADMIN);
        student = authentication.login(TestRole.STUDENT);
    }

    @AfterAll
    static void logoutSessions() {
        logout(admin);
        logout(student);
    }

    @Test
    void administratorCanReadAdminAnalyticsOverview() {
        Response response = analytics.adminOverview(admin.accessToken());
        assertEquals(200, response.statusCode(), response.asString());
        assertNotNull(response.jsonPath().get("users"), response.asString());
        assertNotNull(response.jsonPath().get("partial"), response.asString());
    }

    @Test
    void nonAdminCannotReadAdminAnalyticsOverview() {
        Response forbidden = analytics.adminOverview(student.accessToken());
        assertEquals(403, forbidden.statusCode(), forbidden.asString());
        Response unauthorized = analytics.adminOverview(null);
        assertEquals(401, unauthorized.statusCode(), unauthorized.asString());
    }

    private static void logout(AuthenticationSession session) {
        if (session != null && session.refreshable()) authentication.logout(session);
        else if (session != null) session.clear();
    }
}

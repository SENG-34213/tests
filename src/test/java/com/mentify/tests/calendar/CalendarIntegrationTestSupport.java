package com.mentify.tests.calendar;

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

abstract class CalendarIntegrationTestSupport {

    protected static ApiClient apiClient;
    protected static AuthenticationClient authenticationClient;
    protected static CalendarClient calendarClient;

    @BeforeAll
    static void requireCalendarEnvironment() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        BackendReadinessClient readinessClient = new BackendReadinessClient(environment);
        readinessClient.awaitRequiredServices();
        readinessClient.awaitService(RequiredService.CALENDAR_SERVICE);

        apiClient = new ApiClient(ApiClientConfiguration.fromEnvironment(environment, false));
        authenticationClient = new AuthenticationClient(
                apiClient, AuthenticationConfiguration.fromEnvironment());
        calendarClient = new CalendarClient(apiClient);
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

    protected static void requireAdditionalService(RequiredService service) {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        new BackendReadinessClient(environment).awaitService(service);
    }
}

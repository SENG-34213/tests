package com.mentify.tests.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import com.mentify.tests.notification.NotificationClient;
import com.mentify.tests.payment.PaymentClient;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

class AdminOperationsJourneyIT extends EndToEndIntegrationTestSupport {

    @Test
    void administratorCanReviewUsersCoursesPaymentsAndNotificationsThroughGateway() {
        requireServices(RequiredService.COURSE_SERVICE, RequiredService.PAYMENT_SERVICE,
                RequiredService.NOTIFICATION_SERVICE);
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            String token = admin.accessToken();
            Response usersResponse = users.adminUsers("STUDENT", token);
            assertEquals(200, usersResponse.statusCode(), usersResponse.asString());

            Response coursesResponse = courses.getAllCourses(token);
            assertEquals(200, coursesResponse.statusCode(), coursesResponse.asString());

            Response paymentsResponse = new PaymentClient(api).adminPayments(
                    null, null, null, null, null, null, 0, 5, token);
            assertEquals(200, paymentsResponse.statusCode(), paymentsResponse.asString());

            Response notificationsResponse = new NotificationClient(api).list(0, 5, false, token);
            assertEquals(200, notificationsResponse.statusCode(), notificationsResponse.asString());
        } finally {
            logout(admin);
        }
    }

    private static void requireServices(RequiredService... services) {
        BackendReadinessClient readiness = new BackendReadinessClient(TestEnvironmentConfiguration.fromEnvironment());
        for (RequiredService service : services) {
            readiness.awaitService(service);
        }
    }
}

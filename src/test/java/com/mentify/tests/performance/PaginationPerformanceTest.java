package com.mentify.tests.performance;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.notification.NotificationClient;
import org.junit.jupiter.api.Test;

class PaginationPerformanceTest extends PerformanceTestSupport {

    @Test
    void notificationPageLatencyMeetsBaseline() {
        AuthenticationSession session = login();
        try {
            NotificationClient notifications = new NotificationClient(api);
            measure("notification-page", () -> notifications.list(
                    0, 20, false, session.accessToken()));
        } finally {
            session.clear();
        }
    }
}

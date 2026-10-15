package com.mentify.tests.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CalendarAuthenticationIT extends CalendarIntegrationTestSupport {

    @Test
    void shouldRejectUpcomingCalendarRequestsWithoutAuthentication() {
        Response response = calendarClient.upcoming(null, 5);

        assertEquals(401, response.statusCode());
    }

    @Test
    void shouldRejectEventCreationWithoutAuthentication() {
        Response response = calendarClient.create(validEventRequest(), null);

        assertEquals(401, response.statusCode());
    }

    @Test
    void shouldAllowAnAuthenticatedStudentToQueryUpcomingEvents() {
        AuthenticationSession session = login(TestRole.STUDENT);

        try {
            Response response = calendarClient.upcoming(session.accessToken(), 5);

            assertEquals(200, response.statusCode());
            assertEquals("success", response.jsonPath().getString("status"));
            assertNotNull(response.jsonPath().get("data"));
        } finally {
            logout(session);
        }
    }

    private static Map<String, Object> validEventRequest() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        return Map.of(
                "title", "Integration test event",
                "description", "A non-persistent authentication contract request",
                "eventType", "CUSTOM",
                "startDateTime", start.toString(),
                "endDateTime", start.plusHours(1).toString(),
                "allDay", false,
                "visibility", "PRIVATE");
    }
}

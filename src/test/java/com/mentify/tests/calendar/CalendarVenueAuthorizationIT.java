package com.mentify.tests.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CalendarVenueAuthorizationIT extends CalendarIntegrationTestSupport {

    @Test
    void shouldRequireAuthenticationToListVenues() {
        assertEquals(401, calendarClient.venues(null).statusCode());
    }

    @Test
    void shouldAllowAuthenticatedUsersToListVenues() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            Response response = calendarClient.venues(student.accessToken());
            assertEquals(200, response.statusCode(), response.asString());
            assertEquals(200, response.jsonPath().getInt("statusCode"), response.asString());
            assertNotNull(response.jsonPath().get("data"), response.asString());
        } finally {
            logout(student);
        }
    }

    @Test
    void shouldRestrictVenueCreationToAdministrators() {
        AuthenticationSession student = login(TestRole.STUDENT);
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Map<String, Object> venue = Map.of("name", "TEST no-write", "location", "TEST", "capacity", 1);
            assertEquals(403, calendarClient.createVenue(venue, student.accessToken()).statusCode());
            assertEquals(403, calendarClient.createVenue(venue, teacher.accessToken()).statusCode());
            assertEquals(401, calendarClient.createVenue(venue, null).statusCode());
        } finally {
            logout(student);
            logout(teacher);
        }
    }

    @Test
    void shouldRejectInvalidVenueDataForAdministrator() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            Response response = calendarClient.createVenue(Map.of(), admin.accessToken());
            assertEquals(400, response.statusCode(), response.asString());
        } finally {
            logout(admin);
        }
    }
}

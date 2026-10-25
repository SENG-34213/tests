package com.mentify.tests.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CalendarVenueAuthorizationIT extends CalendarIntegrationTestSupport {

    @Test
    void administratorCanCreateVenueInConfirmedDisposableCalendarDatabase() {
        assumeTrue(Boolean.parseBoolean(System.getenv().getOrDefault(
                        "TEST_CALENDAR_VENUE_WRITE_ENABLED", "false")),
                "Set TEST_CALENDAR_VENUE_WRITE_ENABLED=true for the isolated venue-create probe");
        assumeTrue(Boolean.parseBoolean(System.getenv().getOrDefault(
                        "TEST_CALENDAR_DISPOSABLE_DATABASE_CONFIRMED", "false")),
                "Confirm the Calendar database is disposable; venue creation has no delete endpoint");

        AuthenticationSession admin = login(TestRole.ADMIN);
        String venueName = "CODEx-TEST-" + UUID.randomUUID();
        try {
            Response created = calendarClient.createVenue(Map.of(
                    "name", venueName,
                    "location", "Automated test venue",
                    "capacity", 5), admin.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            assertEquals(201, created.jsonPath().getInt("statusCode"), created.asString());
            String venueId = created.jsonPath().getString("data.id");
            assertNotNull(venueId, created.asString());
            assertEquals(venueName, created.jsonPath().getString("data.name"));

            Response listed = calendarClient.venues(admin.accessToken());
            assertEquals(200, listed.statusCode(), listed.asString());
            assertTrue(listed.jsonPath().getList("data.name", String.class).contains(venueName),
                    listed.asString());
            assertTrue(listed.jsonPath().getList("data.id", String.class).contains(venueId),
                    listed.asString());
        } finally {
            logout(admin);
        }
    }

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

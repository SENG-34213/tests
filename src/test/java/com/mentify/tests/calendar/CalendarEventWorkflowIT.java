package com.mentify.tests.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CalendarEventWorkflowIT extends CalendarIntegrationTestSupport {

    @Test
    void shouldAllowTeacherToCreateUpdateAndReadAnEvent() {
        AuthenticationSession session = login(TestRole.TEACHER);
        String eventId = null;

        try {
            Response createResponse = calendarClient.create(validEventRequest("teacher-create"), session.accessToken());
            assertSuccessful(createResponse, 201);
            eventId = eventId(createResponse);

            Map<String, Object> updateRequest = validEventRequest("teacher-update");
            Response updateResponse = calendarClient.update(eventId, updateRequest, session.accessToken());
            assertSuccessful(updateResponse, 200);
            assertEquals("success", updateResponse.jsonPath().getString("status"));
            Response getResponse = calendarClient.getEvent(eventId, session.accessToken());
            assertSuccessful(getResponse, 200);
            assertEquals(eventId, getResponse.jsonPath().getString("data.id"));
            assertEquals(updateRequest.get("title"), getResponse.jsonPath().getString("data.title"));
        } finally {
            cancelIfCreated(eventId, session);
            logout(session);
        }
    }

    @Test
    void shouldAllowAdminToCreateAnEvent() {
        AuthenticationSession session = login(TestRole.ADMIN);
        String eventId = null;

        try {
            Response response = calendarClient.create(validEventRequest("admin-create"), session.accessToken());
            assertSuccessful(response, 201);
            eventId = eventId(response);
        } finally {
            cancelIfCreated(eventId, session);
            logout(session);
        }
    }

    @Test
    void shouldAllowStudentToRetrieveCalendarEvents() {
        AuthenticationSession session = login(TestRole.STUDENT);

        try {
            LocalDateTime start = LocalDateTime.now().minusDays(1).withNano(0);
            LocalDateTime end = LocalDateTime.now().plusDays(30).withNano(0);
            Response response = calendarClient.events(session.accessToken(), start, end);

            assertEquals(200, response.statusCode(), response.asString());
            assertEquals("success", response.jsonPath().getString("status"));
            assertNotNull(response.jsonPath().get("data"));
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldAllowTeacherToCancelAnEvent() {
        AuthenticationSession session = login(TestRole.TEACHER);
        String eventId = null;

        try {
            Response createResponse = calendarClient.create(validEventRequest("teacher-cancel"), session.accessToken());
            assertSuccessful(createResponse, 201);
            eventId = eventId(createResponse);

            Response cancelResponse = calendarClient.cancel(eventId, session.accessToken());
            assertSuccessful(cancelResponse, 200);
            assertEquals("success", cancelResponse.jsonPath().getString("status"));
            eventId = null;
        } finally {
            cancelIfCreated(eventId, session);
            logout(session);
        }
    }

    @Test
    void shouldRejectStudentEventCreation() {
        AuthenticationSession session = login(TestRole.STUDENT);

        try {
            Response response = calendarClient.create(validEventRequest("student-create"), session.accessToken());

            assertEquals(403, response.statusCode(), response.asString());
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldRejectStudentEventUpdate() {
        AuthenticationSession session = login(TestRole.STUDENT);

        try {
            Response response = calendarClient.update("00000000-0000-0000-0000-000000000000",
                    validEventRequest("student-update"), session.accessToken());

            assertEquals(403, response.statusCode(), response.asString());
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldRejectStudentEventCancellation() {
        AuthenticationSession session = login(TestRole.STUDENT);

        try {
            Response response = calendarClient.cancel("00000000-0000-0000-0000-000000000000",
                    session.accessToken());

            assertEquals(403, response.statusCode(), response.asString());
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldRejectInvalidEventRequest() {
        AuthenticationSession session = login(TestRole.TEACHER);

        try {
            Response response = calendarClient.create(Map.of("allDay", false), session.accessToken());

            assertEquals(400, response.statusCode(), response.asString());
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldRejectEventWithInvalidDateRange() {
        AuthenticationSession session = login(TestRole.TEACHER);
        LocalDateTime start = LocalDateTime.now().plusDays(2).withNano(0);

        try {
            Map<String, Object> request = Map.of(
                    "title", uniqueTitle("invalid-range"),
                    "eventType", "CUSTOM",
                    "startDateTime", start.toString(),
                    "endDateTime", start.minusHours(1).toString(),
                    "allDay", false,
                    "visibility", "PRIVATE");
            Response response = calendarClient.create(request, session.accessToken());

            assertEquals(400, response.statusCode(), response.asString());
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldReturnNotFoundForUnknownEvent() {
        AuthenticationSession session = login(TestRole.TEACHER);

        try {
            Response response = calendarClient.update("00000000-0000-0000-0000-000000000000",
                    validEventRequest("unknown-event"), session.accessToken());

            assertEquals(404, response.statusCode(), response.asString());
            assertEquals(404, calendarClient.getEvent("00000000-0000-0000-0000-000000000000",
                    session.accessToken()).statusCode());
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldRejectEventSearchWithoutRequiredDateRange() {
        AuthenticationSession session = login(TestRole.TEACHER);
        try {
            Response response = calendarClient.events(session.accessToken());
            assertEquals(400, response.statusCode(), response.asString());
        } finally {
            logout(session);
        }
    }

    private static Map<String, Object> validEventRequest(String scenario) {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        return Map.of(
                "title", uniqueTitle(scenario),
                "description", "TEST-007 Calendar Service workflow event",
                "eventType", "CUSTOM",
                "startDateTime", start.toString(),
                "endDateTime", start.plusHours(1).toString(),
                "allDay", false,
                "visibility", "PRIVATE",
                "location", "Integration test environment");
    }

    private static String uniqueTitle(String scenario) {
        return "TEST-007 " + scenario + " " + UUID.randomUUID();
    }

    private static void assertSuccessful(Response response, int expectedStatus) {
        assertEquals(expectedStatus, response.statusCode(), response.asString());
        assertEquals("success", response.jsonPath().getString("status"));
    }

    private static String eventId(Response response) {
        String eventId = response.jsonPath().getString("data.id");
        if (eventId == null || eventId.isBlank()) {
            eventId = response.jsonPath().getString("data.eventId");
        }
        assertNotNull(eventId, "Create response did not contain data.id or data.eventId");
        return eventId;
    }

    private static void cancelIfCreated(String eventId, AuthenticationSession session) {
        if (eventId != null && session != null && session.authenticated()) {
            calendarClient.cancel(eventId, session.accessToken());
        }
    }
}

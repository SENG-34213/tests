package com.mentify.tests.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.RequiredService;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CalendarEventWorkflowIT extends CalendarIntegrationTestSupport {

    @Test
    void shouldAllowTeacherToCreateUpdateAndReadAnEvent() {
        UUID courseId = configuredCourseId();
        org.junit.jupiter.api.Assumptions.assumeTrue(courseId != null,
                "Set TEST_CALENDAR_COURSE_ID to a course assigned to the primary teacher");
        requireAdditionalService(RequiredService.COURSE_SERVICE);
        AuthenticationSession session = login(TestRole.TEACHER);
        String eventId = null;

        try {
            Response createResponse = calendarClient.create(
                    courseEventRequest("teacher-create", courseId), session.accessToken());
            assertSuccessful(createResponse, 201);
            eventId = eventId(createResponse);

            Map<String, Object> updateRequest = courseEventRequest("teacher-update", courseId);
            Response updateResponse = calendarClient.update(eventId, updateRequest, session.accessToken());
            assertSuccessful(updateResponse, 200);
            assertEquals(200, updateResponse.jsonPath().getInt("statusCode"), updateResponse.asString());
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
            assertEquals(200, response.jsonPath().getInt("statusCode"), response.asString());
            assertNotNull(response.jsonPath().get("data"));
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldAllowTeacherToCancelAnEvent() {
        UUID courseId = configuredCourseId();
        org.junit.jupiter.api.Assumptions.assumeTrue(courseId != null,
                "Set TEST_CALENDAR_COURSE_ID to a course assigned to the primary teacher");
        requireAdditionalService(RequiredService.COURSE_SERVICE);
        AuthenticationSession session = login(TestRole.TEACHER);
        String eventId = null;

        try {
            Response createResponse = calendarClient.create(
                    courseEventRequest("teacher-cancel", courseId), session.accessToken());
            assertSuccessful(createResponse, 201);
            eventId = eventId(createResponse);

            Response cancelResponse = calendarClient.cancel(eventId, session.accessToken());
            assertSuccessful(cancelResponse, 200);
            assertEquals(200, cancelResponse.jsonPath().getInt("statusCode"), cancelResponse.asString());
            eventId = null;
        } finally {
            cancelIfCreated(eventId, session);
            logout(session);
        }
    }

    @Test
    void anotherTeacherCannotUpdateOrCancelAnUnassignedCourseEvent() {
        UUID courseId = configuredCourseId();
        org.junit.jupiter.api.Assumptions.assumeTrue(courseId != null,
                "Set TEST_CALENDAR_COURSE_ID to a course assigned only to the primary teacher");
        org.junit.jupiter.api.Assumptions.assumeTrue(hasSecondaryTeacherCredentials(),
                "Set distinct SECONDARY_TEACHER_TEST_USERNAME/PASSWORD credentials");
        requireAdditionalService(RequiredService.COURSE_SERVICE);

        AuthenticationSession owner = login(TestRole.TEACHER);
        AuthenticationSession otherTeacher = null;
        String eventId = null;
        try {
            Response created = calendarClient.create(courseEventRequest("ownership", courseId), owner.accessToken());
            assertSuccessful(created, 201);
            eventId = eventId(created);

            otherTeacher = login(TestRole.SECONDARY_TEACHER);
            assertEquals(404, calendarClient.getEvent(eventId, otherTeacher.accessToken()).statusCode(),
                    "An unassigned teacher must not discover the course event");
            assertEquals(403, calendarClient.update(eventId, courseEventRequest("other-teacher", courseId),
                    otherTeacher.accessToken()).statusCode());
            assertEquals(403, calendarClient.cancel(eventId, otherTeacher.accessToken()).statusCode());
        } finally {
            try {
                logout(otherTeacher);
            } finally {
                try {
                    cancelIfCreated(eventId, owner);
                } finally {
                    logout(owner);
                }
            }
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
        LocalDateTime start = uniqueStartDateTime();
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

    private static Map<String, Object> courseEventRequest(String scenario, UUID courseId) {
        LocalDateTime start = uniqueStartDateTime();
        return Map.of(
                "title", uniqueTitle(scenario),
                "description", "TEST-007 Calendar Service workflow event",
                "eventType", "CUSTOM",
                "startDateTime", start.toString(),
                "endDateTime", start.plusHours(1).toString(),
                "allDay", false,
                "visibility", "COURSE",
                "courseId", courseId.toString(),
                "location", "Integration test environment");
    }

    private static UUID configuredCourseId() {
        String value = System.getenv("TEST_CALENDAR_COURSE_ID");
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid UUID for TEST_CALENDAR_COURSE_ID", exception);
        }
    }

    private static boolean hasSecondaryTeacherCredentials() {
        String username = System.getenv("SECONDARY_TEACHER_TEST_USERNAME");
        String password = System.getenv("SECONDARY_TEACHER_TEST_PASSWORD");
        return username != null && !username.isBlank() && password != null && !password.isBlank();
    }

    private static String uniqueTitle(String scenario) {
        return "TEST-007 " + scenario + " " + UUID.randomUUID();
    }

    private static LocalDateTime uniqueStartDateTime() {
        int minuteOffset = Math.floorMod(UUID.randomUUID().hashCode(), 840);
        return LocalDateTime.now().plusDays(2).withHour(7).withMinute(0).withSecond(0).withNano(0)
                .plusMinutes(minuteOffset);
    }

    private static void assertSuccessful(Response response, int expectedStatus) {
        assertEquals(expectedStatus, response.statusCode(), response.asString());
        assertEquals(expectedStatus, response.jsonPath().getInt("statusCode"), response.asString());
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

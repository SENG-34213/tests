package com.mentify.tests.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

class CourseCrudIT extends CourseIntegrationTestSupport {

    private static final String UNKNOWN_COURSE_ID = "00000000-0000-0000-0000-000000000000";

    @BeforeEach
    void requireCourseFixtures() {
        Assumptions.assumeTrue(CourseTestDataConfiguration.fromEnvironment().configured(),
                "Set TEST_COURSE_GRADE_ID and TEST_COURSE_TEACHER_ID for persistent Course CRUD tests");
    }

    @Test
    void shouldCreateCourseAsAdmin() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            Response response = courseClient.create(validCourseRequest("TEST-009 Create"), admin.accessToken());

            assertEquals(201, response.statusCode(), response.asString());
            assertEquals(201, response.jsonPath().getInt("statusCode"));
            assertNotNull(courseId(response));
            deleteIfCreated(response, admin.accessToken());
        } finally {
            logout(admin);
        }
    }

    @Test
    void shouldReadAndUpdateCreatedCourseAsAdmin() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        String courseId = null;
        try {
            courseId = createCourse(admin.accessToken());

            Response read = courseClient.getById(courseId, admin.accessToken());
            assertEquals(200, read.statusCode(), read.asString());
            assertEquals(courseId, read.jsonPath().getString("data.id"));

            Response update = courseClient.update(
                    courseId, validCourseRequest("TEST-009 Updated"), admin.accessToken());
            assertEquals(200, update.statusCode(), update.asString());
            assertEquals("TEST-009 Updated", update.jsonPath().getString("data.courseName"));
        } finally {
            deleteIfNeeded(courseId, admin.accessToken());
            logout(admin);
        }
    }

    @Test
    void shouldAllowStudentAndTeacherToLookupCreatedCourse() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        String courseId = null;
        try {
            courseId = createCourse(admin.accessToken());

            assertLookupAllowed(courseId, TestRole.STUDENT);
            assertLookupAllowed(courseId, TestRole.TEACHER);
        } finally {
            deleteIfNeeded(courseId, admin.accessToken());
            logout(admin);
        }
    }

    @Test
    void shouldFindCreatedCourseByBulkLookupAsAdmin() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        String courseId = null;
        try {
            courseId = createCourse(admin.accessToken());

            Response response = courseClient.bulkLookup(
                    Map.of("ids", List.of(courseId)), admin.accessToken());

            assertEquals(200, response.statusCode(), response.asString());
            assertTrue(response.jsonPath().getList("data.id").contains(courseId), response.asString());
        } finally {
            deleteIfNeeded(courseId, admin.accessToken());
            logout(admin);
        }
    }

    @Test
    void shouldDeleteCreatedCourseAsAdmin() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        String courseId = null;
        try {
            courseId = createCourse(admin.accessToken());

            Response delete = courseClient.delete(courseId, admin.accessToken());
            assertEquals(200, delete.statusCode(), delete.asString());
            assertEquals(200, delete.jsonPath().getInt("statusCode"));

            Response readAfterDelete = courseClient.getById(courseId, admin.accessToken());
            assertEquals(404, readAfterDelete.statusCode(), readAfterDelete.asString());
            courseId = null;
        } finally {
            deleteIfNeeded(courseId, admin.accessToken());
            logout(admin);
        }
    }

    @Test
    void shouldRejectInvalidCoursePayloadAsAdmin() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            Response response = courseClient.create(Map.of(), admin.accessToken());

            assertEquals(400, response.statusCode(), response.asString());
        } finally {
            logout(admin);
        }
    }

    @Test
    void shouldReturnNotFoundForUnknownCourse() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            Response response = courseClient.getById(UNKNOWN_COURSE_ID, admin.accessToken());

            assertEquals(404, response.statusCode(), response.asString());
        } finally {
            logout(admin);
        }
    }

    private static String createCourse(String bearerToken) {
        Response response = courseClient.create(validCourseRequest("TEST-009 Lifecycle"), bearerToken);
        assertEquals(201, response.statusCode(), response.asString());
        return courseId(response);
    }

    private static void assertLookupAllowed(String courseId, TestRole role) {
        AuthenticationSession session = login(role);
        try {
            Response response = courseClient.lookup(courseId, session.accessToken());

            assertEquals(200, response.statusCode(), response.asString());
            assertEquals(courseId, response.jsonPath().getString("data.id"));
        } finally {
            logout(session);
        }
    }

    private static String courseId(Response response) {
        String courseId = response.jsonPath().getString("data.id");
        assertNotNull(courseId, response.asString());
        return courseId;
    }

    private static void deleteIfCreated(Response response, String bearerToken) {
        String courseId = response.jsonPath().getString("data.id");
        deleteIfNeeded(courseId, bearerToken);
    }

    private static void deleteIfNeeded(String courseId, String bearerToken) {
        if (courseId != null) {
            Response response = courseClient.delete(courseId, bearerToken);
            assertEquals(200, response.statusCode(), response.asString());
        }
    }

    private static Map<String, Object> validCourseRequest(String namePrefix) {
        CourseTestDataConfiguration data = CourseTestDataConfiguration.fromEnvironment();
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("courseName", namePrefix + " " + UUID.randomUUID());
        request.put("courseDescription", "Created by TEST-009 and removed during test cleanup.");
        request.put("courseFeeMonthly", 25.00);
        request.put("gradeId", data.gradeId().toString());
        request.put("assignedTeacherId", data.teacherId().toString());
        request.put("subject", "Integration Testing");
        request.put("isOnline", true);
        request.put("discountOfferPercent", 0.0);
        request.put("isVisible", true);
        request.put("isPublished", false);
        return request;
    }
}

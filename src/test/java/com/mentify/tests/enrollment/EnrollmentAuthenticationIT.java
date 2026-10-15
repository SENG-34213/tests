package com.mentify.tests.enrollment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class EnrollmentAuthenticationIT extends EnrollmentIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void shouldRejectProtectedEndpointsWithoutAuthentication() {
        assertEquals(401, enrollmentClient.getActiveEnrollments(null).statusCode());
        assertEquals(401, enrollmentClient.create(validAdminRequest(), null).statusCode());
        assertEquals(401, enrollmentClient.selfEnroll(validStudentRequest(), null).statusCode());
        assertEquals(401, enrollmentClient.update(UNKNOWN_ID, validUpdateRequest(), null).statusCode());
        assertEquals(401, enrollmentClient.membership(UNKNOWN_ID, UNKNOWN_ID, null).statusCode());
        assertEquals(401, enrollmentClient.studentCourses(UNKNOWN_ID, null).statusCode());
        assertEquals(401, enrollmentClient.courseStudents(UNKNOWN_ID, null).statusCode());
        assertEquals(401, enrollmentClient.unenrolledStudents(null).statusCode());
    }

    @Test
    void shouldRejectStudentsAndTeachersFromManagingEnrollments() {
        assertStatusForRole(TestRole.STUDENT, token -> enrollmentClient.getActiveEnrollments(token), 403);
        assertStatusForRole(TestRole.TEACHER, token -> enrollmentClient.getActiveEnrollments(token), 403);
        assertStatusForRole(TestRole.TEACHER, token -> enrollmentClient.create(validAdminRequest(), token), 403);
        assertStatusForRole(TestRole.STUDENT, token -> enrollmentClient.update(UNKNOWN_ID, validUpdateRequest(), token), 403);
        assertStatusForRole(TestRole.TEACHER, token -> enrollmentClient.update(UNKNOWN_ID, validUpdateRequest(), token), 403);
    }

    @Test
    void shouldRejectStudentsFromTeacherAndAdminQueries() {
        assertStatusForRole(TestRole.STUDENT, token -> enrollmentClient.courseStudents(UNKNOWN_ID, token), 403);
        assertStatusForRole(TestRole.STUDENT, token -> enrollmentClient.unenrolledStudents(token), 403);
        assertStatusForRole(TestRole.TEACHER, token -> enrollmentClient.unenrolledStudents(token), 403);
    }

    @Test
    void shouldRejectStudentFromAnotherStudentsCourseList() {
        EnrollmentTestDataConfiguration data = requireReadFixtures();
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            Response response = enrollmentClient.studentCourses(data.otherStudentId().toString(), student.accessToken());

            assertEquals(403, response.statusCode(), response.asString());
        } finally {
            logout(student);
        }
    }

    @Test
    void shouldAllowStudentToReadOwnActiveCourseIds() {
        EnrollmentTestDataConfiguration data = requireReadFixtures();
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            Response response = enrollmentClient.studentCourses(data.studentId().toString(), student.accessToken());

            assertEquals(200, response.statusCode(), response.asString());
            assertEquals("success", response.jsonPath().getString("status"));
            assertNotNull(response.jsonPath().getList("data"));
        } finally {
            logout(student);
        }
    }

    @Test
    void shouldAllowStudentTeacherAndAdminToCheckMembership() {
        EnrollmentTestDataConfiguration data = requireReadFixtures();
        for (TestRole role : List.of(TestRole.STUDENT, TestRole.TEACHER, TestRole.ADMIN)) {
            AuthenticationSession session = login(role);
            try {
                Response response = enrollmentClient.membership(
                        data.studentId().toString(), data.courseId().toString(), session.accessToken());

                assertEquals(200, response.statusCode(), response.asString());
                assertTrue(response.as(Boolean.class));
            } finally {
                logout(session);
            }
        }
    }

    @Test
    void shouldReturnFalseForUnknownMembershipCombination() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            Response response = enrollmentClient.membership(UNKNOWN_ID, UNKNOWN_ID, admin.accessToken());

            assertEquals(200, response.statusCode(), response.asString());
            assertFalse(response.as(Boolean.class));
        } finally {
            logout(admin);
        }
    }

    @Test
    void shouldAllowTeacherToListStudentsByCourse() {
        EnrollmentTestDataConfiguration data = requireReadFixtures();
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response response = enrollmentClient.courseStudents(data.courseId().toString(), teacher.accessToken());

            assertEquals(200, response.statusCode(), response.asString());
            assertEquals("success", response.jsonPath().getString("status"));
            assertNotNull(response.jsonPath().getList("data"));
        } finally {
            logout(teacher);
        }
    }

    @Test
    void shouldAllowAdminToListActiveAndUnenrolledStudents() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            Response enrollments = enrollmentClient.getActiveEnrollments(admin.accessToken());
            assertEquals(200, enrollments.statusCode(), enrollments.asString());
            assertEquals("success", enrollments.jsonPath().getString("status"));
            assertNotNull(enrollments.jsonPath().getList("data"));

            Response unenrolled = enrollmentClient.unenrolledStudents(admin.accessToken());
            assertEquals(200, unenrolled.statusCode(), unenrolled.asString());
            assertEquals("success", unenrolled.jsonPath().getString("status"));
            assertNotNull(unenrolled.jsonPath().getList("data"));
        } finally {
            logout(admin);
        }
    }

    @Test
    void shouldRejectInvalidEnrollmentRequests() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(400, enrollmentClient.create(Map.of(), admin.accessToken()).statusCode());
            assertEquals(400, enrollmentClient.create(
                    Map.of("studentId", "not-a-uuid", "courseIds", List.of(UNKNOWN_ID)),
                    admin.accessToken()).statusCode());
            assertEquals(400, enrollmentClient.selfEnroll(Map.of("courseIds", List.of()),
                    student.accessToken()).statusCode());
            assertEquals(400, enrollmentClient.update("not-a-uuid", validUpdateRequest(), admin.accessToken())
                    .statusCode());
        } finally {
            logout(admin);
            logout(student);
        }
    }

    @Test
    void shouldReturnNotFoundForUnknownEnrollmentUpdate() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            Response response = enrollmentClient.update(UNKNOWN_ID, validUpdateRequest(), admin.accessToken());

            assertTrue(response.statusCode() == 404 || response.statusCode() == 400, response.asString());
        } finally {
            logout(admin);
        }
    }

    @Test
    void shouldSupportStudentSelfEnrollmentWhenWriteFixturesAreExplicitlyEnabled() {
        EnrollmentTestDataConfiguration data = requireWriteFixtures();
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            Response response = enrollmentClient.selfEnroll(
                    Map.of("courseIds", List.of(data.alternateCourseId().toString())), student.accessToken());

            assertTrue(response.statusCode() == 201 || response.statusCode() == 400, response.asString());
            if (response.statusCode() == 201) {
                assertEquals("success", response.jsonPath().getString("status"));
                assertTrue(enrollmentClient.membership(
                        data.studentId().toString(), data.alternateCourseId().toString(), student.accessToken())
                        .as(Boolean.class));
            }
        } finally {
            logout(student);
        }
    }

    @Test
    void shouldSupportAdminEnrollmentManagementWhenWriteFixturesAreExplicitlyEnabled() {
        EnrollmentTestDataConfiguration data = requireWriteFixtures();
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            Response create = enrollmentClient.create(validAdminRequest(data), admin.accessToken());
            assertTrue(create.statusCode() == 201 || create.statusCode() == 400, create.asString());
            if (create.statusCode() == 201) {
                String enrollmentId = create.jsonPath().getString("data.id");
                assertNotNull(enrollmentId, create.asString());

                Response update = enrollmentClient.update(enrollmentId,
                        validUpdateRequest(data), admin.accessToken());
                assertEquals(200, update.statusCode(), update.asString());
                assertEquals("success", update.jsonPath().getString("status"));
            }
        } finally {
            logout(admin);
        }
    }

    private static void assertStatusForRole(TestRole role, EnrollmentRequest request, int expectedStatus) {
        AuthenticationSession session = login(role);
        try {
            assertEquals(expectedStatus, request.send(session.accessToken()).statusCode());
        } finally {
            logout(session);
        }
    }

    private static EnrollmentTestDataConfiguration requireReadFixtures() {
        EnrollmentTestDataConfiguration data = testData();
        Assumptions.assumeTrue(data.readFixturesConfigured(),
                "Configure TEST_ENROLLMENT_STUDENT_ID, TEST_ENROLLMENT_OTHER_STUDENT_ID, and "
                        + "TEST_ENROLLMENT_COURSE_ID for enrollment fixture tests");
        return data;
    }

    private static EnrollmentTestDataConfiguration requireWriteFixtures() {
        EnrollmentTestDataConfiguration data = testData();
        Assumptions.assumeTrue(data.writeFixturesConfigured(),
                "Set TEST_ENROLLMENT_WRITE_ENABLED=true with controlled enrollment fixtures for write tests");
        return data;
    }

    private static Map<String, Object> validAdminRequest() {
        return Map.of("studentId", UUID.fromString(UNKNOWN_ID), "courseIds", List.of(UUID.fromString(UNKNOWN_ID)));
    }

    private static Map<String, Object> validAdminRequest(EnrollmentTestDataConfiguration data) {
        return Map.of("studentId", data.studentId(), "courseIds", List.of(data.courseId()));
    }

    private static Map<String, Object> validStudentRequest() {
        return Map.of("courseIds", List.of(UUID.fromString(UNKNOWN_ID)));
    }

    private static Map<String, Object> validUpdateRequest() {
        return Map.of("courseIds", List.of(UUID.fromString(UNKNOWN_ID)));
    }

    private static Map<String, Object> validUpdateRequest(EnrollmentTestDataConfiguration data) {
        return Map.of("courseIds", List.of(data.alternateCourseId()));
    }

    @FunctionalInterface
    private interface EnrollmentRequest {
        Response send(String bearerToken);
    }
}

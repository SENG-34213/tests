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
        assertEquals(401, enrollmentClient.delete(UNKNOWN_ID, null).statusCode());
        assertEquals(401, enrollmentClient.membership(UNKNOWN_ID, UNKNOWN_ID, null).statusCode());
        assertEquals(401, enrollmentClient.studentCourses(UNKNOWN_ID, null).statusCode());
        assertEquals(401, enrollmentClient.courseStudents(UNKNOWN_ID, null).statusCode());
        assertEquals(401, enrollmentClient.courseEnrollments(UNKNOWN_ID, null).statusCode());
        assertEquals(401, enrollmentClient.unenrolledStudents(null).statusCode());
    }

    @Test
    void shouldRejectStudentsAndTeachersFromManagingEnrollments() {
        assertStatusForRole(TestRole.STUDENT, token -> enrollmentClient.getActiveEnrollments(token), 403);
        assertStatusForRole(TestRole.TEACHER, token -> enrollmentClient.getActiveEnrollments(token), 403);
        assertStatusForRole(TestRole.STUDENT, token -> enrollmentClient.create(validAdminRequest(), token), 403);
        assertStatusForRole(TestRole.TEACHER,
                token -> enrollmentClient.selfEnroll(validStudentRequest(), token), 403);
        assertStatusForRole(TestRole.ADMIN,
                token -> enrollmentClient.selfEnroll(validStudentRequest(), token), 403);
        assertStatusForRole(TestRole.STUDENT, token -> enrollmentClient.update(UNKNOWN_ID, validUpdateRequest(), token), 403);
        assertStatusForRole(TestRole.STUDENT, token -> enrollmentClient.delete(UNKNOWN_ID, token), 403);
        assertStatusForRole(TestRole.TEACHER,
                token -> enrollmentClient.update(UNKNOWN_ID, validUpdateRequest(), token), 404);
        assertStatusForRole(TestRole.TEACHER, token -> enrollmentClient.delete(UNKNOWN_ID, token), 404);
    }

    @Test
    void shouldRejectStudentsFromTeacherAndAdminQueries() {
        assertStatusForRole(TestRole.STUDENT, token -> enrollmentClient.courseStudents(UNKNOWN_ID, token), 403);
        assertStatusForRole(TestRole.STUDENT, token -> enrollmentClient.courseEnrollments(UNKNOWN_ID, token), 403);
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
        for (TestRole role : List.of(TestRole.STUDENT, TestRole.TEACHER, TestRole.ADMIN, TestRole.SUPER_ADMIN)) {
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

            Response enrollments = enrollmentClient.courseEnrollments(data.courseId().toString(),
                    teacher.accessToken());
            assertEquals(200, enrollments.statusCode(), enrollments.asString());
            assertEquals("success", enrollments.jsonPath().getString("status"));
            assertNotNull(enrollments.jsonPath().getList("data"));
        } finally {
            logout(teacher);
        }
    }

    @Test
    void shouldAllowAdminToListActiveAndUnenrolledStudents() {
        for (TestRole role : List.of(TestRole.ADMIN, TestRole.SUPER_ADMIN)) {
            AuthenticationSession admin = login(role);
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
            assertEquals(400, enrollmentClient.delete("not-a-uuid", admin.accessToken()).statusCode());
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

            assertEquals(404, response.statusCode(), response.asString());
            Response deleted = enrollmentClient.delete(UNKNOWN_ID, admin.accessToken());
            assertEquals(404, deleted.statusCode(), deleted.asString());
        } finally {
            logout(admin);
        }
    }

    @Test
    void shouldDenyTeacherEnrollmentCreationForAnotherTeachersCourse() {
        EnrollmentTestDataConfiguration data = testData();
        Assumptions.assumeTrue(data.readFixturesConfigured() && data.unownedCourseId() != null,
                "Set TEST_ENROLLMENT_UNOWNED_COURSE_ID to a course owned by another teacher");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response denied = enrollmentClient.create(Map.of("studentId", data.studentId(),
                    "courseIds", List.of(data.unownedCourseId()), "status", "PENDING"),
                    teacher.accessToken());
            assertEquals(403, denied.statusCode(), denied.asString());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void shouldDenyTeacherUpdatingOrDeletingEnrollmentForAnotherTeachersCourse() {
        EnrollmentTestDataConfiguration data = testData();
        Assumptions.assumeTrue(data.enrollmentId() != null,
                "Set TEST_ENROLLMENT_ID to an active enrollment in another teacher's course");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response update = enrollmentClient.update(data.enrollmentId().toString(), Map.of("status", "REJECTED"),
                    teacher.accessToken());
            assertEquals(403, update.statusCode(), update.asString());
            Response delete = enrollmentClient.delete(data.enrollmentId().toString(), teacher.accessToken());
            assertEquals(403, delete.statusCode(), delete.asString());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void shouldAllowTeacherToManageEnrollmentForOwnedCourseWhenWritesAreEnabled() {
        EnrollmentTestDataConfiguration data = requireWriteFixtures();
        requireCommunicationService();
        AuthenticationSession teacher = login(TestRole.TEACHER);
        String enrollmentId = null;
        try {
            Response existing = enrollmentClient.membership(data.otherStudentId().toString(),
                    data.courseId().toString(), teacher.accessToken());
            assertEquals(200, existing.statusCode(), existing.asString());
            Assumptions.assumeFalse(existing.as(Boolean.class),
                    "Other student fixture is already enrolled in the teacher-owned test course");

            Response created = enrollmentClient.create(Map.of("studentId", data.otherStudentId(),
                    "courseIds", List.of(data.courseId()), "status", "PENDING"), teacher.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            enrollmentId = created.jsonPath().getString("data.id");
            assertNotNull(enrollmentId, created.asString());

            Response updated = enrollmentClient.update(enrollmentId, Map.of("status", "APPROVED"),
                    teacher.accessToken());
            assertEquals(200, updated.statusCode(), updated.asString());
            assertEquals("APPROVED", updated.jsonPath().getString("data.status"));

            assertEquals(200, enrollmentClient.delete(enrollmentId, teacher.accessToken()).statusCode());
            enrollmentId = null;
            assertFalse(enrollmentClient.membership(data.otherStudentId().toString(),
                    data.courseId().toString(), teacher.accessToken()).as(Boolean.class));
        } finally {
            try {
                if (enrollmentId != null) {
                    enrollmentClient.delete(enrollmentId, teacher.accessToken());
                }
            } finally {
                logout(teacher);
            }
        }
    }

    @Test
    void shouldSupportStudentSelfEnrollmentWhenWriteFixturesAreExplicitlyEnabled() {
        EnrollmentTestDataConfiguration data = testData();
        Assumptions.assumeTrue(data.selfEnrollmentFixturesConfigured(),
                "Set TEST_ENROLLMENT_WRITE_ENABLED=true with a verified free course fixture for student self-enrollment");
        requireCommunicationService();
        AuthenticationSession student = login(TestRole.STUDENT);
        String enrollmentId = null;
        try {
            Response existing = enrollmentClient.membership(data.studentId().toString(),
                    data.freeCourseId().toString(), student.accessToken());
            assertEquals(200, existing.statusCode(), existing.asString());
            Assumptions.assumeFalse(existing.as(Boolean.class),
                    "Student is already enrolled in TEST_ENROLLMENT_FREE_COURSE_ID");
            Response response = enrollmentClient.selfEnroll(
                    Map.of("courseIds", List.of(data.freeCourseId().toString())), student.accessToken());
            assertEquals(201, response.statusCode(), response.asString());
            assertEquals("PENDING", response.jsonPath().getString("data.status"));
            enrollmentId = response.jsonPath().getString("data.id");
            assertNotNull(enrollmentId, response.asString());
            assertEquals(data.studentId().toString(), response.jsonPath().getString("data.studentId"));
            assertTrue(response.jsonPath().getList("data.courseIds", String.class)
                    .contains(data.freeCourseId().toString()), response.asString());
            assertTrue(enrollmentClient.membership(data.studentId().toString(),
                    data.freeCourseId().toString(), student.accessToken()).as(Boolean.class));
        } finally {
            try {
                if (enrollmentId != null) {
                    AuthenticationSession admin = login(TestRole.ADMIN);
                    try {
                        assertEquals(200, enrollmentClient.delete(enrollmentId, admin.accessToken()).statusCode());
                    } finally {
                        logout(admin);
                    }
                }
            } finally {
                logout(student);
            }
        }
    }

    @Test
    void shouldSupportAdminEnrollmentManagementWhenWriteFixturesAreExplicitlyEnabled() {
        EnrollmentTestDataConfiguration data = requireWriteFixtures();
        requireCommunicationService();
        AuthenticationSession admin = login(TestRole.ADMIN);
        String enrollmentId = null;
        try {
            Response existing = enrollmentClient.membership(data.studentId().toString(),
                    data.alternateCourseId().toString(), admin.accessToken());
            assertEquals(200, existing.statusCode(), existing.asString());
            Assumptions.assumeFalse(existing.as(Boolean.class),
                    "Student is already enrolled in TEST_ENROLLMENT_ALTERNATE_COURSE_ID");
            Response secondExisting = enrollmentClient.membership(data.studentId().toString(),
                    data.secondAlternateCourseId().toString(), admin.accessToken());
            assertEquals(200, secondExisting.statusCode(), secondExisting.asString());
            Assumptions.assumeFalse(secondExisting.as(Boolean.class),
                    "Student is already enrolled in TEST_ENROLLMENT_SECOND_ALTERNATE_COURSE_ID");

            Response create = enrollmentClient.create(validAdminRequest(data), admin.accessToken());
            assertEquals(201, create.statusCode(), create.asString());
            enrollmentId = create.jsonPath().getString("data.id");
            assertNotNull(enrollmentId, create.asString());
            String createdEnrollmentId = enrollmentId;
            assertEquals("PENDING", create.jsonPath().getString("data.status"));
            assertEquals(data.studentId().toString(), create.jsonPath().getString("data.studentId"));
            assertTrue(create.jsonPath().getList("data.courseIds", String.class)
                    .contains(data.alternateCourseId().toString()), create.asString());

            Response listed = enrollmentClient.courseEnrollments(data.alternateCourseId().toString(),
                    admin.accessToken());
            assertEquals(200, listed.statusCode(), listed.asString());
            assertTrue(listed.jsonPath().getList("data.id", String.class).contains(enrollmentId), listed.asString());

            Response approved = enrollmentClient.update(enrollmentId, Map.of("courseIds",
                    List.of(data.secondAlternateCourseId().toString()), "status", "APPROVED"), admin.accessToken());
            assertEquals(200, approved.statusCode(), approved.asString());
            assertEquals("APPROVED", approved.jsonPath().getString("data.status"));
            assertTrue(approved.jsonPath().getList("data.courseIds", String.class)
                    .contains(data.secondAlternateCourseId().toString()), approved.asString());
            assertFalse(enrollmentClient.membership(data.studentId().toString(),
                    data.alternateCourseId().toString(), admin.accessToken()).as(Boolean.class));
            Response oldCourseEnrollments = enrollmentClient.courseEnrollments(
                    data.alternateCourseId().toString(), admin.accessToken());
            assertEquals(200, oldCourseEnrollments.statusCode(), oldCourseEnrollments.asString());
            assertFalse(oldCourseEnrollments.jsonPath().getList("data.id", String.class)
                    .contains(createdEnrollmentId), oldCourseEnrollments.asString());

            Response rejected = enrollmentClient.update(enrollmentId, Map.of("status", "REJECTED"),
                    admin.accessToken());
            assertEquals(200, rejected.statusCode(), rejected.asString());
            assertEquals("REJECTED", rejected.jsonPath().getString("data.status"));

            assertEquals(200, enrollmentClient.delete(enrollmentId, admin.accessToken()).statusCode());
            enrollmentId = null;
            assertFalse(enrollmentClient.membership(data.studentId().toString(),
                    data.alternateCourseId().toString(), admin.accessToken()).as(Boolean.class));
            assertFalse(enrollmentClient.membership(data.studentId().toString(),
                    data.secondAlternateCourseId().toString(), admin.accessToken()).as(Boolean.class));
            Response afterDelete = enrollmentClient.courseEnrollments(data.alternateCourseId().toString(),
                    admin.accessToken());
            assertEquals(200, afterDelete.statusCode(), afterDelete.asString());
            assertFalse(afterDelete.jsonPath().getList("data.id", String.class).contains(createdEnrollmentId));
        } finally {
            try {
                if (enrollmentId != null) {
                    enrollmentClient.delete(enrollmentId, admin.accessToken());
                }
            } finally {
                logout(admin);
            }
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
        return Map.of("studentId", data.studentId(), "courseIds", List.of(data.alternateCourseId()),
                "status", "PENDING");
    }

    private static Map<String, Object> validStudentRequest() {
        return Map.of("courseIds", List.of(UUID.fromString(UNKNOWN_ID)));
    }

    private static Map<String, Object> validUpdateRequest() {
        return Map.of("courseIds", List.of(UUID.fromString(UNKNOWN_ID)));
    }

    @FunctionalInterface
    private interface EnrollmentRequest {
        Response send(String bearerToken);
    }
}

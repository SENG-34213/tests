package com.mentify.tests.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class CourseAnnouncementsProgressIT extends CourseIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void shouldAllowStudentsToReadOwnAnnouncementsAndProgress() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            Response announcements = courseClient.myAnnouncements(null, 0, 20, student.accessToken());
            assertEquals(200, announcements.statusCode(), announcements.asString());
            assertEquals("success", announcements.jsonPath().getString("status"));
            assertNotNull(announcements.jsonPath().get("data"), announcements.asString());

            Response progress = courseClient.myProgress(student.accessToken());
            assertEquals(200, progress.statusCode(), progress.asString());
            assertEquals("success", progress.jsonPath().getString("status"));
            assertNotNull(progress.jsonPath().get("data"), progress.asString());
        } finally {
            logout(student);
        }
    }

    @Test
    void shouldRejectAnnouncementAndProgressOperationsForWrongRoles() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(403, courseClient.myAnnouncements(null, 0, 20, teacher.accessToken()).statusCode());
            assertEquals(403, courseClient.myProgress(teacher.accessToken()).statusCode());
            assertEquals(403, courseClient.createAnnouncement(UNKNOWN_ID, Map.of(), student.accessToken())
                    .statusCode());

            Response invalidCreate = courseClient.createAnnouncement(UNKNOWN_ID, Map.of(), teacher.accessToken());
            assertEquals(400, invalidCreate.statusCode(), invalidCreate.asString());
            assertEquals(400, courseClient.updateLessonProgress(UNKNOWN_ID, Map.of(), student.accessToken())
                    .statusCode());
            assertEquals(403, courseClient.updateLessonProgress(UNKNOWN_ID,
                    Map.of("completed", true), teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
            logout(student);
        }
    }

    @Test
    void shouldValidateAnnouncementPaginationBeforeCallingService() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertThrows(IllegalArgumentException.class,
                    () -> courseClient.myAnnouncements(null, -1, 20, student.accessToken()));
            assertThrows(IllegalArgumentException.class,
                    () -> courseClient.myAnnouncements(null, 0, 101, student.accessToken()));
        } finally {
            logout(student);
        }
    }

    @Test
    void shouldReadProgressForAnEnrolledStudentCourseFixture() {
        String fixtureValue = System.getenv("TEST_STUDENT_PROGRESS_COURSE_ID");
        Assumptions.assumeTrue(fixtureValue != null && !fixtureValue.isBlank(),
                "Set TEST_STUDENT_PROGRESS_COURSE_ID to a course enrolled by the student test account");
        UUID courseId = UUID.fromString(fixtureValue.trim());

        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            Response response = courseClient.courseProgress(courseId.toString(), student.accessToken());
            assertEquals(200, response.statusCode(), response.asString());
            assertEquals(courseId.toString(), response.jsonPath().getString("data.courseId"));
            assertNotNull(response.jsonPath().get("data.progressPercentage"), response.asString());
        } finally {
            logout(student);
        }
    }

    @Test
    void shouldCreateAnnouncementForApprovedTeacherCourseFixture() {
        String courseValue = System.getenv("TEST_TEACHER_PORTAL_COURSE_ID");
        Assumptions.assumeTrue(Boolean.parseBoolean(
                        System.getenv().getOrDefault("TEST_COURSE_ANNOUNCEMENT_WRITE_ENABLED", "false"))
                        && courseValue != null && !courseValue.isBlank(),
                "Set TEST_COURSE_ANNOUNCEMENT_WRITE_ENABLED=true and TEST_TEACHER_PORTAL_COURSE_ID; "
                        + "announcement records persist because the API has no delete route");
        UUID courseId = UUID.fromString(courseValue.trim());
        String title = "TEST-028 announcement " + UUID.randomUUID();

        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response created = courseClient.createAnnouncement(courseId.toString(),
                    Map.of("title", title, "message", "Synthetic integration-test announcement"),
                    teacher.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            assertEquals(courseId.toString(), created.jsonPath().getString("data.courseId"));
            assertEquals(title, created.jsonPath().getString("data.title"));
            assertNotNull(created.jsonPath().getString("data.id"), created.asString());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void shouldUpdateAndRestoreLessonProgressForApprovedStudentFixture() {
        String courseValue = System.getenv("TEST_STUDENT_PROGRESS_COURSE_ID");
        String lessonValue = System.getenv("TEST_STUDENT_PROGRESS_LESSON_ID");
        Assumptions.assumeTrue(Boolean.parseBoolean(
                        System.getenv().getOrDefault("TEST_STUDENT_PROGRESS_WRITE_ENABLED", "false"))
                        && courseValue != null && !courseValue.isBlank()
                        && lessonValue != null && !lessonValue.isBlank(),
                "Set TEST_STUDENT_PROGRESS_WRITE_ENABLED=true and enrolled course/lesson fixture IDs");
        UUID courseId = UUID.fromString(courseValue.trim());
        UUID lessonId = UUID.fromString(lessonValue.trim());

        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            Response previousProgress = courseClient.myProgress(student.accessToken());
            assertEquals(200, previousProgress.statusCode(), previousProgress.asString());
            java.util.List<java.util.Map<String, Object>> rows = previousProgress.jsonPath()
                    .getList("data");
            Boolean previousValue = rows.stream()
                    .filter(row -> lessonId.toString().equals(String.valueOf(row.get("lessonId"))))
                    .map(row -> Boolean.valueOf(String.valueOf(row.get("completed"))))
                    .findFirst().orElse(null);
            try {
                Response updated = courseClient.updateLessonProgress(lessonId.toString(),
                        Map.of("completed", true), student.accessToken());
                assertEquals(200, updated.statusCode(), updated.asString());
                assertEquals(lessonId.toString(), updated.jsonPath().getString("data.lessonId"));
                assertEquals(courseId.toString(), updated.jsonPath().getString("data.courseId"));
                assertTrue(updated.jsonPath().getBoolean("data.completed"));

                Response courseProgress = courseClient.courseProgress(courseId.toString(), student.accessToken());
                assertEquals(200, courseProgress.statusCode(), courseProgress.asString());
                assertNotNull(courseProgress.jsonPath().get("data.progressPercentage"), courseProgress.asString());
            } finally {
                assertEquals(200, courseClient.updateLessonProgress(lessonId.toString(),
                        Map.of("completed", previousValue != null && previousValue), student.accessToken())
                        .statusCode());
            }
        } finally {
            logout(student);
        }
    }
}

package com.mentify.tests.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.RequiredService;
import io.restassured.response.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CompleteWorkflowIT extends EndToEndIntegrationTestSupport {

    @Test
    void studentTeacherAndAdminCompleteCrossServiceWorkflowsThroughGateway() throws IOException {
        EndToEndTestConfiguration config = EndToEndTestConfiguration.fromEnvironment();
        assumeTrue(config.writeEnabled() && config.courseGradeId() != null
                        && !config.courseGradeId().isBlank(),
                "Set TEST_E2E_WRITE_ENABLED=true and TEST_E2E_COURSE_GRADE_ID to enable isolated write journeys");
        requireService(RequiredService.COURSE_SERVICE);
        requireService(RequiredService.ENROLLMENT_SERVICE);
        requireService(RequiredService.ASSIGNMENT_SERVICE);
        requireService(RequiredService.MATERIAL_SERVICE);
        requireService(RequiredService.PAYMENT_SERVICE);
        requireService(RequiredService.NOTIFICATION_SERVICE);

        AuthenticationSession teacher = login(TestRole.TEACHER);
        Path materialFile = Files.createTempFile("mentify-e2e-material-", ".txt");
        Files.writeString(materialFile, "Synthetic course material " + UUID.randomUUID(), StandardCharsets.UTF_8);
        AuthenticationSession student = null;
        AuthenticationSession admin = null;
        String courseId = null;
        String enrollmentId = null;
        String moduleId = null;
        String lessonId = null;
        String materialReferenceId = null;
        String materialId = null;
        String assignmentId = null;
        String quizId = null;
        try {
            String teacherId = currentUserId(teacher.accessToken());
            Response createdCourse = courses.create(courseRequest(config.courseGradeId(), teacherId),
                    teacher.accessToken());
            assertEquals(201, createdCourse.statusCode(), createdCourse.asString());
            courseId = responseId(createdCourse);
            assertEquals(teacherId, createdCourse.jsonPath().getString("data.assignedTeacherId"));

            Response createdModule = courses.createModule(courseId, moduleRequest(), teacher.accessToken());
            assertEquals(201, createdModule.statusCode(), createdModule.asString());
            moduleId = responseId(createdModule);
            Response createdLesson = courses.createLesson(courseId, moduleId, lessonRequest(),
                    teacher.accessToken());
            assertEquals(201, createdLesson.statusCode(), createdLesson.asString());
            lessonId = responseId(createdLesson);

            Response createdMaterial = courses.uploadLessonMaterial(courseId, moduleId, lessonId,
                    "TEST-E2E material " + UUID.randomUUID(), "Synthetic E2E lesson material",
                    materialFile.toFile(), teacher.accessToken());
            assertEquals(201, createdMaterial.statusCode(), createdMaterial.asString());
            materialReferenceId = responseId(createdMaterial);
            materialId = createdMaterial.jsonPath().getString("data.materialId");
            assertNotNull(materialId, createdMaterial.asString());
            Response downloadedMaterial = materials.download(materialId, teacher.accessToken());
            assertEquals(200, downloadedMaterial.statusCode(), downloadedMaterial.asString());
            assertNotNull(downloadedMaterial.jsonPath().getString("data.downloadUrl"),
                    downloadedMaterial.asString());

            Response createdAssignment = assignments.create(assignmentRequest(courseId, moduleId),
                    teacher.accessToken());
            assertEquals(201, createdAssignment.statusCode(), createdAssignment.asString());
            assignmentId = responseId(createdAssignment);
            assertEquals(200, assignments.publish(assignmentId, teacher.accessToken()).statusCode());

            Response createdQuiz = quizzes.create(quizRequest(courseId), teacher.accessToken());
            assertEquals(201, createdQuiz.statusCode(), createdQuiz.asString());
            quizId = responseId(createdQuiz);
            Response createdQuestion = quizQuestions.create(quizId, questionRequest(), teacher.accessToken());
            assertEquals(201, createdQuestion.statusCode(), createdQuestion.asString());
            String questionId = responseId(createdQuestion);
            String correctOptionId = createdQuestion.jsonPath().getString("data.options[0].id");
            assertNotNull(correctOptionId, createdQuestion.asString());
            assertEquals(200, quizzes.publish(quizId, teacher.accessToken()).statusCode());

            student = login(TestRole.STUDENT);
            String studentId = currentUserId(student.accessToken());
            Response browsedCourse = courses.lookup(courseId, student.accessToken());
            assertEquals(200, browsedCourse.statusCode(), browsedCourse.asString());
            Response enrollment = enrollments.selfEnroll(Map.of("courseIds", List.of(courseId)),
                    student.accessToken());
            assertEquals(201, enrollment.statusCode(), enrollment.asString());
            enrollmentId = responseId(enrollment);
            assertEquals("PENDING", enrollment.jsonPath().getString("data.status"));
            assertTrue(enrollments.membership(studentId, courseId, student.accessToken()).as(Boolean.class));

            Response approved = enrollments.update(enrollmentId, Map.of("status", "APPROVED"),
                    teacher.accessToken());
            assertEquals(200, approved.statusCode(), approved.asString());
            assertEquals("APPROVED", approved.jsonPath().getString("data.status"));

            Response studentCourse = courses.lookup(courseId, student.accessToken());
            assertEquals(200, studentCourse.statusCode(), studentCourse.asString());
            Response studentLesson = courses.lesson(courseId, moduleId, lessonId, student.accessToken());
            assertEquals(200, studentLesson.statusCode(), studentLesson.asString());
            Response studentMaterials = courses.lessonMaterials(courseId, moduleId, lessonId,
                    student.accessToken());
            assertEquals(200, studentMaterials.statusCode(), studentMaterials.asString());
            assertTrue(studentMaterials.jsonPath().getList("data.id", String.class).contains(materialReferenceId),
                    studentMaterials.asString());
            Response studentMaterialDownload = materials.download(materialId, student.accessToken());
            assertEquals(200, studentMaterialDownload.statusCode(), studentMaterialDownload.asString());
            assertNotNull(studentMaterialDownload.jsonPath().getString("data.downloadUrl"),
                    studentMaterialDownload.asString());
            Response courseAssignments = assignments.listForCourse(courseId, student.accessToken());
            assertEquals(200, courseAssignments.statusCode(), courseAssignments.asString());
            assertTrue(courseAssignments.jsonPath().getList("data.id", String.class).contains(assignmentId),
                    courseAssignments.asString());

            Response draft = submissions.create(assignmentId,
                    Map.of("content", "Synthetic end-to-end assignment response"), student.accessToken());
            assertEquals(201, draft.statusCode(), draft.asString());
            String submissionId = responseId(draft);
            Response submitted = submissions.submit(submissionId, student.accessToken());
            assertEquals(200, submitted.statusCode(), submitted.asString());
            assertEquals("SUBMITTED", submitted.jsonPath().getString("data.status"));
            Response graded = submissions.grade(submissionId,
                    Map.of("marks", BigDecimal.valueOf(85), "feedback", "End-to-end grade"),
                    teacher.accessToken());
            assertEquals(200, graded.statusCode(), graded.asString());
            assertEquals("85", graded.jsonPath().getString("data.marks"));

            Response studentQuiz = quizzes.getStudent(quizId, student.accessToken());
            assertEquals(200, studentQuiz.statusCode(), studentQuiz.asString());
            Response attempt = quizAttempts.start(quizId, student.accessToken());
            assertEquals(201, attempt.statusCode(), attempt.asString());
            String attemptId = attempt.jsonPath().getString("data.attemptId");
            assertNotNull(attemptId, attempt.asString());
            assertEquals(200, quizAttempts.saveAnswer(attemptId,
                    Map.of("questionId", questionId, "selectedOptionId", correctOptionId),
                    student.accessToken()).statusCode());
            Response result = quizAttempts.submit(attemptId, student.accessToken());
            assertEquals(200, result.statusCode(), result.asString());
            assertTrue(result.jsonPath().getDouble("data.score") > 0, result.asString());
            assertTrue(result.jsonPath().getBoolean("data.passed"), result.asString());
            assertEquals(200, quizzes.results(quizId, teacher.accessToken()).statusCode());

            admin = login(TestRole.ADMIN);
            assertEquals(200, users.adminUsers("STUDENT", admin.accessToken()).statusCode());
            Response adminCourses = courses.getAllCourses(admin.accessToken());
            assertEquals(200, adminCourses.statusCode(), adminCourses.asString());
            assertTrue(adminCourses.jsonPath().getList("data.id", String.class).contains(courseId),
                    adminCourses.asString());
            assertEquals(200, payments.adminPayments(null, null, null, null, null, null,
                    0, 10, admin.accessToken()).statusCode());
            assertEquals(200, notifications.list(0, 10, false, admin.accessToken()).statusCode());
        } finally {
            if (student != null && enrollmentId != null) {
                enrollments.delete(enrollmentId, teacher.accessToken());
            }
            if (assignmentId != null) {
                assignments.delete(assignmentId, teacher.accessToken());
            }
            if (quizId != null) {
                quizzes.delete(quizId, teacher.accessToken());
            }
            if (courseId != null && moduleId != null && materialReferenceId != null) {
                courses.deleteLearningMaterial(courseId, moduleId, materialReferenceId, teacher.accessToken());
            }
            if (materialId != null) {
                materials.delete(materialId, teacher.accessToken());
            }
            if (courseId != null && moduleId != null && lessonId != null) {
                courses.deleteLesson(courseId, moduleId, lessonId, teacher.accessToken());
            }
            if (courseId != null && moduleId != null) {
                courses.deleteModule(courseId, moduleId, teacher.accessToken());
            }
            if (courseId != null) {
                admin = admin == null ? login(TestRole.ADMIN) : admin;
                courses.delete(courseId, admin.accessToken());
            }
            logout(admin);
            logout(student);
            logout(teacher);
            Files.deleteIfExists(materialFile);
        }
    }

    private static String currentUserId(String token) {
        Response response = users.currentUser(token);
        assertEquals(200, response.statusCode(), response.asString());
        return responseId(response);
    }

    private static String responseId(Response response) {
        String id = response.jsonPath().getString("data.id");
        assertNotNull(id, response.asString());
        return id;
    }

    private static Map<String, Object> courseRequest(String gradeId, String teacherId) {
        return Map.of("courseName", "TEST-E2E " + UUID.randomUUID(),
                "courseDescription", "Disposable course created by the opt-in E2E workflow",
                "courseFeeMonthly", 0.0,
                "gradeId", gradeId,
                "assignedTeacherId", teacherId,
                "subject", "Integration Testing",
                "isOnline", true,
                "discountOfferPercent", 0.0,
                "isVisible", true,
                "isPublished", true);
    }

    private static Map<String, Object> moduleRequest() {
        return Map.of("title", "TEST-E2E module " + UUID.randomUUID(),
                "description", "Disposable end-to-end module",
                "sequenceOrder", 1,
                "startDate", LocalDate.now().toString(),
                "endDate", LocalDate.now().plusDays(7).toString(),
                "isVisible", true);
    }

    private static Map<String, Object> lessonRequest() {
        return Map.of("title", "TEST-E2E lesson " + UUID.randomUUID(),
                "description", "Disposable end-to-end lesson",
                "durationMinutes", 10,
                "contentMarkdown", "Synthetic lesson content for the end-to-end journey.",
                "contentType", "MARKDOWN",
                "isVisible", true);
    }

    private static Map<String, Object> assignmentRequest(String courseId, String moduleId) {
        LocalDateTime start = LocalDateTime.now().minusMinutes(1).withNano(0);
        return Map.ofEntries(
                Map.entry("courseId", courseId),
                Map.entry("moduleId", moduleId),
                Map.entry("title", "TEST-E2E assignment " + UUID.randomUUID()),
                Map.entry("description", "Disposable assignment for the end-to-end journey"),
                Map.entry("instructions", "Submit a short synthetic response."),
                Map.entry("startDate", start.toString()),
                Map.entry("dueDate", start.plusDays(2).toString()),
                Map.entry("maxMarks", 100),
                Map.entry("allowedAttempts", 1),
                Map.entry("allowLateSubmission", false),
                Map.entry("latePenaltyPercentage", 0),
                Map.entry("rubricItems", List.of(Map.of("criterion", "Completion", "marks", 100))));
    }

    private static Map<String, Object> quizRequest(String courseId) {
        LocalDateTime now = LocalDateTime.now();
        return Map.of("courseId", courseId,
                "title", "TEST-E2E quiz " + UUID.randomUUID(),
                "description", "Disposable quiz for the end-to-end journey",
                "durationMinutes", 15,
                "passMark", BigDecimal.valueOf(5),
                "startTime", now.minusMinutes(1).withNano(0).toString(),
                "endTime", now.plusDays(2).withNano(0).toString(),
                "maxAttempts", 1,
                "showResultImmediately", true,
                "creationMethod", "MANUAL");
    }

    private static Map<String, Object> questionRequest() {
        return Map.of("questionText", "Which option is correct?",
                "questionType", "MULTIPLE_CHOICE_SINGLE_ANSWER",
                "marks", BigDecimal.TEN,
                "questionOrder", 1,
                "options", List.of(
                        Map.of("optionText", "Correct", "correct", true, "optionOrder", 1),
                        Map.of("optionText", "Incorrect", "correct", false, "optionOrder", 2)));
    }
}

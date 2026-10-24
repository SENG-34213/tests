package com.mentify.tests.exam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import com.mentify.tests.auth.AuthenticationClient;
import com.mentify.tests.auth.AuthenticationConfiguration;
import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import com.mentify.tests.material.MaterialClient;
import com.mentify.tests.material.MaterialDownloadVerifier;
import io.restassured.response.Response;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ExamLifecycleIT {

    private static ExamClient exams;
    private static MaterialClient materials;
    private static AuthenticationClient authentication;

    @BeforeAll
    static void requireExamEnvironment() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        BackendReadinessClient readiness = new BackendReadinessClient(environment);
        readiness.awaitRequiredServices();
        readiness.awaitService(RequiredService.EXAM_SERVICE);
        readiness.awaitService(RequiredService.COURSE_SERVICE);
        readiness.awaitService(RequiredService.ENROLLMENT_SERVICE);
        readiness.awaitService(RequiredService.MATERIAL_SERVICE);
        ApiClient api = new ApiClient(ApiClientConfiguration.fromEnvironment(environment, false));
        authentication = new AuthenticationClient(api, AuthenticationConfiguration.fromEnvironment());
        exams = new ExamClient(api);
        materials = new MaterialClient(api);
    }

    @Test
    void teacherCanRunTheExamLifecycleAndReadResults() throws Exception {
        ExamTestData data = ExamTestData.fromEnvironment();
        assumeTrue(data.writeEnabled() && data.fixturesConfigured(),
                "Set TEST_EXAM_WRITE_ENABLED=true and approved exam fixtures to run write tests");
        AuthenticationSession teacher = authentication.login(TestRole.TEACHER);
        try {
            String token = teacher.accessToken();
            Response created = exams.create(data.createRequest(), token);
            assertEquals(201, created.statusCode());
            String examId = created.jsonPath().getString("data.id");
            assertTrue(examId != null && !examId.isBlank());
            verifyAttachmentLifecycle(examId, token);
            assertEquals(200, exams.get(examId, token).statusCode());
            assertEquals(200, exams.listByCourse(data.courseId().toString(), token).statusCode());
            Response updated = exams.update(examId, updateRequestWithCourseChange(), token);
            assertEquals(200, updated.statusCode(), updated.asString());
            assertEquals(data.courseId().toString(), updated.jsonPath().getString("data.courseId"), updated.asString());
            assertEquals(200, exams.schedule(examId, token).statusCode());
            AuthenticationSession student = authentication.login(TestRole.STUDENT);
            try {
                Response studentExam = exams.studentExam(examId, student.accessToken());
                assertEquals(200, studentExam.statusCode(), studentExam.asString());
                assertEquals(examId, studentExam.jsonPath().getString("data.id"));
                assertEquals("SCHEDULED", studentExam.jsonPath().getString("data.status"));
                verifyStudentAttachmentVisibility(examId, token, student.accessToken());
            } finally {
                logout(student);
            }
            assertEquals(200, exams.startMarking(examId, token).statusCode());
            assertEquals(200, exams.enterMarks(examId, data.marksRequest(), token).statusCode());
            assertEquals(200, exams.complete(examId, token).statusCode());
            Response resultSheet = exams.results(examId, token);
            assertEquals(200, resultSheet.statusCode(), resultSheet.asString());
            assertEquals(examId, resultSheet.jsonPath().getString("data.examId"), resultSheet.asString());
            assertEquals("COMPLETED", resultSheet.jsonPath().getString("data.status"), resultSheet.asString());
            assertTrue(resultSheet.jsonPath().getList("data.results.studentId", String.class)
                    .contains(data.studentId().toString()), resultSheet.asString());
            Response summary = exams.summary(examId, token);
            assertEquals(200, summary.statusCode(), summary.asString());
            assertEquals(examId, summary.jsonPath().getString("data.examId"), summary.asString());
            assertTrue(summary.jsonPath().getLong("data.totalStudents") >= 1, summary.asString());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void teacherCanCreateAndUpdateExamUsingMultipartInstructionRoutes() {
        ExamTestData data = ExamTestData.fromEnvironment();
        org.junit.jupiter.api.Assumptions.assumeTrue(data.writeEnabled() && data.fixturesConfigured(),
                "Set TEST_EXAM_WRITE_ENABLED=true and approved exam fixtures to run write tests");
        AuthenticationSession teacher = authentication.login(TestRole.TEACHER);
        String examId = null;
        try {
            Response created = exams.createWithInstructions(data.createRequest(), false, teacher.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            examId = created.jsonPath().getString("data.id");
            assertTrue(examId != null && !examId.isBlank(), created.asString());

            Response updated = exams.updateWithInstructions(examId, data.updateRequest(), false,
                    teacher.accessToken());
            assertEquals(200, updated.statusCode(), updated.asString());
            assertEquals("Updated TEST-017 exam", updated.jsonPath().getString("data.title"));
        } finally {
            if (examId != null) exams.cancel(examId, teacher.accessToken());
            logout(teacher);
        }
    }

    @Test
    void teacherCanCancelAnEligibleExam() {
        ExamTestData data = ExamTestData.fromEnvironment();
        assumeTrue(data.writeEnabled() && data.fixturesConfigured(),
                "Set TEST_EXAM_WRITE_ENABLED=true and approved exam fixtures to run write tests");
        AuthenticationSession teacher = authentication.login(TestRole.TEACHER);
        String examId = null;
        try {
            Response created = exams.create(data.createRequest(), teacher.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            examId = created.jsonPath().getString("data.id");
            assertTrue(examId != null && !examId.isBlank(), created.asString());

            Response cancelled = exams.cancel(examId, teacher.accessToken());
            assertEquals(200, cancelled.statusCode(), cancelled.asString());
            assertEquals("CANCELLED", cancelled.jsonPath().getString("data.status"), cancelled.asString());
            examId = null;
        } finally {
            if (examId != null) {
                exams.cancel(examId, teacher.accessToken());
            }
            logout(teacher);
        }
    }

    private static void verifyAttachmentLifecycle(String examId, String token) throws Exception {
        Path unsupported = Files.createTempFile("mentify-test-exam-invalid-", ".txt");
        Path attachment = Files.createTempFile("mentify-test-exam-", ".pdf");
        String content = "%PDF-1.4\n% Synthetic exam attachment " + UUID.randomUUID() + "\n%%EOF\n";
        String attachmentId = null;
        String materialId = null;
        try {
            Files.writeString(unsupported, "Not a PDF", StandardCharsets.UTF_8);
            Response rejected = exams.uploadAttachment(examId, unsupported.toFile(), true, token);
            assertEquals(400, rejected.statusCode(), rejected.asString());

            Files.writeString(attachment, content, StandardCharsets.UTF_8);
            Response uploaded = exams.uploadAttachment(examId, attachment.toFile(), true, token);
            assertEquals(201, uploaded.statusCode());
            attachmentId = uploaded.jsonPath().getString("data.id");
            materialId = uploaded.jsonPath().getString("data.materialId");
            assertTrue(attachmentId != null && !attachmentId.isBlank());
            assertTrue(materialId != null && !materialId.isBlank());
            assertEquals(true, uploaded.jsonPath().getBoolean("data.studentVisible"));
            assertEquals(content.length(), uploaded.jsonPath().getLong("data.fileSize"));

            Response listed = exams.attachments(examId, token);
            assertEquals(200, listed.statusCode());
            assertTrue(listed.jsonPath().getList("data*.id", String.class).contains(attachmentId));

            Response downloaded = exams.downloadAttachment(examId, attachmentId, token);
            assertEquals(200, downloaded.statusCode());
            assertEquals(attachmentId, downloaded.jsonPath().getString("data.id"));
            assertEquals(content.length(), downloaded.jsonPath().getLong("data.fileSize"));
            assertNotNull(downloaded.jsonPath().getString("data.downloadUrl"), downloaded.asString());
            MaterialDownloadVerifier.verifyIfEnabled(
                    downloaded.jsonPath().getString("data.downloadUrl"), attachment);
        } finally {
            try {
                if (attachmentId != null) {
                    assertEquals(200, exams.deleteAttachment(examId, attachmentId, token).statusCode());
                }
            } finally {
                try {
                    if (materialId != null) {
                        assertEquals(200, materials.delete(materialId, token).statusCode());
                    }
                } finally {
                    Files.deleteIfExists(unsupported);
                    Files.deleteIfExists(attachment);
                }
            }
        }
    }

    private static void verifyStudentAttachmentVisibility(String examId, String teacherToken,
                                                          String studentToken) throws Exception {
        verifyStudentAttachmentAccess(examId, true, teacherToken, studentToken);
        verifyStudentAttachmentAccess(examId, false, teacherToken, studentToken);
    }

    private static void verifyStudentAttachmentAccess(String examId, boolean studentVisible,
                                                       String teacherToken, String studentToken) throws Exception {
        Path attachment = Files.createTempFile("mentify-test-exam-visibility-", ".pdf");
        String content = "%PDF-1.4\n% Visibility probe " + UUID.randomUUID() + "\n%%EOF\n";
        String attachmentId = null;
        String materialId = null;
        try {
            Files.writeString(attachment, content, StandardCharsets.UTF_8);
            Response uploaded = exams.uploadAttachment(examId, attachment.toFile(), studentVisible, teacherToken);
            assertEquals(201, uploaded.statusCode(), uploaded.asString());
            attachmentId = uploaded.jsonPath().getString("data.id");
            materialId = uploaded.jsonPath().getString("data.materialId");
            assertTrue(attachmentId != null && !attachmentId.isBlank(), uploaded.asString());
            assertTrue(materialId != null && !materialId.isBlank(), uploaded.asString());

            Response studentList = exams.attachments(examId, studentToken);
            assertEquals(200, studentList.statusCode(), studentList.asString());
            boolean listed = studentList.jsonPath().getList("data*.id", String.class).contains(attachmentId);
            assertEquals(studentVisible, listed, studentList.asString());

            Response studentDownload = exams.downloadAttachment(examId, attachmentId, studentToken);
            assertEquals(studentVisible ? 200 : 403, studentDownload.statusCode(), studentDownload.asString());
            if (studentVisible) {
                assertNotNull(studentDownload.jsonPath().getString("data.downloadUrl"), studentDownload.asString());
                MaterialDownloadVerifier.verifyIfEnabled(
                        studentDownload.jsonPath().getString("data.downloadUrl"), attachment);
            }
        } finally {
            try {
                if (attachmentId != null) {
                    assertEquals(200, exams.deleteAttachment(examId, attachmentId, teacherToken).statusCode());
                }
            } finally {
                try {
                    if (materialId != null) {
                        assertEquals(200, materials.delete(materialId, teacherToken).statusCode());
                    }
                } finally {
                    Files.deleteIfExists(attachment);
                }
            }
        }
    }

    private static Map<String, Object> updateRequestWithCourseChange() {
        return Map.of(
                "title", "Updated TEST-017 exam",
                "description", "Updated integration fixture",
                "examDate", java.time.LocalDate.now().plusDays(31).toString(),
                "startTime", "10:00:00",
                "endTime", "12:00:00",
                "totalMarks", 100,
                "passMarks", 50,
                "type", "OTHER",
                "courseId", UUID.randomUUID().toString());
    }

    private static void logout(AuthenticationSession session) {
        if (session != null && session.refreshable()) {
            authentication.logout(session);
        } else if (session != null) {
            session.clear();
        }
    }
}

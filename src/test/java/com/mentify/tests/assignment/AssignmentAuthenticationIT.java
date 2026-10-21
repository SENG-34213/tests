package com.mentify.tests.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AssignmentAuthenticationIT extends AssignmentIntegrationTestSupport {

    @Test
    void rejectsProtectedAssignmentAndSubmissionRequestsWithoutAuthentication() {
        assertEquals(401, assignmentClient.create(Map.of(), null).statusCode());
        assertEquals(401, assignmentClient.update("00000000-0000-0000-0000-000000000000", Map.of(), null).statusCode());
        assertEquals(401, assignmentClient.patch("00000000-0000-0000-0000-000000000000", Map.of(), null).statusCode());
        assertEquals(401, assignmentClient.get("00000000-0000-0000-0000-000000000000", null).statusCode());
        assertEquals(401, assignmentClient.listForCourse(
                "00000000-0000-0000-0000-000000000000", null).statusCode());
        assertEquals(401, assignmentClient.delete(
                "00000000-0000-0000-0000-000000000000", null).statusCode());
        assertEquals(401, assignmentClient.publish("00000000-0000-0000-0000-000000000000", null).statusCode());
        assertEquals(401, assignmentClient.close("00000000-0000-0000-0000-000000000000", null).statusCode());
        assertEquals(401, assignmentClient.myAssignments(null, Map.of()).statusCode());
        assertEquals(401, submissionClient.create(
                "00000000-0000-0000-0000-000000000000", Map.of(), null).statusCode());
        assertEquals(401, submissionClient.listMine(null).statusCode());
        assertEquals(401, submissionClient.update("00000000-0000-0000-0000-000000000000", Map.of(), null).statusCode());
        assertEquals(401, submissionClient.submit("00000000-0000-0000-0000-000000000000", null).statusCode());
        assertEquals(401, submissionClient.get("00000000-0000-0000-0000-000000000000", null).statusCode());
        assertEquals(401, submissionClient.listForAssignment(
                "00000000-0000-0000-0000-000000000000", null).statusCode());
        assertEquals(401, submissionClient.grade(
                "00000000-0000-0000-0000-000000000000", Map.of(), null).statusCode());
        assertEquals(401, submissionClient.returnSubmission("00000000-0000-0000-0000-000000000000", null).statusCode());
    }

    @Test
    void rejectsInvalidAssignmentPayloadBeforeCreation() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response response = assignmentClient.create(Map.of(), teacher.accessToken());

            assertEquals(400, response.statusCode(), response.asString());
            Response emptyPatch = assignmentClient.patch(
                    "00000000-0000-0000-0000-000000000000", Map.of(), teacher.accessToken());
            assertEquals(400, emptyPatch.statusCode(), emptyPatch.asString());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void rejectsAssignmentAttemptPenaltyAndRubricValidationBoundaries() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Map<String, Object> request = validAssignmentPayload();
            request.put("allowedAttempts", 0);
            assertEquals(400, assignmentClient.create(request, teacher.accessToken()).statusCode());

            request = validAssignmentPayload();
            request.put("latePenaltyPercentage", 101);
            assertEquals(400, assignmentClient.create(request, teacher.accessToken()).statusCode());

            request = validAssignmentPayload();
            request.put("rubricItems", List.of(Map.of("criterion", " ", "marks", 1)));
            assertEquals(400, assignmentClient.create(request, teacher.accessToken()).statusCode());

            request = validAssignmentPayload();
            List<Map<String, Object>> rubricItems = new ArrayList<>();
            for (int index = 0; index < 51; index++) {
                rubricItems.add(Map.of("criterion", "Criterion " + index, "marks", 0));
            }
            request.put("rubricItems", rubricItems);
            Response tooManyRubricItems = assignmentClient.create(request, teacher.accessToken());
            assertEquals(400, tooManyRubricItems.statusCode(), tooManyRubricItems.asString());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void rejectsMissingNegativeAndOverMaximumGradeMarksBeforeLookup() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            String unknownSubmissionId = "00000000-0000-0000-0000-000000000000";
            assertEquals(400, submissionClient.grade(unknownSubmissionId, Map.of(),
                    teacher.accessToken()).statusCode());
            assertEquals(400, submissionClient.grade(unknownSubmissionId,
                    Map.of("marks", new BigDecimal("-0.01")), teacher.accessToken()).statusCode());
            Response tooManyMarks = submissionClient.grade(unknownSubmissionId,
                    Map.of("marks", new BigDecimal("100.01")), teacher.accessToken());
            assertEquals(400, tooManyMarks.statusCode(), tooManyMarks.asString());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void rejectsStudentAssignmentManagement() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(403, assignmentClient.create(Map.of(), student.accessToken()).statusCode());
            assertEquals(403, assignmentClient.patch("00000000-0000-0000-0000-000000000000",
                    Map.of("title", "blocked"), student.accessToken()).statusCode());
            assertEquals(200, assignmentClient.myAssignments(student.accessToken(), Map.of()).statusCode());
        } finally {
            logout(student);
        }
    }

    @Test
    void rejectsTeacherSubmissionCreation() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(403, submissionClient.create(
                    "00000000-0000-0000-0000-000000000000", Map.of(),
                    teacher.accessToken()).statusCode());
            assertEquals(403, assignmentClient.myAssignments(teacher.accessToken(), Map.of()).statusCode());
        } finally {
            logout(teacher);
        }
    }

    private static Map<String, Object> validAssignmentPayload() {
        LocalDateTime start = LocalDateTime.now().plusHours(2).withNano(0);
        return new LinkedHashMap<>(Map.of(
                "courseId", UUID.randomUUID().toString(),
                "moduleId", UUID.randomUUID().toString(),
                "title", "Validation-only assignment",
                "startDate", start.toString(),
                "dueDate", start.plusDays(1).toString(),
                "maxMarks", 100,
                "allowedAttempts", 1,
                "allowLateSubmission", false,
                "latePenaltyPercentage", 0));
    }
}

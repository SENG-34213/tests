package com.mentify.tests.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class AssignmentWorkflowIT extends AssignmentIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void teacherCanCreateReadListUpdatePublishCloseAndDeleteAssignment() {
        AssignmentTestDataConfiguration data = requireWriteFixtures();
        AuthenticationSession teacher = login(TestRole.TEACHER);
        String assignmentId = null;
        try {
            Response created = assignmentClient.create(validAssignmentRequest(data, "TEST-011"),
                    teacher.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            assertEquals("success", created.jsonPath().getString("status"));
            assignmentId = created.jsonPath().getString("data.id");
            assertNotNull(assignmentId, created.asString());
            assertEquals("TEST-011", created.jsonPath().getString("data.title"));
            assertEquals(data.courseId().toString(), created.jsonPath().getString("data.courseId"));
            assertEquals(2, created.jsonPath().getList("data.rubricItems").size());
            assertEquals("Correctness", created.jsonPath().getString("data.rubricItems[0].criterion"));
            assertEquals(60, created.jsonPath().getInt("data.rubricItems[0].marks"));
            assertEquals("Explanation", created.jsonPath().getString("data.rubricItems[1].criterion"));
            assertEquals(40, created.jsonPath().getInt("data.rubricItems[1].marks"));

            Response read = assignmentClient.get(assignmentId, teacher.accessToken());
            assertEquals(200, read.statusCode(), read.asString());
            assertEquals(assignmentId, read.jsonPath().getString("data.id"));

            Response listed = assignmentClient.listForCourse(data.courseId().toString(), teacher.accessToken());
            assertEquals(200, listed.statusCode(), listed.asString());
            assertTrue(listed.jsonPath().getList("data.id").contains(assignmentId), listed.asString());

            Response updated = assignmentClient.update(assignmentId,
                    validUpdateRequest("TEST-011 Updated"), teacher.accessToken());
            assertEquals(200, updated.statusCode(), updated.asString());
            assertEquals("TEST-011 Updated", updated.jsonPath().getString("data.title"));
            assertEquals(2, updated.jsonPath().getList("data.rubricItems").size(),
                    "Full update must preserve rubric when rubricItems is omitted");

            Response patched = assignmentClient.patch(assignmentId,
                    Map.of("title", "TEST-011 Patched"), teacher.accessToken());
            assertEquals(200, patched.statusCode(), patched.asString());
            assertEquals("TEST-011 Patched", patched.jsonPath().getString("data.title"));
            assertEquals("Updated synthetic TEST-011 assignment", patched.jsonPath().getString("data.description"),
                    "PATCH must preserve fields omitted from its request");
            assertEquals(100, patched.jsonPath().getInt("data.maxMarks"),
                    "PATCH must preserve omitted numeric fields");
            assertEquals("Correctness", patched.jsonPath().getString("data.rubricItems[0].criterion"),
                    "PATCH must preserve omitted rubric items");

            Response published = assignmentClient.publish(assignmentId, teacher.accessToken());
            assertEquals(200, published.statusCode(), published.asString());
            assertEquals("PUBLISHED", published.jsonPath().getString("data.status"));

            Response closed = assignmentClient.close(assignmentId, teacher.accessToken());
            assertEquals(200, closed.statusCode(), closed.asString());
            assertEquals("CLOSED", closed.jsonPath().getString("data.status"));

            Response deleted = assignmentClient.delete(assignmentId, teacher.accessToken());
            assertEquals(200, deleted.statusCode(), deleted.asString());
            assignmentId = null;
        } finally {
            if (assignmentId != null) {
                assignmentClient.delete(assignmentId, teacher.accessToken());
            }
            logout(teacher);
        }
    }

    @Test
    void unknownAssignmentIsNotReportedAsServerError() {
        AssignmentTestDataConfiguration data = requireReadFixtures();
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response response = assignmentClient.get(UNKNOWN_ID, teacher.accessToken());
            assertEquals(404, response.statusCode(), response.asString());
        } finally {
            logout(teacher);
        }
    }

    private static AssignmentTestDataConfiguration requireReadFixtures() {
        AssignmentTestDataConfiguration data = AssignmentTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.readFixturesConfigured(),
                "Set TEST_ASSIGNMENT_COURSE_ID, TEST_ASSIGNMENT_MODULE_ID, "
                        + "TEST_ASSIGNMENT_STUDENT_ID and TEST_ASSIGNMENT_ID");
        return data;
    }

    private static AssignmentTestDataConfiguration requireWriteFixtures() {
        AssignmentTestDataConfiguration data = AssignmentTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.writeFixturesConfigured(),
                "Set assignment fixture IDs and TEST_ASSIGNMENT_WRITE_ENABLED=true for write tests");
        return data;
    }

    private static Map<String, Object> validAssignmentRequest(
            AssignmentTestDataConfiguration data, String title) {
        LocalDateTime start = LocalDateTime.now().plusHours(1).withNano(0);
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("courseId", data.courseId().toString());
        request.put("moduleId", data.moduleId().toString());
        request.put("title", title);
        request.put("description", "Synthetic TEST-011 assignment");
        request.put("instructions", "Complete the integration test assignment");
        request.put("startDate", start.toString());
        request.put("dueDate", start.plusDays(7).toString());
        request.put("maxMarks", 100);
        request.put("allowedAttempts", 2);
        request.put("allowLateSubmission", false);
        request.put("latePenaltyPercentage", 0);
        request.put("rubricItems", java.util.List.of(
                Map.of("criterion", "Correctness", "marks", 60),
                Map.of("criterion", "Explanation", "marks", 40)));
        return request;
    }

    private static Map<String, Object> validUpdateRequest(String title) {
        LocalDateTime start = LocalDateTime.now().plusHours(1).withNano(0);
        return Map.of(
                "title", title,
                "description", "Updated synthetic TEST-011 assignment",
                "instructions", "Complete the updated integration test assignment",
                "startDate", start.toString(),
                "dueDate", start.plusDays(7).toString(),
                "maxMarks", 100,
                "allowedAttempts", 2,
                "allowLateSubmission", false,
                "latePenaltyPercentage", 0);
    }
}

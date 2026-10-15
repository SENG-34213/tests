package com.mentify.tests.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AssignmentAuthenticationIT extends AssignmentIntegrationTestSupport {

    @Test
    void rejectsProtectedAssignmentAndSubmissionRequestsWithoutAuthentication() {
        assertEquals(401, assignmentClient.create(Map.of(), null).statusCode());
        assertEquals(401, assignmentClient.get("00000000-0000-0000-0000-000000000000", null).statusCode());
        assertEquals(401, assignmentClient.listForCourse(
                "00000000-0000-0000-0000-000000000000", null).statusCode());
        assertEquals(401, assignmentClient.delete(
                "00000000-0000-0000-0000-000000000000", null).statusCode());
        assertEquals(401, submissionClient.create(
                "00000000-0000-0000-0000-000000000000", Map.of(), null).statusCode());
        assertEquals(401, submissionClient.listMine(null).statusCode());
        assertEquals(401, submissionClient.grade(
                "00000000-0000-0000-0000-000000000000", Map.of(), null).statusCode());
    }

    @Test
    void rejectsInvalidAssignmentPayloadBeforeCreation() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response response = assignmentClient.create(Map.of(), teacher.accessToken());

            assertEquals(400, response.statusCode(), response.asString());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void rejectsStudentAssignmentManagement() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(403, assignmentClient.create(Map.of(), student.accessToken()).statusCode());
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
        } finally {
            logout(teacher);
        }
    }
}

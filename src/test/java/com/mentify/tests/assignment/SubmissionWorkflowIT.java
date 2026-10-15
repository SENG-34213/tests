package com.mentify.tests.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class SubmissionWorkflowIT extends AssignmentIntegrationTestSupport {

    @Test
    void studentCanCreateUpdateAndSubmitDraftAndTeacherCanReviewIt() {
        AssignmentTestDataConfiguration data = requireWriteFixtures();
        AuthenticationSession student = login(TestRole.STUDENT);
        String submissionId = null;
        try {
            Response created = submissionClient.create(
                    data.assignmentId().toString(), Map.of("content", "TEST-011 draft"),
                    student.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            submissionId = created.jsonPath().getString("data.id");
            assertNotNull(submissionId, created.asString());
            assertEquals("DRAFT", created.jsonPath().getString("data.status"));

            Response updated = submissionClient.update(submissionId,
                    Map.of("content", "TEST-011 updated draft"), student.accessToken());
            assertEquals(200, updated.statusCode(), updated.asString());
            assertEquals("TEST-011 updated draft", updated.jsonPath().getString("data.content"));

            Response submitted = submissionClient.submit(submissionId, student.accessToken());
            assertEquals(200, submitted.statusCode(), submitted.asString());
            assertEquals("SUBMITTED", submitted.jsonPath().getString("data.status"));

            Response mine = submissionClient.listMine(student.accessToken());
            assertEquals(200, mine.statusCode(), mine.asString());
            assertTrue(mine.jsonPath().getList("data.id").contains(submissionId), mine.asString());
        } finally {
            logout(student);
        }

        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response listed = submissionClient.listForAssignment(
                    data.assignmentId().toString(), teacher.accessToken());
            assertEquals(200, listed.statusCode(), listed.asString());
            assertTrue(listed.jsonPath().getList("data.id").contains(submissionId), listed.asString());

            Response graded = submissionClient.grade(submissionId,
                    Map.of("marks", BigDecimal.valueOf(85), "feedback", "Good work"),
                    teacher.accessToken());
            assertEquals(200, graded.statusCode(), graded.asString());
            assertEquals("85", graded.jsonPath().getString("data.marks"));

            Response returned = submissionClient.returnSubmission(submissionId, teacher.accessToken());
            assertEquals(200, returned.statusCode(), returned.asString());
            assertEquals("RETURNED", returned.jsonPath().getString("data.status"));
        } finally {
            logout(teacher);
        }
    }

    private static AssignmentTestDataConfiguration requireWriteFixtures() {
        AssignmentTestDataConfiguration data = AssignmentTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.writeFixturesConfigured(),
                "Set assignment fixture IDs and TEST_ASSIGNMENT_WRITE_ENABLED=true for write tests");
        return data;
    }
}

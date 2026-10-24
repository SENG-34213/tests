package com.mentify.tests.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class SubmissionWorkflowIT extends AssignmentIntegrationTestSupport {

    @Test
    void rejectsSubmittingADraftAfterItsDueDateWhenLateWorkIsDisabled() {
        AssignmentTestDataConfiguration data = AssignmentTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.lateSubmissionId() != null,
                "Set TEST_ASSIGNMENT_LATE_SUBMISSION_ID to a draft on a published, past-due assignment");
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            Response draft = submissionClient.get(data.lateSubmissionId().toString(), student.accessToken());
            assertEquals(200, draft.statusCode(), draft.asString());
            assertEquals("DRAFT", draft.jsonPath().getString("data.status"));

            String assignmentId = draft.jsonPath().getString("data.assignmentId");
            assertNotNull(assignmentId, draft.asString());
            Response assignment = assignmentClient.get(assignmentId, student.accessToken());
            assertEquals(200, assignment.statusCode(), assignment.asString());
            assertEquals(false, assignment.jsonPath().getBoolean("data.allowLateSubmission"));
            assertTrue(LocalDateTime.parse(assignment.jsonPath().getString("data.dueDate"))
                    .isBefore(LocalDateTime.now()), "Fixture assignment must already be past due");

            Response denied = submissionClient.submit(data.lateSubmissionId().toString(), student.accessToken());
            assertEquals(409, denied.statusCode(), denied.asString());

            Response unchanged = submissionClient.get(data.lateSubmissionId().toString(), student.accessToken());
            assertEquals(200, unchanged.statusCode(), unchanged.asString());
            assertEquals("DRAFT", unchanged.jsonPath().getString("data.status"));
            assertNull(unchanged.jsonPath().getString("data.submittedAt"));
        } finally {
            logout(student);
        }
    }

    @Test
    void rejectsAnAdditionalSubmissionAfterTheConfiguredAttemptLimit() {
        AssignmentTestDataConfiguration data = AssignmentTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.attemptLimitAssignmentId() != null,
                "Set TEST_ASSIGNMENT_ATTEMPT_LIMIT_ID to a published assignment where the student has reached its limit");
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            Response assignment = assignmentClient.get(
                    data.attemptLimitAssignmentId().toString(), student.accessToken());
            assertEquals(200, assignment.statusCode(), assignment.asString());
            int allowedAttempts = assignment.jsonPath().getInt("data.allowedAttempts");
            assertTrue(allowedAttempts > 0, assignment.asString());

            Response before = submissionClient.listMine(student.accessToken());
            assertEquals(200, before.statusCode(), before.asString());
            java.util.List<String> assignmentIdsBefore = before.jsonPath().getList("data.assignmentId", String.class);
            long attemptsBefore = assignmentIdsBefore.stream()
                    .filter(data.attemptLimitAssignmentId().toString()::equals)
                    .count();
            Assumptions.assumeTrue(attemptsBefore >= allowedAttempts,
                    "Fixture student must already have reached TEST_ASSIGNMENT_ATTEMPT_LIMIT_ID's attempt limit");

            Response denied = submissionClient.create(data.attemptLimitAssignmentId().toString(),
                    Map.of("content", "Additional attempt must be rejected"), student.accessToken());
            assertEquals(409, denied.statusCode(), denied.asString());

            Response after = submissionClient.listMine(student.accessToken());
            assertEquals(200, after.statusCode(), after.asString());
            java.util.List<String> assignmentIdsAfter = after.jsonPath().getList("data.assignmentId", String.class);
            long attemptsAfter = assignmentIdsAfter.stream()
                    .filter(data.attemptLimitAssignmentId().toString()::equals)
                    .count();
            assertEquals(attemptsBefore, attemptsAfter,
                    "A rejected attempt must not create another submission record");
        } finally {
            logout(student);
        }
    }

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

package com.mentify.tests.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AssignmentAttachmentAuthenticationIT extends AssignmentIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void rejectsAttachmentAndFileRequestsWithoutAuthentication() {
        assertEquals(401, assignmentAttachmentClient.list(UNKNOWN_ID, null).statusCode());
        assertEquals(401, assignmentAttachmentClient.create(UNKNOWN_ID, Map.of(), null).statusCode());
        assertEquals(401, assignmentAttachmentClient.download(UNKNOWN_ID, UNKNOWN_ID, null).statusCode());
        assertEquals(401, assignmentAttachmentClient.delete(UNKNOWN_ID, UNKNOWN_ID, null).statusCode());
        assertEquals(401, submissionFileClient.list(UNKNOWN_ID, null).statusCode());
        assertEquals(401, submissionFileClient.create(UNKNOWN_ID, Map.of(), null).statusCode());
        assertEquals(401, submissionFileClient.download(UNKNOWN_ID, UNKNOWN_ID, null).statusCode());
        assertEquals(401, submissionFileClient.delete(UNKNOWN_ID, UNKNOWN_ID, null).statusCode());
    }

    @Test
    void rejectsInvalidAttachmentPayloadAndStudentManagement() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(400, assignmentAttachmentClient.create(UNKNOWN_ID, Map.of(),
                    teacher.accessToken()).statusCode());
            assertEquals(403, assignmentAttachmentClient.create(UNKNOWN_ID, Map.of(),
                    student.accessToken()).statusCode());
        } finally {
            logout(teacher);
            logout(student);
        }
    }

    @Test
    void rejectsTeacherSubmissionFileManagement() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(403, submissionFileClient.create(UNKNOWN_ID, Map.of(),
                    teacher.accessToken()).statusCode());
            assertEquals(403, submissionFileClient.delete(UNKNOWN_ID, UNKNOWN_ID,
                    teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }
}

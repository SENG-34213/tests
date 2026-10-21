package com.mentify.tests.communication;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class CommunicationGroupWorkflowIT extends CommunicationIntegrationTestSupport {

    @Test
    void teacherCanCreateInspectManageAndArchiveAGroup() {
        CommunicationTestDataConfiguration data = CommunicationTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.fixturesConfigured() && data.writeEnabled(),
                "Communication write fixtures are not enabled");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response created = communicationClient.createGroup(CommunicationTestDataFactory.group(data.courseId()),
                    teacher.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            String groupId = created.jsonPath().getString("data.id");
            assertTrue(groupId != null && !groupId.isBlank());
            assertEquals(200, communicationClient.listGroups(teacher.accessToken()).statusCode());
            assertEquals(200, communicationClient.getGroup(groupId, teacher.accessToken()).statusCode());
            assertEquals(200, communicationClient.listMembers(groupId, teacher.accessToken()).statusCode());
            assertEquals(200, communicationClient.addStudent(groupId,
                    CommunicationTestDataFactory.student(data.studentId()), teacher.accessToken()).statusCode());
            assertEquals(200, communicationClient.archive(groupId, teacher.accessToken()).statusCode());
            assertEquals(200, communicationClient.unarchive(groupId, teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void courseMembershipUsesTheCourseEndpoint() {
        CommunicationTestDataConfiguration data = CommunicationTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.fixturesConfigured() && data.writeEnabled(),
                "Communication write fixtures are not enabled");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(200, communicationClient.addStudentToCourse(data.courseId().toString(),
                    CommunicationTestDataFactory.student(data.studentId()), teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }
}

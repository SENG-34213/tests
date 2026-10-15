package com.mentify.tests.communication;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
            assertTrue(created.statusCode() == 201 || created.statusCode() == 200);
            String groupId = created.jsonPath().getString("data.id");
            assertTrue(groupId != null && !groupId.isBlank());
            assertTrue(communicationClient.listGroups(teacher.accessToken()).statusCode() == 200);
            assertTrue(communicationClient.getGroup(groupId, teacher.accessToken()).statusCode() == 200);
            assertTrue(communicationClient.listMembers(groupId, teacher.accessToken()).statusCode() == 200);
            assertTrue(communicationClient.addStudent(groupId, CommunicationTestDataFactory.student(data.studentId()),
                    teacher.accessToken()).statusCode() < 500);
            assertTrue(communicationClient.archive(groupId, teacher.accessToken()).statusCode() == 200);
            assertTrue(communicationClient.unarchive(groupId, teacher.accessToken()).statusCode() == 200);
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
            assertTrue(communicationClient.addStudentToCourse(data.courseId().toString(),
                    CommunicationTestDataFactory.student(data.studentId()), teacher.accessToken()).statusCode() < 500);
        } finally {
            logout(teacher);
        }
    }
}

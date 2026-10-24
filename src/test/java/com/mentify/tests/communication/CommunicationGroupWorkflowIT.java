package com.mentify.tests.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
            assertEquals(201, created.statusCode(), created.asString());
            String groupId = created.jsonPath().getString("data.id");
            assertTrue(groupId != null && !groupId.isBlank());
            Response groups = communicationClient.listGroups(teacher.accessToken());
            assertEquals(200, groups.statusCode(), groups.asString());
            assertTrue(groups.jsonPath().getList("data.id", String.class).contains(groupId), groups.asString());
            Response group = communicationClient.getGroup(groupId, teacher.accessToken());
            assertEquals(200, group.statusCode(), group.asString());
            assertEquals(groupId, group.jsonPath().getString("data.id"), group.asString());
            Response members = communicationClient.listMembers(groupId, teacher.accessToken());
            assertEquals(200, members.statusCode(), members.asString());
            assertFalse(members.jsonPath().getList("data.userId", String.class).isEmpty(), members.asString());
            String updatedName = "Updated integration group " + UUID.randomUUID();
            Response updated = communicationClient.updateGroup(groupId,
                    java.util.Map.of("name", updatedName, "description", "Updated TEST-016 group",
                            "studentMessagingEnabled", true), teacher.accessToken());
            assertEquals(200, updated.statusCode(), updated.asString());
            assertEquals(updatedName, updated.jsonPath().getString("data.name"));
            assertEquals("Updated TEST-016 group", updated.jsonPath().getString("data.description"));
            assertEquals(200, communicationClient.addStudent(groupId,
                    CommunicationTestDataFactory.student(data.studentId()), teacher.accessToken()).statusCode());
            members = communicationClient.listMembers(groupId, teacher.accessToken());
            assertEquals(200, members.statusCode(), members.asString());
            assertTrue(members.jsonPath().getList("data.userId", String.class)
                    .contains(data.studentId().toString()), members.asString());
            Response archived = communicationClient.archive(groupId, teacher.accessToken());
            assertEquals(200, archived.statusCode(), archived.asString());
            assertEquals("ARCHIVED", archived.jsonPath().getString("data.status"), archived.asString());
            Response unarchived = communicationClient.unarchive(groupId, teacher.accessToken());
            assertEquals(200, unarchived.statusCode(), unarchived.asString());
            assertEquals("ACTIVE", unarchived.jsonPath().getString("data.status"), unarchived.asString());
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

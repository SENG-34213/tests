package com.mentify.tests.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StudentAssignmentIT extends AssignmentIntegrationTestSupport {

    @Test
    void studentCanReadFilteredPagedAssignmentsForAnEnrolledCourse() {
        AssignmentTestDataConfiguration data = AssignmentTestDataConfiguration.fromEnvironment();
        assumeTrue(data.readFixturesConfigured(), "Set approved assignment, course, module and student fixture IDs");
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            Response response = assignmentClient.myAssignments(student.accessToken(), Map.of(
                    "courseId", data.courseId().toString(), "page", 0, "size", 5));

            assertEquals(200, response.statusCode(), response.asString());
            assertEquals(0, response.jsonPath().getInt("data.number"));
            assertEquals(5, response.jsonPath().getInt("data.size"));
            List<Map<String, Object>> assignments = response.jsonPath().getList("data.content");
            assertTrue(assignments.size() <= 5);
            assertTrue(response.jsonPath().getLong("data.totalElements") >= assignments.size());
            assertTrue(assignments.stream().allMatch(assignment ->
                    data.courseId().toString().equals(assignment.get("courseId"))
                            && !"DRAFT".equals(assignment.get("assignmentStatus"))));
        } finally {
            logout(student);
        }
    }

    @Test
    void studentAssignmentFiltersAndPagingRejectInvalidRanges() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            Response invalidRange = assignmentClient.myAssignments(student.accessToken(), Map.of(
                    "dueFrom", "2030-02-02T00:00:00", "dueTo", "2030-02-01T00:00:00"));
            assertEquals(400, invalidRange.statusCode(), invalidRange.asString());

            Response invalidPageSize = assignmentClient.myAssignments(student.accessToken(), Map.of("size", 0));
            assertEquals(400, invalidPageSize.statusCode(), invalidPageSize.asString());

            Response oversizedPage = assignmentClient.myAssignments(student.accessToken(), Map.of("size", 101));
            assertEquals(400, oversizedPage.statusCode(), oversizedPage.asString());
        } finally {
            logout(student);
        }
    }
}

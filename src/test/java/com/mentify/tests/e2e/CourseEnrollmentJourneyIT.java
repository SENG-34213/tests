package com.mentify.tests.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import org.junit.jupiter.api.Test;

class CourseEnrollmentJourneyIT extends EndToEndIntegrationTestSupport {

    @Test
    void studentCourseAndEnrollmentIdentifiersRemainConsistentThroughGateway() {
        EndToEndTestConfiguration data = EndToEndTestConfiguration.fromEnvironment();
        assumeTrue(data.courseId() != null && !data.courseId().isBlank(),
                "Set TEST_E2E_COURSE_ID to an approved course fixture");
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            String token = student.accessToken();
            var profile = users.currentUser(token);
            assertEquals(200, profile.statusCode(), profile.asString());
            String studentId = profile.jsonPath().getString("data.id");
            assertTrue(studentId != null && !studentId.isBlank(), profile.asString());

            var course = courses.lookup(data.courseId(), token);
            assertEquals(200, course.statusCode(), course.asString());
            assertEquals(data.courseId(), course.jsonPath().getString("data.id"), course.asString());

            var studentCourses = enrollments.studentCourses(studentId, token);
            assertEquals(200, studentCourses.statusCode(), studentCourses.asString());
            assertEquals("success", studentCourses.jsonPath().getString("status"), studentCourses.asString());
            assertTrue(studentCourses.jsonPath().getList("data").contains(data.courseId()), studentCourses.asString());

            var membership = enrollments.membership(studentId, data.courseId(), token);
            assertEquals(200, membership.statusCode(), membership.asString());
            assertTrue(membership.as(Boolean.class), "Enrollment Service must confirm the same student/course pair");
        } finally {
            logout(student);
        }
    }
}

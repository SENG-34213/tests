package com.mentify.tests.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
            assertEquals(200, courses.lookup(data.courseId(), token).statusCode());
            assertEquals(200, enrollments.getActiveEnrollments(token).statusCode());
            assertEquals(200, enrollments.courseStudents(data.courseId(), token).statusCode());
        } finally {
            logout(student);
        }
    }
}

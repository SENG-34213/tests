package com.mentify.tests.performance;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.course.CourseClient;
import com.mentify.tests.enrollment.EnrollmentClient;
import com.mentify.tests.user.UserClient;
import org.junit.jupiter.api.Test;

class ReadEndpointPerformanceTest extends PerformanceTestSupport {

    @Test
    void currentUserReadLatencyMeetsBaseline() {
        AuthenticationSession session = login();
        try {
            UserClient users = new UserClient(api);
            measure("current-user-read", () -> users.currentUser(session.accessToken()));
        } finally {
            session.clear();
        }
    }

    @Test
    void courseReadLatencyMeetsBaselineWhenFixtureIsConfigured() {
        assumeTrue(!performance.courseId().isBlank(),
                "Set PERFORMANCE_COURSE_ID to run course read baseline");
        AuthenticationSession session = login();
        try {
            CourseClient courses = new CourseClient(api);
            measure("course-read", () -> courses.lookup(performance.courseId(), session.accessToken()));
        } finally {
            session.clear();
        }
    }

    @Test
    void enrollmentReadLatencyMeetsBaselineWhenFixtureIsConfigured() {
        assumeTrue(!performance.courseId().isBlank(),
                "Set PERFORMANCE_COURSE_ID to run enrollment read baseline");
        AuthenticationSession session = login();
        try {
            EnrollmentClient enrollments = new EnrollmentClient(api);
            measure("enrollment-read", () -> enrollments.courseStudents(
                    performance.courseId(), session.accessToken()));
        } finally {
            session.clear();
        }
    }
}

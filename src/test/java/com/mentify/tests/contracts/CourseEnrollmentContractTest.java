package com.mentify.tests.contracts;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.course.CourseClient;
import com.mentify.tests.enrollment.EnrollmentClient;
import org.junit.jupiter.api.Test;

class CourseEnrollmentContractTest extends ContractTestSupport {

    @Test
    void courseAndEnrollmentIdentifiersRemainCompatibleWhenFixtureIsConfigured() {
        assumeTrue(!contract.courseId().isBlank(),
                "Set CONTRACT_COURSE_ID to run course/enrollment contract checks");
        AuthenticationSession session = login(TestRole.STUDENT);
        try {
            CourseClient courses = new CourseClient(api);
            EnrollmentClient enrollments = new EnrollmentClient(api);
            ContractAssertions.assertCommonResponse(courses.lookup(
                    contract.courseId(), session.accessToken()));
            ContractAssertions.assertCommonResponse(enrollments.courseStudents(
                    contract.courseId(), session.accessToken()));
        } finally {
            logout(session);
        }
    }
}

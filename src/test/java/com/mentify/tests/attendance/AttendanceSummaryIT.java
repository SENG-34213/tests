package com.mentify.tests.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import org.junit.jupiter.api.Test;

class AttendanceSummaryIT extends AttendanceIntegrationTestSupport {

    @Test
    void studentCanReadOwnCourseSummary() {
        AttendanceTestDataConfiguration data = AttendanceTestDataConfiguration.fromEnvironment();
        assumeTrue(data.fixturesConfigured(), "Set approved attendance course and student IDs");
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(200, attendanceClient.mySummary(data.courseId().toString(), student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }

    @Test
    void teacherCanReadStudentCourseSummary() {
        AttendanceTestDataConfiguration data = AttendanceTestDataConfiguration.fromEnvironment();
        assumeTrue(data.fixturesConfigured(), "Set approved attendance course and student IDs");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(200, attendanceClient.studentSummary(data.studentId().toString(), data.courseId().toString(),
                    teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }
}

package com.mentify.tests.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.assignment.AssignmentClient;
import com.mentify.tests.assignment.SubmissionClient;
import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import com.mentify.tests.material.MaterialClient;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

class TeacherCourseJourneyIT extends EndToEndIntegrationTestSupport {

    @Test
    void teacherCanFollowOwnedCourseFromLessonAndMaterialToAssignmentGrade() {
        EndToEndWorkflowFixtures fixtures = EndToEndWorkflowFixtures.fromEnvironment();
        assumeTrue(fixtures.teacherJourneyConfigured(),
                "Configure TEST_E2E_TEACHER_COURSE_ID, MODULE_ID, LESSON_ID, MATERIAL_ID, "
                        + "ASSIGNMENT_ID, and SUBMISSION_ID using their TEST_E2E_TEACHER_* names");
        requireServices(RequiredService.COURSE_SERVICE, RequiredService.ASSIGNMENT_SERVICE,
                RequiredService.MATERIAL_SERVICE);

        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            String token = teacher.accessToken();
            Response course = courses.getById(fixtures.teacherCourseId().toString(), token);
            assertEquals(200, course.statusCode(), course.asString());
            assertEquals(fixtures.teacherCourseId().toString(), course.jsonPath().getString("data.id"),
                    course.asString());

            Response lesson = courses.lesson(fixtures.teacherCourseId().toString(),
                    fixtures.teacherModuleId().toString(), fixtures.teacherLessonId().toString(), token);
            assertEquals(200, lesson.statusCode(), lesson.asString());
            assertEquals(fixtures.teacherLessonId().toString(), lesson.jsonPath().getString("data.id"),
                    lesson.asString());

            Response material = new MaterialClient(api).get(fixtures.teacherMaterialId().toString(), token);
            assertEquals(200, material.statusCode(), material.asString());

            Response assignment = new AssignmentClient(api).get(fixtures.teacherAssignmentId().toString(), token);
            assertEquals(200, assignment.statusCode(), assignment.asString());
            assertEquals(fixtures.teacherCourseId().toString(), assignment.jsonPath().getString("data.courseId"),
                    assignment.asString());

            Response submission = new SubmissionClient(api).get(fixtures.teacherSubmissionId().toString(), token);
            assertEquals(200, submission.statusCode(), submission.asString());
            assertEquals(fixtures.teacherAssignmentId().toString(),
                    submission.jsonPath().getString("data.assignmentId"), submission.asString());
            assertTrue(submission.jsonPath().get("data.marks") != null,
                    "The teacher-visible submission must carry its persisted grade");
        } finally {
            logout(teacher);
        }
    }

    private static void requireServices(RequiredService... services) {
        BackendReadinessClient readiness = new BackendReadinessClient(TestEnvironmentConfiguration.fromEnvironment());
        for (RequiredService service : services) {
            readiness.awaitService(service);
        }
    }
}

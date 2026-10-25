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
import com.mentify.tests.quiz.QuizClient;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

class StudentLearningJourneyIT extends EndToEndIntegrationTestSupport {

    @Test
    void studentCanFollowEnrolledCourseThroughLessonAssignmentQuizAndGradedResult() {
        EndToEndWorkflowFixtures fixtures = EndToEndWorkflowFixtures.fromEnvironment();
        assumeTrue(fixtures.studentJourneyConfigured(),
                "Configure TEST_E2E_COURSE_ID, TEST_E2E_MODULE_ID, TEST_E2E_LESSON_ID, "
                        + "TEST_E2E_ASSIGNMENT_ID, TEST_E2E_QUIZ_ID, and TEST_E2E_GRADED_SUBMISSION_ID");
        requireServices(RequiredService.COURSE_SERVICE, RequiredService.ENROLLMENT_SERVICE,
                RequiredService.ASSIGNMENT_SERVICE);

        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            String token = student.accessToken();
            Response profile = users.currentUser(token);
            assertEquals(200, profile.statusCode(), profile.asString());
            String studentId = profile.jsonPath().getString("data.id");
            assertTrue(studentId != null && !studentId.isBlank(), profile.asString());

            Response course = courses.lookup(fixtures.courseId().toString(), token);
            assertEquals(200, course.statusCode(), course.asString());
            assertEquals(fixtures.courseId().toString(), course.jsonPath().getString("data.id"), course.asString());

            Response membership = enrollments.membership(studentId, fixtures.courseId().toString(), token);
            assertEquals(200, membership.statusCode(), membership.asString());
            assertTrue(membership.as(Boolean.class), "The authenticated student must be enrolled in this course");
            Response enrolledCourses = enrollments.studentCourses(studentId, token);
            assertEquals(200, enrolledCourses.statusCode(), enrolledCourses.asString());
            assertTrue(enrolledCourses.jsonPath().getList("data").contains(fixtures.courseId().toString()),
                    enrolledCourses.asString());

            Response lesson = courses.lesson(fixtures.courseId().toString(), fixtures.moduleId().toString(),
                    fixtures.lessonId().toString(), token);
            assertEquals(200, lesson.statusCode(), lesson.asString());
            assertEquals(fixtures.lessonId().toString(), lesson.jsonPath().getString("data.id"), lesson.asString());

            AssignmentClient assignments = new AssignmentClient(api);
            Response assignment = assignments.get(fixtures.assignmentId().toString(), token);
            assertEquals(200, assignment.statusCode(), assignment.asString());
            assertEquals(fixtures.assignmentId().toString(), assignment.jsonPath().getString("data.id"),
                    assignment.asString());
            assertEquals(fixtures.courseId().toString(), assignment.jsonPath().getString("data.courseId"),
                    assignment.asString());

            QuizClient quizzes = new QuizClient(api);
            Response quiz = quizzes.getStudent(fixtures.quizId().toString(), token);
            assertEquals(200, quiz.statusCode(), quiz.asString());
            assertEquals(fixtures.quizId().toString(), quiz.jsonPath().getString("data.id"), quiz.asString());

            SubmissionClient submissions = new SubmissionClient(api);
            Response result = submissions.get(fixtures.gradedSubmissionId().toString(), token);
            assertEquals(200, result.statusCode(), result.asString());
            assertEquals(fixtures.assignmentId().toString(), result.jsonPath().getString("data.assignmentId"),
                    result.asString());
            assertTrue(result.jsonPath().get("data.marks") != null, result.asString());
        } finally {
            logout(student);
        }
    }

    private static void requireServices(RequiredService... services) {
        BackendReadinessClient readiness = new BackendReadinessClient(TestEnvironmentConfiguration.fromEnvironment());
        for (RequiredService service : services) {
            readiness.awaitService(service);
        }
    }
}

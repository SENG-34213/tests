package com.mentify.tests.e2e;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import com.mentify.tests.auth.AuthenticationClient;
import com.mentify.tests.auth.AuthenticationConfiguration;
import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.assignment.AssignmentClient;
import com.mentify.tests.assignment.SubmissionClient;
import com.mentify.tests.course.CourseClient;
import com.mentify.tests.enrollment.EnrollmentClient;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import com.mentify.tests.material.MaterialClient;
import com.mentify.tests.notification.NotificationClient;
import com.mentify.tests.payment.PaymentClient;
import com.mentify.tests.quiz.QuizAttemptClient;
import com.mentify.tests.quiz.QuizClient;
import com.mentify.tests.quiz.QuizQuestionClient;
import com.mentify.tests.user.UserClient;
import org.junit.jupiter.api.BeforeAll;

abstract class EndToEndIntegrationTestSupport {

    protected static ApiClient api;
    protected static AuthenticationClient authentication;
    protected static UserClient users;
    protected static CourseClient courses;
    protected static EnrollmentClient enrollments;
    protected static AssignmentClient assignments;
    protected static SubmissionClient submissions;
    protected static QuizClient quizzes;
    protected static QuizQuestionClient quizQuestions;
    protected static QuizAttemptClient quizAttempts;
    protected static MaterialClient materials;
    protected static PaymentClient payments;
    protected static NotificationClient notifications;
    private static BackendReadinessClient readiness;

    @BeforeAll
    static void requireGatewayEnvironment() {
        EndToEndTestConfiguration e2e = EndToEndTestConfiguration.fromEnvironment();
        assumeTrue(e2e.enabled(), "Set TEST_E2E_ENABLED=true to run cross-service journeys");
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        readiness = new BackendReadinessClient(environment);
        readiness.awaitRequiredServices();
        readiness.awaitService(RequiredService.USER_SERVICE);
        api = new ApiClient(ApiClientConfiguration.fromEnvironment(environment, false));
        authentication = new AuthenticationClient(api, AuthenticationConfiguration.fromEnvironment());
        users = new UserClient(api);
        courses = new CourseClient(api);
        enrollments = new EnrollmentClient(api);
        assignments = new AssignmentClient(api);
        submissions = new SubmissionClient(api);
        quizzes = new QuizClient(api);
        quizQuestions = new QuizQuestionClient(api);
        quizAttempts = new QuizAttemptClient(api);
        materials = new MaterialClient(api);
        payments = new PaymentClient(api);
        notifications = new NotificationClient(api);
    }

    protected static void requireService(RequiredService service) {
        readiness.awaitService(service);
    }

    protected static AuthenticationSession login(TestRole role) {
        return authentication.login(role);
    }

    protected static void logout(AuthenticationSession session) {
        if (session != null && session.refreshable()) {
            authentication.logout(session);
        } else if (session != null) {
            session.clear();
        }
    }
}

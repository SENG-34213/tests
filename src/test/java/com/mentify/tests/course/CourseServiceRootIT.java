package com.mentify.tests.course;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CourseServiceRootIT extends CourseIntegrationTestSupport {

    private static CourseClient directCourseClient;

    @BeforeAll
    static void configureDirectCourseClient() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        ApiClient directService = new ApiClient(
                ApiClientConfiguration.forService(environment, RequiredService.COURSE_SERVICE, false));
        directCourseClient = new CourseClient(directService);
    }

    @Test
    void authenticatedUserCanReadCourseServiceRootGreeting() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response response = directCourseClient.hello(teacher.accessToken());
            assertEquals(200, response.statusCode(), response.asString());
            assertEquals("Mentify Backend", response.asString());
        } finally {
            logout(teacher);
        }
    }
}

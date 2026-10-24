package com.mentify.tests.course;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CourseServiceRootIT {

    private static CourseClient courseClient;

    @BeforeAll
    static void requireCourseService() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        new BackendReadinessClient(environment).awaitService(RequiredService.COURSE_SERVICE);
        ApiClientConfiguration directService = new ApiClientConfiguration(
                environment.serviceUrls().get(RequiredService.COURSE_SERVICE),
                environment.connectTimeout(), environment.readTimeout(), false);
        courseClient = new CourseClient(new ApiClient(directService));
    }

    @Test
    void serviceRootReturnsCourseServiceGreeting() {
        Response response = courseClient.hello();
        assertEquals(200, response.statusCode());
        assertEquals("Mentify Backend", response.asString());
    }
}

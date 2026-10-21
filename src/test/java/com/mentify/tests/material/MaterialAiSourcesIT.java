package com.mentify.tests.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class MaterialAiSourcesIT extends MaterialIntegrationTestSupport {

    @Test
    void shouldAllowTeacherToReadAiSourcesForConfiguredCourse() {
        MaterialTestDataConfiguration data = requireReadFixtures();
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response response = materialClient.aiSources(data.courseId().toString(), teacher.accessToken());
            assertEquals(200, response.statusCode(), response.asString());
            assertEquals("success", response.jsonPath().getString("status"));
            assertNotNull(response.jsonPath().get("data.content"), response.asString());
        } finally {
            logout(teacher);
        }

        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            Response response = materialClient.aiSources(data.courseId().toString(), admin.accessToken());
            assertEquals(200, response.statusCode(), response.asString());
            assertNotNull(response.jsonPath().get("data.content"), response.asString());
        } finally {
            logout(admin);
        }
    }

    @Test
    void shouldRejectStudentAndUnauthenticatedAiSourceReads() {
        MaterialTestDataConfiguration data = requireReadFixtures();
        String courseId = data.courseId().toString();
        assertEquals(401, materialClient.aiSources(courseId, null).statusCode());

        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(403, materialClient.aiSources(courseId, student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }

    private static MaterialTestDataConfiguration requireReadFixtures() {
        MaterialTestDataConfiguration data = MaterialTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.courseId() != null,
                "Set TEST_MATERIAL_COURSE_ID for read-only AI source tests");
        return data;
    }
}

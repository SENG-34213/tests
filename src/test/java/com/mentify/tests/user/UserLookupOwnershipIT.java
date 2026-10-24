package com.mentify.tests.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class UserLookupOwnershipIT extends UserIntegrationTestSupport {

    @Test
    void studentCanLookupOnlyTheirOwnSummaryWhenAnotherStudentIsConfigured() {
        Assumptions.assumeTrue(secondaryStudentCredentialsConfigured(),
                "Set distinct SECONDARY_STUDENT_TEST_USERNAME and SECONDARY_STUDENT_TEST_PASSWORD");
        AuthenticationSession student = login(TestRole.STUDENT);
        AuthenticationSession otherStudent = null;
        try {
            otherStudent = login(TestRole.SECONDARY_STUDENT);
            Response studentProfile = users.currentUser(student.accessToken());
            Response otherProfile = users.currentUser(otherStudent.accessToken());
            assertEquals(200, studentProfile.statusCode(), studentProfile.asString());
            assertEquals(200, otherProfile.statusCode(), otherProfile.asString());
            String studentId = studentProfile.jsonPath().getString("data.id");
            String otherStudentId = otherProfile.jsonPath().getString("data.id");
            assertNotEquals(studentId, otherStudentId,
                    "The secondary-student fixture must refer to a different user");

            Response ownSummary = users.lookupStudents(Map.of("studentIds", List.of(studentId)),
                    student.accessToken());
            assertEquals(200, ownSummary.statusCode(), ownSummary.asString());
            assertTrue(ownSummary.jsonPath().getList("data.studentId", String.class).contains(studentId),
                    ownSummary.asString());

            Response otherSummary = users.lookupStudents(Map.of("studentIds", List.of(otherStudentId)),
                    student.accessToken());
            assertEquals(403, otherSummary.statusCode(), otherSummary.asString());
            assertEquals(403, users.studentsLookup(student.accessToken(), otherStudentId).statusCode());
        } finally {
            logout(student);
            logout(otherStudent);
        }
    }

    private static boolean secondaryStudentCredentialsConfigured() {
        String username = System.getenv("SECONDARY_STUDENT_TEST_USERNAME");
        String password = System.getenv("SECONDARY_STUDENT_TEST_PASSWORD");
        return username != null && !username.isBlank() && password != null && !password.isBlank();
    }
}

package com.mentify.tests.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UserAuthorizationIT extends UserIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void studentsAndTeachersCannotPerformAdministratorOperations() {
        AuthenticationSession student = login(TestRole.STUDENT);
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(403, users.usersByRole("STUDENT", student.accessToken()).statusCode());
            assertEquals(403, users.analytics(student.accessToken()).statusCode());
            assertEquals(403, users.updateStatus(UNKNOWN_ID, Map.of("active", false), student.accessToken()).statusCode());
            assertEquals(403, users.usersByRole("STUDENT", teacher.accessToken()).statusCode());
            assertEquals(403, users.registerUser(Map.of(), teacher.accessToken()).statusCode());
        } finally {
            logout(student);
            logout(teacher);
        }
    }

    @Test
    void protectedUserEndpointsRejectMissingAuthentication() {
        assertEquals(401, users.currentUser(null).statusCode());
        assertEquals(401, users.usersByRole("STUDENT", null).statusCode());
        assertEquals(401, users.listStudents(0, 20, null).statusCode());
        assertEquals(401, users.analytics(null).statusCode());
    }
}

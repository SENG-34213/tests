package com.mentify.tests.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UserStatusLifecycleIT extends UserIntegrationTestSupport {

    @Test
    void administratorCanDeactivateAndReactivateDedicatedTestUser() {
        UserTestData config = UserTestData.fromEnvironment();
        assumeTrue(config.writeEnabled() && config.userId() != null && !config.userId().isBlank(),
                "Set TEST_USER_WRITE_ENABLED=true and TEST_USER_ID to a dedicated disposable user");
        String username = System.getenv("TEST_USER_STATUS_USERNAME");
        String password = System.getenv("TEST_USER_STATUS_PASSWORD");
        assumeTrue(username != null && !username.isBlank() && password != null && !password.isBlank(),
                "Set dedicated TEST_USER_STATUS_USERNAME/PASSWORD credentials for the disposable user");
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            String token = admin.accessToken();
            Response students = users.usersByRole("STUDENT", token);
            assertEquals(200, students.statusCode(), students.asString());
            List<Map<String, Object>> records = students.jsonPath().getList("data");
            boolean fixtureMatches = records.stream().anyMatch(user -> config.userId().equals(user.get("id"))
                    && username.equalsIgnoreCase(String.valueOf(user.get("email"))));
            assumeTrue(fixtureMatches,
                    "TEST_USER_ID and TEST_USER_STATUS_USERNAME must identify the same dedicated student account");
            assertEquals(200, users.login(Map.of("identifier", username, "password", password)).statusCode());
            assertEquals(200, users.updateStatus(config.userId(), Map.of("active", false), token).statusCode());
            Response inactiveLogin = users.login(Map.of("identifier", username, "password", password));
            if (inactiveLogin.statusCode() < 400) {
                throw new AssertionError("Deactivated test account must not authenticate");
            }
            assertEquals(200, users.updateStatus(config.userId(), Map.of("active", true), token).statusCode());
            assertEquals(200, users.login(Map.of("identifier", username, "password", password)).statusCode());
        } finally {
            users.updateStatus(config.userId(), Map.of("active", true), admin.accessToken());
            logout(admin);
        }
    }
}

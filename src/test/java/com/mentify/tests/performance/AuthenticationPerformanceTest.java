package com.mentify.tests.performance;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationConfiguration;
import com.mentify.tests.auth.TestCredentials;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuthenticationPerformanceTest extends PerformanceTestSupport {

    @Test
    void studentLoginLatencyMeetsBaseline() {
        TestCredentials credentials;
        try {
            credentials = AuthenticationConfigurationAccess.studentCredentials();
        } catch (IllegalStateException exception) {
            assumeTrue(false, "Configure dedicated student credentials for performance testing");
            return;
        }
        measure("student-login", () -> api.post("/api/v1/auth/login",
                Map.of("identifier", credentials.username(), "password", credentials.password()), null));
    }

    private static final class AuthenticationConfigurationAccess {
        private static TestCredentials studentCredentials() {
            return AuthenticationConfiguration.fromEnvironment().credentialsFor(TestRole.STUDENT);
        }
    }
}

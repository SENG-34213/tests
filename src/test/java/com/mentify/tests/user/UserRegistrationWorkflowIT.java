package com.mentify.tests.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class UserRegistrationWorkflowIT extends UserIntegrationTestSupport {

    @Test
    void publicStudentRegistrationCreatesAccountAndCapturesCredentialsEmail() {
        UserTestData config = UserTestData.fromEnvironment();
        assumeTrue(config.writeEnabled(), "Set TEST_USER_WRITE_ENABLED=true for isolated registration tests");
        requireMailCaptureOptIn();
        MailpitTestClient mailpit = MailpitTestClient.fromEnvironment();
        String email = syntheticEmail();
        Set<String> existingMessageIds = mailpit.messageIdsForRecipient(email);
        AuthenticationSession admin = login(TestRole.ADMIN);
        String userId = null;
        try {
            Response created = users.registerStudent(UserTestData.validPublicStudentRegistration(email), null);
            assertEquals(201, created.statusCode(), created.asString());
            userId = created.jsonPath().getString("data.id");
            assertNotNull(userId, created.asString());
            assertEquals(email, created.jsonPath().getString("data.email"));
            assertEquals("STUDENT", created.jsonPath().getString("data.role"));

            String credentialsMessage = mailpit.awaitNewMessageForRecipient(email, existingMessageIds);
            mailpit.assertMessageHasContent(credentialsMessage);
        } finally {
            try {
                if (userId != null) {
                    Response deleted = users.deleteUser(userId, admin.accessToken());
                    assertEquals(200, deleted.statusCode(), deleted.asString());
                }
            } finally {
                try {
                    mailpit.deleteMessages(diff(mailpit.messageIdsForRecipient(email), existingMessageIds));
                } finally {
                    logout(admin);
                }
            }
        }
    }

    @Test
    void administratorCanRegisterSyntheticStudentAndCaptureInvitationAndResetEmails() {
        UserTestData config = UserTestData.fromEnvironment();
        assumeTrue(config.writeEnabled(), "Set TEST_USER_WRITE_ENABLED=true for isolated registration tests");
        requireMailCaptureOptIn();
        MailpitTestClient mailpit = MailpitTestClient.fromEnvironment();
        String email = syntheticEmail();
        Set<String> existingMessageIds = mailpit.messageIdsForRecipient(email);
        AuthenticationSession admin = login(TestRole.ADMIN);
        String userId = null;
        try {
            Response created = users.registerUser(UserTestData.validRegistration(email), admin.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            userId = created.jsonPath().getString("data.id");
            assertNotNull(userId, created.asString());
            assertEquals(email, created.jsonPath().getString("data.email"));
            assertEquals("STUDENT", created.jsonPath().getString("data.role"));

            String invitationId = mailpit.awaitNewMessageForRecipient(email, existingMessageIds);
            mailpit.assertMessageHasContent(invitationId);
            Set<String> afterInvitation = new java.util.HashSet<>(existingMessageIds);
            afterInvitation.add(invitationId);

            Response resent = users.resendInvitation(userId, admin.accessToken());
            assertEquals(200, resent.statusCode(), resent.asString());
            String resentInvitationId = mailpit.awaitNewMessageForRecipient(email, afterInvitation);
            mailpit.assertMessageHasContent(resentInvitationId);
            afterInvitation.add(resentInvitationId);

            Response resetRequested = authentication.requestPasswordReset(email);
            assertEquals(200, resetRequested.statusCode(), resetRequested.asString());
            String resetId = mailpit.awaitNewMessageForRecipient(email, afterInvitation);
            assertFalse(resetId.isBlank());
            mailpit.assertMessageHasContent(resetId);

            Matcher otpMatch = Pattern.compile("(?<!\\d)\\d{6}(?!\\d)").matcher(mailpit.messageText(resetId));
            assertTrue(otpMatch.find(), "Captured reset email must contain a six-digit OTP");
            Response verified = authentication.verifyPasswordReset(email, otpMatch.group());
            assertEquals(200, verified.statusCode(), verified.asString());
            String resetToken = verified.jsonPath().getString("data.resetToken");
            assertNotNull(resetToken, verified.asString());
            Response passwordReset = authentication.resetPassword(resetToken, "SyntheticReset123!");
            assertEquals(200, passwordReset.statusCode(), passwordReset.asString());
        } finally {
            try {
                if (userId != null) {
                    Response deleted = users.deleteUser(userId, admin.accessToken());
                    assertEquals(200, deleted.statusCode(), deleted.asString());
                }
            } finally {
                try {
                    mailpit.deleteMessages(diff(mailpit.messageIdsForRecipient(email), existingMessageIds));
                } finally {
                    logout(admin);
                }
            }
        }
    }

    @Test
    void superAdministratorCanRegisterSyntheticAdministratorAndCaptureInvitation() {
        UserTestData config = UserTestData.fromEnvironment();
        assumeTrue(config.writeEnabled() && Boolean.parseBoolean(
                        System.getenv().getOrDefault("TEST_SUPER_ADMIN_SUPPORTED", "false")),
                "Set TEST_USER_WRITE_ENABLED=true and TEST_SUPER_ADMIN_SUPPORTED=true");
        assumeTrue(credentialsConfigured("SUPER_ADMIN_TEST_USERNAME", "SUPER_ADMIN_TEST_PASSWORD"),
                "Configure a dedicated SUPER_ADMIN_TEST_USERNAME/PASSWORD pair");
        requireMailCaptureOptIn();
        MailpitTestClient mailpit = MailpitTestClient.fromEnvironment();
        String email = syntheticEmail();
        Set<String> existingMessageIds = mailpit.messageIdsForRecipient(email);
        AuthenticationSession superAdmin = login(TestRole.SUPER_ADMIN);
        String userId = null;
        try {
            Response created = users.registerAdmin(UserTestData.validAdminRegistration(email),
                    superAdmin.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            userId = created.jsonPath().getString("data.id");
            assertNotNull(userId, created.asString());
            assertEquals("ADMIN", created.jsonPath().getString("data.role"));
            assertEquals("INVITED", created.jsonPath().getString("data.accountStatus"));

            String invitationId = mailpit.awaitNewMessageForRecipient(email, existingMessageIds);
            mailpit.assertMessageHasContent(invitationId);
        } finally {
            try {
                if (userId != null) {
                    Response deleted = users.deleteUser(userId, superAdmin.accessToken());
                    assertEquals(200, deleted.statusCode(), deleted.asString());
                }
            } finally {
                try {
                    mailpit.deleteMessages(diff(mailpit.messageIdsForRecipient(email), existingMessageIds));
                } finally {
                    logout(superAdmin);
                }
            }
        }
    }

    private static void requireMailCaptureOptIn() {
        assumeTrue(Boolean.parseBoolean(System.getenv().getOrDefault(
                        "TEST_USER_MAIL_CAPTURE_ENABLED", "false")),
                "Set TEST_USER_MAIL_CAPTURE_ENABLED=true only after backend SMTP is configured to local Mailpit");
    }

    private static String syntheticEmail() {
        return "codex-test-" + UUID.randomUUID() + "@example.invalid";
    }

    private static boolean credentialsConfigured(String usernameName, String passwordName) {
        String username = System.getenv(usernameName);
        String password = System.getenv(passwordName);
        return username != null && !username.isBlank() && password != null && !password.isBlank();
    }

    private static Set<String> diff(Set<String> current, Set<String> original) {
        current.removeAll(original);
        return current;
    }
}

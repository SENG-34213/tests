package com.mentify.tests.material;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MaterialAuthenticationIT extends MaterialIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void rejectsProtectedMaterialRequestsWithoutAuthentication() {
        assertEquals(401, materialClient.uploadWithoutFile(Map.of(), null).statusCode());
        assertEquals(401, materialClient.list(Map.of(), null).statusCode());
        assertEquals(401, materialClient.get(UNKNOWN_ID, null).statusCode());
        assertEquals(401, materialClient.download(UNKNOWN_ID, null).statusCode());
        assertEquals(401, materialClient.update(UNKNOWN_ID, Map.of(), null).statusCode());
        assertEquals(401, materialClient.archive(UNKNOWN_ID, null).statusCode());
        assertEquals(401, materialClient.delete(UNKNOWN_ID, null).statusCode());
        assertEquals(401, materialClient.audit(Map.of("dryRun", true), null).statusCode());
        assertEquals(401, materialClient.cleanup(Map.of("dryRun", true), null).statusCode());
    }

    @Test
    void studentCannotManageMaterialsOrRunMaintenance() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(403, materialClient.update(UNKNOWN_ID, Map.of("title", "blocked"),
                    student.accessToken()).statusCode());
            assertEquals(403, materialClient.archive(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, materialClient.delete(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, materialClient.audit(Map.of("dryRun", true),
                    student.accessToken()).statusCode());
            assertEquals(403, materialClient.cleanup(Map.of("dryRun", true),
                    student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }

    @Test
    void teacherCannotRunMaintenance() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(403, materialClient.audit(Map.of("dryRun", true),
                    teacher.accessToken()).statusCode());
            assertEquals(403, materialClient.cleanup(Map.of("dryRun", true),
                    teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void profileImageUploadRequiresAnImagePart() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(400, materialClient.uploadProfileImageWithoutFile(student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }

    @Test
    void studentCannotDownloadAnotherUsersProfileImage() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            String otherUsersKey = "materials/profile-images/00000000-0000-0000-0000-000000000001/image.png";
            assertEquals(403, materialClient.downloadProfileImage(otherUsersKey, student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }
}

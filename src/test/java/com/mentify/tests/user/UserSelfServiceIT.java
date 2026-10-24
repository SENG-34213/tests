package com.mentify.tests.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UserSelfServiceIT extends UserIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void shouldAllowUsersToReadAndNoOpUpdateTheirOwnProfile() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            Response profile = users.currentUser(student.accessToken());
            assertEquals(200, profile.statusCode(), profile.asString());
            assertNotNull(profile.jsonPath().get("data.id"), profile.asString());

            Response update = users.updateCurrentUser(Map.of(), student.accessToken());
            assertEquals(200, update.statusCode(), update.asString());
            assertEquals(200, update.jsonPath().getInt("statusCode"));
        } finally {
            logout(student);
        }
    }

    @Test
    void shouldAllowTeacherToSubmitNoOpProfileUpdateAndRestrictTeacherOnlyRoutes() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            Response update = users.updateTeacherProfile(Map.of(), teacher.accessToken());
            assertEquals(200, update.statusCode(), update.asString());

            assertEquals(403, users.updateTeacherProfile(Map.of(), student.accessToken()).statusCode());
            assertEquals(403, users.guidanceNotes(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, users.createGuidanceNote(UNKNOWN_ID, Map.of("note", "blocked"),
                    student.accessToken()).statusCode());

            Response invalidNote = users.createGuidanceNote(UNKNOWN_ID, Map.of(), teacher.accessToken());
            assertEquals(400, invalidNote.statusCode(), invalidNote.asString());
        } finally {
            logout(teacher);
            logout(student);
        }
    }

    @Test
    void shouldReturnNotFoundForUnknownTeacherAndEmptyForUnknownStudentLookup() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(404, users.teacherLookup(UNKNOWN_ID, teacher.accessToken()).statusCode());
            assertEquals(404, users.teacherCanonicalId(UNKNOWN_ID, teacher.accessToken()).statusCode());

            Response students = users.studentsLookup(teacher.accessToken(), UNKNOWN_ID);
            assertEquals(200, students.statusCode(), students.asString());
            assertNotNull(students.jsonPath().get("data"), students.asString());

            Response emptyRequest = users.lookupStudents(Map.of("studentIds", java.util.List.of()),
                    teacher.accessToken());
            assertEquals(400, emptyRequest.statusCode(), emptyRequest.asString());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void shouldRejectUnauthenticatedSelfServiceAndProtectedLookupCalls() {
        assertEquals(401, users.currentUser(null).statusCode());
        assertEquals(401, users.updateCurrentUser(Map.of(), null).statusCode());
        assertEquals(401, users.changePassword(Map.of(), null).statusCode());
        assertEquals(401, users.updateTeacherProfile(Map.of(), null).statusCode());
        assertEquals(401, users.teacherLookup(UNKNOWN_ID, null).statusCode());
        assertEquals(401, users.teacherCanonicalId(UNKNOWN_ID, null).statusCode());
        assertEquals(401, users.studentsLookup(null, UNKNOWN_ID).statusCode());
        assertEquals(401, users.lookupStudents(Map.of("studentIds", java.util.List.of(UNKNOWN_ID)), null)
                .statusCode());
        assertEquals(401, users.guidanceNotes(UNKNOWN_ID, null).statusCode());
        assertEquals(401, users.createGuidanceNote(UNKNOWN_ID, Map.of(), null).statusCode());
    }

    @Test
    void shouldValidatePasswordChangeAndRequireAuthenticationForRegistration() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(400, users.changePassword(Map.of(), student.accessToken()).statusCode());
        } finally {
            logout(student);
        }

        assertEquals(401, users.registerStudent(Map.of(), null).statusCode());
    }
}

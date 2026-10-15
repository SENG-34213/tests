package com.mentify.tests.quiz;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class QuizClientTest {

    private final ApiClient client = new ApiClient(new ApiClientConfiguration(
            URI.create("http://localhost:8080"), Duration.ofSeconds(1), Duration.ofSeconds(1), false));

    @Test
    void clientsRequireApiClient() {
        assertThrows(NullPointerException.class, () -> new QuizClient(null));
        assertThrows(NullPointerException.class, () -> new QuizQuestionClient(null));
        assertThrows(NullPointerException.class, () -> new QuizAttemptClient(null));
    }

    @Test
    void clientsCanBeConstructed() {
        assertNotNull(new QuizClient(client));
        assertNotNull(new QuizQuestionClient(client));
        assertNotNull(new QuizAttemptClient(client));
    }

    @Test
    void clientsRejectBlankResourceIds() {
        QuizClient quizzes = new QuizClient(client);
        QuizQuestionClient questions = new QuizQuestionClient(client);
        QuizAttemptClient attempts = new QuizAttemptClient(client);
        assertThrows(IllegalArgumentException.class, () -> quizzes.getTeacher(" ", null));
        assertThrows(IllegalArgumentException.class, () -> quizzes.listForCourse("", null));
        assertThrows(IllegalArgumentException.class, () -> questions.create(" ", Map.of(), null));
        assertThrows(IllegalArgumentException.class, () -> questions.update("q", "", Map.of(), null));
        assertThrows(IllegalArgumentException.class, () -> attempts.start(null, null));
        assertThrows(IllegalArgumentException.class, () -> attempts.saveAnswer("", Map.of(), null));
    }
}

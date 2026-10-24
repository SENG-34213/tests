package com.mentify.tests.auth;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import io.restassured.response.Response;
import java.util.Map;

public class AuthenticationClient {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String REFRESH_PATH = "/api/v1/auth/refresh";
    private static final String LOGOUT_PATH = "/api/v1/auth/logout";

    private final ApiClient apiClient;
    private final ApiClient authServiceClient;
    private final AuthenticationConfiguration configuration;

    public AuthenticationClient() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        this.apiClient = new ApiClient(ApiClientConfiguration.fromEnvironment(environment, false));
        this.authServiceClient = new ApiClient(ApiClientConfiguration.forService(
                environment, RequiredService.AUTH_SERVICE, false));
        this.configuration = AuthenticationConfiguration.fromEnvironment();
    }

    public AuthenticationClient(ApiClient apiClient, AuthenticationConfiguration configuration) {
        this(apiClient, apiClient, configuration);
    }

    public AuthenticationClient(ApiClient apiClient, ApiClient authServiceClient,
                                AuthenticationConfiguration configuration) {
        this.apiClient = apiClient;
        this.authServiceClient = authServiceClient;
        this.configuration = configuration;
    }

    public AuthenticationSession login() {
        return login(configuration.requireDefaultCredentials());
    }

    public AuthenticationSession login(TestRole role) {
        return login(configuration.credentialsFor(role));
    }

    public AuthenticationSession login(TestCredentials credentials) {
        Response response = apiClient.post(LOGIN_PATH,
                Map.of("identifier", credentials.username(), "password", credentials.password()), null);
        return AuthenticationResponseParser.successfulSession(response);
    }

    public AuthenticationSession refresh(AuthenticationSession session) {
        requireRefreshable(session);
        Response response = apiClient.post(REFRESH_PATH,
                Map.of("refreshToken", session.refreshToken()), null);
        AuthenticationSession replacement = AuthenticationResponseParser.successfulSession(response);
        session.replaceTokens(replacement);
        return session;
    }

    public void logout(AuthenticationSession session) {
        requireRefreshable(session);
        Response response = apiClient.post(LOGOUT_PATH,
                Map.of("refreshToken", session.refreshToken()), null);
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw AuthenticationResponseParser.failure(response);
        }
        session.clear();
    }

    public Response requestPasswordReset(String email) {
        return apiClient.post("/api/v1/auth/forgot-password", Map.of("email", email), null);
    }

    public Response verifyPasswordReset(String email, String otp) {
        return apiClient.post("/api/v1/auth/forgot-password/verify",
                Map.of("email", email, "otp", otp), null);
    }

    public Response resetPassword(String resetToken, String newPassword) {
        return apiClient.post("/api/v1/auth/forgot-password/reset",
                Map.of("resetToken", resetToken, "newPassword", newPassword), null);
    }

    public Response jwks() {
        return authServiceClient.get("/api/v1/auth/.well-known/jwks.json");
    }

    public ApiClient authenticatedApiClient(AuthenticationSession session) {
        if (session == null || !session.authenticated()) {
            throw new IllegalArgumentException("An authenticated session is required");
        }
        return apiClient.withBearerToken(session.accessToken());
    }

    private static void requireRefreshable(AuthenticationSession session) {
        if (session == null || !session.refreshable()) {
            throw new IllegalArgumentException("A refreshable authentication session is required");
        }
    }
}

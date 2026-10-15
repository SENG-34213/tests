package com.mentify.tests.auth;

import java.time.Instant;

public final class AuthenticationSession {

    private String accessToken;
    private String refreshToken;
    private final String tokenType;
    private final Integer expiresIn;
    private final Integer refreshExpiresIn;
    private final String scope;
    private final Instant issuedAt;

    public AuthenticationSession(String accessToken, String refreshToken, String tokenType,
                                 Integer expiresIn, Integer refreshExpiresIn, String scope,
                                 Instant issuedAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.refreshExpiresIn = refreshExpiresIn;
        this.scope = scope;
        this.issuedAt = issuedAt;
    }

    public String accessToken() {
        return accessToken;
    }

    public String refreshToken() {
        return refreshToken;
    }

    public String tokenType() {
        return tokenType;
    }

    public Integer expiresIn() {
        return expiresIn;
    }

    public Integer refreshExpiresIn() {
        return refreshExpiresIn;
    }

    public String scope() {
        return scope;
    }

    public Instant issuedAt() {
        return issuedAt;
    }

    public boolean authenticated() {
        return accessToken != null && !accessToken.isBlank();
    }

    public boolean refreshable() {
        return refreshToken != null && !refreshToken.isBlank();
    }

    public void replaceTokens(AuthenticationSession replacement) {
        this.accessToken = replacement.accessToken;
        this.refreshToken = replacement.refreshable() ? replacement.refreshToken : this.refreshToken;
    }

    public void clear() {
        accessToken = null;
        refreshToken = null;
    }

    @Override
    public String toString() {
        return "AuthenticationSession{authenticated=" + authenticated()
                + ", refreshable=" + refreshable() + ", tokenType='" + tokenType + "'}";
    }
}

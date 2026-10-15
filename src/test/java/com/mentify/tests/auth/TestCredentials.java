package com.mentify.tests.auth;

public record TestCredentials(String username, String password) {

    @Override
    public String toString() {
        return "TestCredentials{username='" + username + "', password='[REDACTED]'}";
    }
}

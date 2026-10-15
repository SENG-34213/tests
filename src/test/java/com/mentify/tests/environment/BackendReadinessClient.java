package com.mentify.tests.environment;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class BackendReadinessClient {

    private final TestEnvironmentConfiguration configuration;
    private final HttpProbe probe;
    private final Sleeper sleeper;

    public BackendReadinessClient(TestEnvironmentConfiguration configuration) {
        this(configuration, new JdkHttpProbe(configuration), Thread::sleep);
    }

    BackendReadinessClient(TestEnvironmentConfiguration configuration, HttpProbe probe, Sleeper sleeper) {
        this.configuration = configuration;
        this.probe = probe;
        this.sleeper = sleeper;
    }

    public void awaitRequiredServices() {
        if (!configuration.enabled()) {
            throw new BackendReadinessException(
                    "Integration environment is disabled. Set TEST_ENV_ENABLED=true before running integration tests.");
        }
        configuration.requiredServices().forEach(this::awaitService);
    }

    public void awaitService(RequiredService service) {
        URI endpoint = endpoint(service);
        long deadline = System.nanoTime() + configuration.readinessTimeout().toNanos();
        String lastFailure = "no response";

        while (System.nanoTime() < deadline) {
            try {
                int status = probe.get(endpoint);
                if (status >= 200 && status < 400) {
                    return;
                }
                lastFailure = "HTTP status " + status;
            } catch (IOException | InterruptedException exception) {
                lastFailure = exception.getMessage() == null
                        ? exception.getClass().getSimpleName()
                        : exception.getMessage();
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    throw new BackendReadinessException("Readiness check interrupted for " + service);
                }
            }

            try {
                sleeper.sleep(configuration.pollInterval().toMillis());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new BackendReadinessException("Readiness check interrupted for " + service);
            }
        }

        throw new BackendReadinessException(
                "Service " + service + " was not ready at " + endpoint
                        + " within " + configuration.readinessTimeout().toSeconds()
                        + " seconds. Last failure: " + lastFailure);
    }

    private URI endpoint(RequiredService service) {
        URI base = configuration.serviceUrls().get(service);
        if (service == RequiredService.KEYCLOAK) {
            return URI.create(base + "/realms/" + configuration.keycloakRealm());
        }
        return URI.create(base + "/actuator/health");
    }

    @FunctionalInterface
    interface HttpProbe {
        int get(URI endpoint) throws IOException, InterruptedException;
    }

    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    private static class JdkHttpProbe implements HttpProbe {

        private final HttpClient client;
        private final Duration readTimeout;

        JdkHttpProbe(TestEnvironmentConfiguration configuration) {
            this.client = HttpClient.newBuilder()
                    .connectTimeout(configuration.connectTimeout())
                    .build();
            this.readTimeout = configuration.readTimeout();
        }

        @Override
        public int get(URI endpoint) throws IOException, InterruptedException {
            HttpRequest request = HttpRequest.newBuilder(endpoint)
                    .timeout(readTimeout)
                    .GET()
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
        }
    }
}

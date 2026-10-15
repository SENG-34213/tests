package com.mentify.tests.performance;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import com.mentify.tests.auth.AuthenticationClient;
import com.mentify.tests.auth.AuthenticationConfiguration;
import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeAll;

abstract class PerformanceTestSupport {

    protected static PerformanceConfiguration performance;
    protected static ApiClient api;
    protected static AuthenticationClient authentication;

    @BeforeAll
    static void requirePerformanceEnvironment() {
        performance = PerformanceConfiguration.fromEnvironment();
        assumeTrue(performance.enabled(),
                "Set PERFORMANCE_TEST_ENABLED=true to run performance baselines");
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        assumeTrue("production".equalsIgnoreCase(performance.environment()) == false,
                "Performance tests must never run against production");
        BackendReadinessClient readiness = new BackendReadinessClient(environment);
        readiness.awaitService(RequiredService.API_GATEWAY);
        api = new ApiClient(new ApiClientConfiguration(
                URI.create(performance.baseUrl()),
                environment.connectTimeout(),
                environment.readTimeout(),
                false));
        authentication = new AuthenticationClient(api, AuthenticationConfiguration.fromEnvironment());
    }

    protected static AuthenticationSession login() {
        return authentication.login(TestRole.STUDENT);
    }

    protected static PerformanceMetrics measure(String scenario, Supplier<Response> request) {
        List<PerformanceMetrics.Sample> samples = new ArrayList<>();
        for (int index = 0; index < performance.warmupRequests(); index++) {
            request.get();
        }
        long started = System.nanoTime();
        for (int index = 0; index < performance.measuredRequests(); index++) {
            samples.add(sample(request));
        }
        PerformanceMetrics metrics = PerformanceMetrics.summarize(
                scenario, samples, elapsedMillis(started));
        reportAndAssert(metrics);
        return metrics;
    }

    protected static PerformanceMetrics measureConcurrent(String scenario, Supplier<Response> request) {
        List<PerformanceMetrics.Sample> samples = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(performance.concurrency());
        long started = System.nanoTime();
        try {
            List<Callable<PerformanceMetrics.Sample>> tasks = new ArrayList<>();
            for (int index = 0; index < performance.measuredRequests(); index++) {
                tasks.add(() -> sample(request));
            }
            for (Future<PerformanceMetrics.Sample> task : executor.invokeAll(
                    tasks, performance.timeoutMillis(), TimeUnit.MILLISECONDS)) {
                if (task.isCancelled()) {
                    samples.add(new PerformanceMetrics.Sample(performance.timeoutMillis(), false, true));
                } else {
                    samples.add(task.get());
                }
            }
        } catch (Exception exception) {
            throw new AssertionError("Performance concurrency execution failed safely", exception);
        } finally {
            executor.shutdownNow();
        }
        PerformanceMetrics metrics = PerformanceMetrics.summarize(
                scenario, samples, elapsedMillis(started));
        reportAndAssert(metrics);
        return metrics;
    }

    private static PerformanceMetrics.Sample sample(Supplier<Response> request) {
        long started = System.nanoTime();
        try {
            Response response = request.get();
            long elapsed = elapsedMillis(started);
            return new PerformanceMetrics.Sample(elapsed,
                    response.statusCode() >= 200 && response.statusCode() < 400, false);
        } catch (RuntimeException exception) {
            return new PerformanceMetrics.Sample(elapsedMillis(started), false, false);
        }
    }

    private static void reportAndAssert(PerformanceMetrics metrics) {
        System.out.println(metrics);
        assertTrue(metrics.errorRatePercent() <= performance.maxErrorRatePercent(),
                () -> "Error-rate threshold exceeded: " + metrics);
        assertTrue(metrics.p95Millis() <= performance.maxP95Millis(),
                () -> "p95 threshold exceeded: " + metrics);
        assertTrue(metrics.p99Millis() <= performance.maxP99Millis(),
                () -> "p99 threshold exceeded: " + metrics);
    }

    private static long elapsedMillis(long startedNanos) {
        return Math.max(1, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedNanos));
    }
}

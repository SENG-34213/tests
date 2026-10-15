package com.mentify.tests.performance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record PerformanceMetrics(
        String scenario,
        int requests,
        int successes,
        int failures,
        int timeouts,
        double errorRatePercent,
        long minMillis,
        long maxMillis,
        double averageMillis,
        long p50Millis,
        long p90Millis,
        long p95Millis,
        long p99Millis,
        double throughputPerSecond) {

    static PerformanceMetrics summarize(String scenario, List<Sample> samples, long elapsedMillis) {
        List<Long> latencies = new ArrayList<>();
        int successes = 0;
        int timeouts = 0;
        for (Sample sample : samples) {
            latencies.add(sample.elapsedMillis());
            if (sample.success()) {
                successes++;
            }
            if (sample.timeout()) {
                timeouts++;
            }
        }
        Collections.sort(latencies);
        int failures = samples.size() - successes;
        double errorRate = samples.isEmpty() ? 0 : failures * 100.0 / samples.size();
        double seconds = Math.max(1, elapsedMillis) / 1000.0;
        return new PerformanceMetrics(
                scenario,
                samples.size(),
                successes,
                failures,
                timeouts,
                errorRate,
                percentile(latencies, 0),
                percentile(latencies, 100),
                latencies.stream().mapToLong(Long::longValue).average().orElse(0),
                percentile(latencies, 50),
                percentile(latencies, 90),
                percentile(latencies, 95),
                percentile(latencies, 99),
                samples.size() / seconds);
    }

    private static long percentile(List<Long> values, int percentile) {
        if (values.isEmpty()) {
            return 0;
        }
        int index = (int) Math.ceil(percentile / 100.0 * values.size()) - 1;
        return values.get(Math.max(0, Math.min(index, values.size() - 1)));
    }

    @Override
    public String toString() {
        return "PerformanceMetrics{" +
                "scenario='" + scenario + '\'' +
                ", requests=" + requests +
                ", successes=" + successes +
                ", failures=" + failures +
                ", timeouts=" + timeouts +
                ", errorRatePercent=" + errorRatePercent +
                ", minMillis=" + minMillis +
                ", maxMillis=" + maxMillis +
                ", averageMillis=" + averageMillis +
                ", p50Millis=" + p50Millis +
                ", p90Millis=" + p90Millis +
                ", p95Millis=" + p95Millis +
                ", p99Millis=" + p99Millis +
                ", throughputPerSecond=" + throughputPerSecond +
                '}';
    }

    record Sample(long elapsedMillis, boolean success, boolean timeout) {
    }
}

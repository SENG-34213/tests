# Performance Testing

TEST-025 provides a bounded performance baseline. It measures latency,
success/error rate, timeouts, throughput, and p50/p90/p95/p99 percentiles for
representative gateway, authentication, read, pagination, safe-write, and
controlled-concurrency scenarios.

Performance tests are not load, stress, spike, soak, or denial-of-service
tests. They must run only against a dedicated approved non-production
environment.

## Safe configuration

```powershell
$env:PERFORMANCE_TEST_ENABLED="true"
$env:PERFORMANCE_ENVIRONMENT="dedicated-local"
$env:PERFORMANCE_BASE_URL="http://localhost:8080"
$env:PERFORMANCE_WARMUP_REQUESTS="5"
$env:PERFORMANCE_MEASURED_REQUESTS="30"
$env:PERFORMANCE_CONCURRENCY="2"
$env:PERFORMANCE_TIMEOUT_MS="10000"
$env:PERFORMANCE_MAX_ERROR_RATE_PERCENT="1.0"
$env:PERFORMANCE_MAX_P95_MS="2000"
$env:PERFORMANCE_MAX_P99_MS="5000"
mvn -Dtest="*PerformanceTest,*ConcurrencyTest" test
```

The suite is disabled by default. A `production` environment value is rejected
by the test support. Counts, concurrency, and timeouts are always bounded.

The optional GitHub Actions workflow is manual-only and uses a protected
`dedicated-performance` environment. Configure its required reviewers,
`PERFORMANCE_BASE_URL`, `PERFORMANCE_ALLOWED_HOST_SUFFIX`, optional
`PERFORMANCE_COURSE_ID`, and synthetic student username/password secrets. The
workflow requires explicit confirmation and the deployed backend commit SHA;
it accepts only credential-free HTTPS URLs under the configured host suffix.
It binds the readiness probe and API requests to the same gateway URL.

## Scenarios

- `GatewayPerformanceTest`: public gateway probe.
- `AuthenticationPerformanceTest`: dedicated student login latency.
- `ReadEndpointPerformanceTest`: current-user, optional course, and optional
  enrollment reads.
- `PaginationPerformanceTest`: notification page retrieval.
- `SafeWritePerformanceTest`: opt-in synthetic invalid-email validation request;
  it does not create a financial or provider record.
- `ControlledConcurrencyTest`: bounded concurrent gateway requests.

Course and enrollment scenarios require `PERFORMANCE_COURSE_ID`. Authentication
scenarios require a dedicated student test account. The write scenario also
requires `PERFORMANCE_WRITE_ENABLED=true`.

## Metrics and thresholds

Each scenario prints a `PerformanceMetrics` summary containing request count,
successes, failures, timeouts, error rate, min/max/average, p50/p90/p95/p99,
and throughput. The configured error-rate, p95, and p99 thresholds are checked
after measurement. Thresholds are initial baselines and must be tuned from
dedicated-environment evidence, not production traffic.

## Data and provider safety

Use synthetic users and approved fixtures. Do not call real payment or AI
providers, send real email, use production data, or create uncontrolled records.
The synthetic write is deliberately validation-only. Any future resource write
must add supported cleanup before being enabled.

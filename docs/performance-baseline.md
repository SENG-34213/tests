# Performance Baseline

## Execution

```powershell
# Confirm the normal suite remains independent.
mvn clean test

# Confirm performance tests remain disabled by default.
mvn -Dtest="*PerformanceTest,*ConcurrencyTest" test

# Run in a dedicated environment after configuring bounded values.
$env:PERFORMANCE_TEST_ENABLED="true"
$env:PERFORMANCE_ENVIRONMENT="dedicated-local"
mvn -Dtest="*PerformanceTest,*ConcurrencyTest" test
```

The normal functional build does not require a performance environment. The
performance command skips clearly when `PERFORMANCE_TEST_ENABLED=false` and
does not generate requests.

## Result interpretation

The manual workflow uploads Surefire reports and a sanitized summary at
`target/performance-evidence/summary.md`. It records the workflow run, test
repository commit, backend deployment revision, and measured scenario metrics.
Review request counts, concurrency, p95/p99, error rate, timeout count, and
throughput against the approved baseline. A threshold failure is a regression
signal, not proof of a production performance guarantee.

Repeat the baseline at least twice and document material variance. Check that
no duplicate or uncontrolled records were created and that the environment is
healthy afterward.

## CI separation

Ordinary pull-request CI runs functional verification only. Performance runs
must be explicit and use the manual `.github/workflows/performance.yml` workflow
or a local invocation. Optional
performance-environment unavailability must not fail ordinary functional CI.
Performance artifacts may be retained only when they contain no credentials,
tokens, personal data, or provider secrets.

## Limitations

This repository does not provision a load generator, database benchmark, broker
fault injector, or external-provider stub. Results are lightweight regression
baselines and are not capacity guarantees, stress-test results, or a substitute
for professional performance engineering.

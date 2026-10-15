# Known Limitations

## Environment and fixtures

- Static tests do not start the backend, Keycloak, databases, Kafka, Mailpit,
  or external providers.
- Live tests need a running, approved non-production environment. Readiness
  fails clearly for a required service that is unavailable.
- E2E tests require `TEST_E2E_ENABLED=true`; the course journey also requires
  `TEST_E2E_COURSE_ID`.
- Provider journeys and writes are disabled unless explicitly approved.
- Some workflows require existing non-production IDs because shared data must
  not be deleted by tests.
- Cleanup uses only supported backend endpoints. Where no endpoint exists,
  cleanup remains disabled or dry-run and the limitation is documented.

## Stability review

- No arbitrary `Thread.sleep` synchronization was found in the reviewed test
  sources; readiness uses bounded polling and configured timeouts.
- No intermittent failure was reproduced during TEST-023 static verification.
  Live-environment flakiness requires the services and fixtures to assess.
- Assertion retries are not used to hide product failures.

## Security

- Credentials, tokens, provider keys, and reset links are environment/CI-secret
  inputs. `.env.example` contains placeholders only.
- Provider tests are opt-in and must use test mode. Production infrastructure
  and real personal data are out of scope.
- Logs and reports must not expose passwords, refresh tokens, authorization
  headers, or sensitive response bodies.

## CI

- Default CI verifies the static suite with live flags disabled; it does not
  prove that every backend service is healthy.
- Live integration execution needs separately configured services and secrets.
- JUnit tags are reserved vocabulary, but class-pattern selection is the
  currently verified Maven mechanism. Tag filtering must not be claimed until
  it is configured and tested.

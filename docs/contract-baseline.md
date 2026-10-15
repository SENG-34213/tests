# Contract Baseline

Baseline schemas are stored under `src/test/resources/contracts/`:

```text
contracts/common/api-response-schema.json
contracts/common/error-response-schema.json
contracts/common/pagination-schema.json
```

They describe stable common fields and intentionally allow service-specific
extensions. Generated identifiers, timestamps, and provider-specific payloads
must not be frozen as literal values.

## Baseline update rules

Do not update a schema only to make a failing test pass. A contract update must
explain the endpoint, service, reason, backward-compatibility impact, affected
clients, documentation changes, and migration requirement. Breaking changes
need explicit review and must preserve an old response when compatibility is
required.

## Strict mode

```powershell
$env:CONTRACT_TEST_ENABLED="true"
$env:CONTRACT_SCHEMA_STRICT="true"
$env:CONTRACT_FAIL_ON_MISSING_FIELDS="true"
$env:CONTRACT_FAIL_ON_EXTRA_REQUIRED_FIELDS="true"
mvn -Dtest="*ContractTest" test
```

Strict flags are external configuration. They do not permit secrets or response
payloads to be committed.

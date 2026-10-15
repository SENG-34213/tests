# Contract Change Process

1. Identify the backend service, endpoint, method, status, and changed field.
2. Explain whether the change is additive, compatible, deprecated, or breaking.
3. Update the test client, schema, and documentation together when required.
4. Run normal tests and the focused contract selector.
5. Review reports for passwords, tokens, private data, internal URLs, and stack
   traces before publishing.
6. Obtain review for breaking changes and document migration support.

Schema files are versioned with this repository. Direct database access and
production contract checks are not part of this process.

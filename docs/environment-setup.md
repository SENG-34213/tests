# Test Environment Setup

## Current ticket

TEST-001 requires only Java 17, Maven, and Git. The foundation smoke test does not require Docker or a running Mentify backend.

## Prerequisites

- Java 17
- Maven 3.9 or newer
- Git
- Docker Desktop for future Testcontainers suites

## Local commands

```bash
java -version
mvn -version
mvn clean test
mvn verify
```

## Future services

Later tickets may require the backend API Gateway, Keycloak, PostgreSQL, Kafka, Mailpit, service discovery, configuration server, AI provider doubles, and payment-provider doubles. Their URLs and credentials must be supplied through environment variables or GitHub Secrets.

## Secrets

Copy `.env.example` to `.env` for local values. Never commit `.env`, real passwords, tokens, API keys, production URLs containing credentials, or production data.

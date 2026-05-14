# Contributing to jws-diag

This document covers the workflow and standards for the project.

## Getting Started

1. Fork the repository and clone your fork
2. Ensure you have JDK 11+ and Maven installed
3. Build and run tests: `mvn verify`
4. Run the CLI: `java -jar target/jws-diag-0.1.0-SNAPSHOT.jar --help`

## Branch and PR Workflow

**No direct pushes to `main`.** All changes go through Pull Requests.

### Branch naming

Use a prefix that describes the type of change:

- `feat/` - new functionality (e.g., `feat/summary-discovery`)
- `fix/` - bug fixes (e.g., `fix/xml-parse-error`)
- `docs/` - documentation changes (e.g., `docs/quickstart-guide`)
- `test/` - test additions or fixes (e.g., `test/golden-output`)

### Pull Request process

1. Create a branch from `main` with an appropriate prefix
2. Make your changes in focused, reviewable commits
3. Ensure `mvn verify` passes locally before pushing
4. Open a PR with a clear description: what changed, why, and how to test
5. Request a review from fellow contributors

### PR guidelines

- Keep PRs focused - one feature or fix per PR
- Aim for under 500 lines changed per PR
- Include tests for new functionality
- Update documentation if you change user-facing behavior

## Code Standards

- **Java 11+** - no preview features
- **Maven** - the build must pass `mvn verify`
- **JUnit 5** - for all tests, with AssertJ for assertions
- **No commented-out code** - remove it or don't commit it
- **Javadoc** - on public API classes only, not internal implementation

## Commit Messages

Write clear, concise commit messages:

```
Add CATALINA_BASE discovery from environment variables

Reads CATALINA_BASE and CATALINA_HOME from environment variables
and validates that the paths exist and are readable.
```

- Use imperative mood in the subject line ("Add", not "Added")
- Keep the subject under 72 characters
- Add a body when the "why" isn't obvious from the subject

## Test Fixtures

Place test configuration files (server.xml variants, etc.) in
`src/test/resources/fixtures/`. Use descriptive names:

- `server-valid-basic.xml` - a minimal valid configuration
- `server-missing-ssl.xml` - configuration with missing SSLHostConfig
- `tomcat-users-default-creds.xml` - configuration with default credentials

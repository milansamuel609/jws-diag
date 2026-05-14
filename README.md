# jws-diag

Diagnostic and configuration validation toolkit for
[JBoss Web Server](https://www.redhat.com/en/technologies/jboss-middleware/web-server) /
[Apache Tomcat](https://tomcat.apache.org/).

**Status:** Under development as part of [Google Summer of Code 2026](https://summerofcode.withgoogle.com/).

## What It Does

jws-diag is a read-only CLI that helps SREs and support engineers quickly understand
and validate a Tomcat/JWS installation:

| Command               | Description                                                        |
|-----------------------|--------------------------------------------------------------------|
| `jws-diag summary`   | Show installed versions, JVM info, OS/container signals, native library status |
| `jws-diag config`    | Parse and display effective connector, TLS, proxy, and executor settings |
| `jws-diag validate`  | Run diagnostic rules and report findings as INFO/WARN/ERROR        |
| `jws-diag bundle`    | Generate a redacted .tar.gz support bundle for safe sharing        |

## Building

Requires JDK 11+ and Maven.

```bash
mvn package
```

## Running

```bash
java -jar target/jws-diag-0.1.0-SNAPSHOT.jar --help
java -jar target/jws-diag-0.1.0-SNAPSHOT.jar summary
java -jar target/jws-diag-0.1.0-SNAPSHOT.jar validate --format JSON
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for the development workflow, code standards,
and PR process.

## License

[Apache License 2.0](LICENSE)

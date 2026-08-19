# AGENTS.md — jws-diag Codebase Guide

> For AI agents and contributors working on this codebase.

## What This Project Is

**jws-diag** is a read-only CLI tool that helps SREs and support engineers quickly understand and validate a Tomcat / JBoss Web Server (JWS) installation. It never modifies files, sends network requests, or escalates privileges.

- **Language:** Java 11+
- **Build:** Maven (`mvn verify`)
- **CLI framework:** picocli 4.7.6
- **JSON:** Jackson 2.17.2
- **Tests:** JUnit 5 + AssertJ (no Mockito)
- **Packaging:** fat JAR via maven-shade-plugin
- **CI:** GitHub Actions on Java 11 and 17

## Quick Commands

```bash
mvn verify                  # build + run all tests
mvn package -q              # build JAR only
java -jar target/jws-diag-0.1.0-SNAPSHOT.jar <subcommand>
```

## Architecture Overview

```
Main (picocli root)
├── summary    — detect installed Tomcat/JWS version, JVM, OS, container
├── config     — parse and display server.xml configuration
├── validate   — run 22 diagnostic rules against the installation
├── bundle     — generate a redacted .tar.gz support bundle
├── logs       — scan log files for known error patterns
├── instances  — detect running Tomcat instances via /proc
├── modcluster — display mod_cluster/mod_proxy_cluster config
└── diff       — structural diff between two server.xml configs
```

Every subcommand follows the same shape:

1. **Command** (`@Command` + `Runnable`) — CLI argument parsing, orchestration
2. **Parser / Scanner / Engine** — core logic, testable independently
3. **Model** — immutable value objects, builder pattern, Jackson-annotated
4. **Formatter** — separate `HumanFormatter` and `JsonFormatter` classes

## Package Map

```
org.jboss.jws.diag
├── Main.java                          # picocli entry point, registers all 8 subcommands
├── common/                            # shared utilities
│   ├── ExitCodes.java                 # OK=0, WARNINGS=1, ERRORS=2
│   ├── OutputFormat.java              # enum: HUMAN, JSON
│   ├── OutputFormatMixin.java         # picocli mixin for --format/-f
│   ├── FileUtils.java                 # readFileIfExists, resolveConfigFile
│   ├── RedactionFilter.java           # redacts password/secret/credential attrs
│   ├── RedactionLevel.java            # enum: DEFAULT, STRICT
│   ├── RuleId.java                    # enum of all 22 rule IDs (SEC/TLS/CONN)
│   ├── Severity.java                  # enum: ERROR, WARN, INFO
│   └── UnixPathSerializer.java        # Jackson serializer: Path → forward slashes
│
├── summary/
│   ├── SummaryCommand.java
│   ├── discovery/                     # CatalinaDiscovery, OsDetector, ContainerDetector,
│   │                                  # JvmDetector, TomcatVersionDetector, JwsVersionDetector,
│   │                                  # NativeLibDetector, ProcessDetector, WellKnownPaths,
│   │                                  # SystemdConfigParser, EnvironmentSource
│   ├── model/                         # JwsInstallation, JvmInfo, OsInfo, ContainerInfo,
│   │                                  # ContainerType, NativeInfo
│   └── formatter/                     # SummaryHumanFormatter, SummaryJsonFormatter
│
├── config/
│   ├── ConfigCommand.java
│   ├── parser/                        # ServerXmlParser, PropertyResolver, TomcatDefaults
│   ├── model/                         # ConfigValue<T>, ServerConfig, ServiceConfig,
│   │                                  # ConnectorConfig, ExecutorConfig, EngineConfig,
│   │                                  # HostConfig, SslHostConfig, CertificateConfig,
│   │                                  # ListenerConfig, RealmConfig, ValveConfig, ValveType
│   └── formatter/                     # ConfigHumanFormatter, ConfigJsonFormatter
│
├── validate/
│   ├── ValidateCommand.java
│   ├── ValidationEngine.java          # owns the hardcoded list of all Rule instances
│   ├── Rule.java                      # interface: List<Finding> evaluate(RuleContext)
│   ├── RuleContext.java               # catalinaBase + parsed server.xml + tomcat-users.xml + username
│   ├── ExitCodeCalculator.java        # max severity → exit code
│   ├── model/
│   │   └── Finding.java               # ruleId, category, severity, summary, detail, file, fix
│   ├── output/
│   │   ├── FindingSummary.java         # counts errors/warnings/info
│   │   ├── HumanReadableOutput.java
│   │   └── JsonOutput.java
│   └── rules/
│       ├── security/                  # SEC-001 through SEC-009 (9 rules)
│       ├── tls/                       # TLS-001 through TLS-007 (7 rules)
│       └── connector/                 # CONN-001 through CONN-006 (6 rules)
│
├── bundle/
│   ├── BundleCommand.java
│   ├── BundleEngine.java              # orchestrates collect → redact → stage → archive
│   ├── BundleContext.java             # catalinaBase/Home, stagingDir, RedactionLevel
│   ├── collect/                       # FileCollector, LogCollector (3-day/10k-line cap)
│   ├── redact/                        # Redactor interface + chain: XmlAttribute, Properties,
│   │                                  # Log, IpAddress, Hostname, EnvironmentVariable redactors
│   │                                  # + companion Masker classes
│   ├── model/                         # CollectedFile (immutable, builder, withContent())
│   └── output/                        # StagingWriter, TarWriter, ArchiveWriter,
│                                      # ManifestGenerator, ValidationResultsWriter
│
├── logs/
│   ├── LogsCommand.java
│   ├── LogScanner.java                # regex matching against LogPattern enum
│   ├── model/                         # LogPattern (5 patterns), LogMatch, LogScanResult
│   └── formatter/                     # LogsHumanFormatter, LogsJsonFormatter
│
├── instances/
│   ├── InstancesCommand.java
│   ├── InstanceScanner.java           # scans /proc/*/cmdline for Bootstrap
│   ├── model/                         # TomcatInstance (pid, catalinaHome, catalinaBase)
│   └── formatter/                     # InstancesHumanFormatter, InstancesJsonFormatter
│
├── modcluster/
│   ├── ModClusterCommand.java
│   ├── ModClusterParser.java          # scans <Listener> for ModCluster className
│   ├── model/                         # ModClusterConfig (with mod_cluster defaults)
│   └── formatter/                     # ModClusterHumanFormatter, ModClusterJsonFormatter
│
└── diff/
    ├── DiffCommand.java
    ├── ConfigDiffer.java              # recursive structural diff of ServerConfig trees
    ├── model/                         # DiffReport, DiffEntry, ChangeType (ADDED/REMOVED/CHANGED)
    └── formatter/                     # DiffHumanFormatter, DiffJsonFormatter
```

## Key Design Patterns

### ConfigValue\<T\> — Provenance Tracking

The config subsystem wraps every parsed attribute in `ConfigValue<T>` to distinguish operator-set values from Tomcat defaults:
```java
ConfigValue.explicit(200)   // {"value": 200, "explicit": true}
ConfigValue.defaulted(200)  // {"value": 200, "explicit": false}
```
This lets SREs see what was actually configured vs. what Tomcat assumes.

### Rule Interface — Validation Rules

Every validation rule implements:
```java
public interface Rule {
    List<Finding> evaluate(RuleContext ctx);
}
```
Rules return `List.of()` for no findings. All check `ctx.getServerXml() == null` first.

Findings use the builder pattern:
```java
Finding.builder()
    .ruleId(RuleId.SEC_001)
    .category("Security")
    .severity(Severity.ERROR)
    .summary("...")
    .detail("...")
    .fix("...")
    .build();
```

### Dual Output — HUMAN / JSON

Every subcommand supports `--format human` (default) and `--format json`. Each has separate formatter classes. JSON output always includes `"schemaVersion": "1.0"`.

### Redaction Chain — Bundle Security

The bundle uses a chain-of-responsibility pattern:
- `Redactor` interface: `supports(CollectedFile)` + `redact(CollectedFile, BundleContext)`
- DEFAULT level: XML attributes + properties + log text
- STRICT level: additionally masks IPs, hostnames, env var references
- `StagingWriter` refuses to write unredacted files (throws `IllegalStateException`)

### XML Security

All XML parsers disable DTD and external entities (XXE protection):
```java
factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
```

## Validation Rules Reference

| Rule ID  | Category   | Severity | Check |
|----------|------------|----------|-------|
| SEC-001  | Security   | ERROR    | Running as root user |
| SEC-002  | Security   | ERROR    | Default credentials in tomcat-users.xml |
| SEC-003  | Security   | ERROR    | Shutdown port not disabled (not -1) |
| SEC-004  | Security   | WARN     | ErrorReportValve not hiding server info |
| SEC-005  | Security   | WARN     | HTTP TRACE method enabled |
| SEC-006  | Security   | INFO     | Connector not bound to localhost |
| SEC-007  | Security   | INFO     | StuckThreadDetectionValve missing or misconfigured |
| SEC-008  | Security   | WARN     | AccessLogValve not configured |
| SEC-009  | Security   | WARN     | GC log stress indicators found |
| TLS-001  | TLS        | WARN     | Deprecated TLS/SSL protocols enabled |
| TLS-002  | TLS        | ERROR    | Certificate expired |
| TLS-003  | TLS        | WARN     | SSL connector missing secure="true" |
| TLS-004  | TLS        | WARN     | SSL connector missing SSLHostConfig |
| TLS-005  | TLS        | ERROR    | Keystore file path does not exist |
| TLS-006  | TLS        | WARN/INFO| Weak or CBC cipher suites configured |
| TLS-007  | TLS        | WARN     | Certificate chain incomplete or self-signed |
| CONN-001 | Connector  | WARN     | maxThreads below CPU-based threshold |
| CONN-002 | Connector  | ERROR    | Multiple connectors on same port |
| CONN-003 | Connector  | WARN     | proxyName without proxyPort or vice versa |
| CONN-004 | Connector  | INFO     | Non-SSL connector missing redirectPort |
| CONN-005 | Connector  | WARN     | Obsolete APR connector protocol |
| CONN-006 | Connector  | WARN     | Port already in use |

## Exit Codes

| Code | Meaning |
|------|---------|
| 0    | OK — no issues (or operation succeeded) |
| 1    | WARNINGS — worst finding is WARN or INFO |
| 2    | ERRORS — at least one ERROR-severity finding |

Exit code is always determined by the highest severity finding.

## CATALINA_HOME / CATALINA_BASE Discovery

Resolution priority (first match wins):
1. `--catalina-home` / `--catalina-base` CLI flags
2. `CATALINA_HOME` / `CATALINA_BASE` environment variables
3. systemd override files (`/etc/sysconfig/tomcat`, `/etc/default/tomcat`)
4. Well-known paths (`/opt/rh/jws*/root/usr/share/tomcat`, `/usr/share/tomcat`, `/opt/tomcat`)
5. Running process detection via `/proc/*/cmdline`

CATALINA_BASE falls back to CATALINA_HOME if not set.

## Property Resolution Order (config command)

1. JVM system properties (`-Dprop=val`)
2. `catalina.properties` file
3. Environment variables (`${env.VAR_NAME}`)
4. VAULT tokens — preserved verbatim (never resolved)
5. Unresolved — kept as original `${...}` text

## Testing Conventions

### Structure
- **Test fixtures:** `src/test/resources/fixtures/<subsystem>/` — XML configs, log files, os-release files
- **Golden files:** `src/test/resources/golden/config/` — expected JSON and human output for parameterized tests
- **No mocking framework** — tests use real objects, `@TempDir`, and fixture files
- **Testability via injection:** constructors accept dependencies (e.g., `InstanceScanner(Path procRoot)`, `LogCollector(Clock clock)`, `LowThreadsCheckRule(IntSupplier cpuCount)`)

### Naming
- Test classes: `<ClassName>Test.java`
- Method names: mix of `should<Action>When<Condition>` and `<scenario>_<expected>` styles

### Key Patterns
- Validation rules: construct `RuleContext` directly with parsed XML `Document` fixtures
- Config parsing: golden output tests via `@ParameterizedTest @ValueSource` comparing against stored files
- Security: dedicated tests assert `***REDACTED***` appears and raw passwords never leak
- Bundle: redaction chain tested per-redactor; `StagingWriter` rejection tested

### Running Tests
```bash
mvn verify                  # full build + tests
mvn test                    # tests only
mvn test -pl . -Dtest=<TestClass>#<method>   # single test
```

## How To Add a New Validation Rule

1. **Create rule class** in `src/main/java/.../validate/rules/<category>/` implementing `Rule`
2. **Add enum value** to `RuleId.java` (e.g., `SEC_010("SEC-010")`)
3. **Register** the rule instance in `ValidationEngine.java`'s hardcoded `List.of(...)` — order: security, TLS, connector
4. **Add test fixtures** in `src/test/resources/fixtures/<category>/`
5. **Write tests** — both pass and fail scenarios
6. **Document** the rule in `docs/findings-to-fixes.md`

## How To Add a New Subcommand

1. **Create package** under `org.jboss.jws.diag.<name>/`
2. **Implement** `<Name>Command.java` as `@Command` + `Runnable` with `OutputFormatMixin`
3. **Register** in `Main.java`'s `@Command(subcommands = {...})`
4. **Create model, formatter, and parser/scanner** classes following existing patterns
5. **JSON output** must include `"schemaVersion": "1.0"`

## Security Invariants

- **Read-only:** never writes to the Tomcat installation, never sends network requests
- **Credential redaction:** attributes matching `password`, `secret`, `credential`, or ending with `pass` are replaced with `***REDACTED***`
- **VAULT tokens:** `${VAULT::...}` references are always preserved (they're already opaque)
- **XXE protection:** all XML parsers disable DTD and external entities
- **No privilege escalation:** works with whatever filesystem permissions the current user has
- **Bundle safety:** `StagingWriter` refuses to write files that haven't been through the redaction pipeline

## Dependencies

| Dependency | Version | Scope | Purpose |
|------------|---------|-------|---------|
| picocli | 4.7.6 | compile | CLI framework |
| jackson-databind | 2.17.2 | compile | JSON serialization |
| junit-jupiter | 5.11.0 | test | Test framework |
| assertj-core | 3.26.3 | test | Fluent assertions |

## Subsystem Independence

The **config** and **validate** subsystems are intentionally independent — they do not share a parser or data model. The validation engine parses raw XML with targeted XPath-style queries, while the config command builds a full structural model. This was a deliberate design decision (two GSoC students worked on them in parallel).

Cross-subsystem dependencies:
- `bundle` depends on `validate` (includes validation results in the bundle)
- `modcluster` and `logs` use `CatalinaDiscovery` from `summary`
- `diff` depends on `config` (reuses `ServerXmlParser` and config model)
- All subcommands use `common/` utilities

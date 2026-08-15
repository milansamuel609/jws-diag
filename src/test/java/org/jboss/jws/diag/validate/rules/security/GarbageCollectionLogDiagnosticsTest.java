package org.jboss.jws.diag.validate.rules.security;

import org.jboss.jws.diag.common.RuleId;
import org.jboss.jws.diag.common.Severity;
import org.jboss.jws.diag.validate.RuleContext;
import org.jboss.jws.diag.validate.model.Finding;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GarbageCollectionLogDiagnosticsTest {

    private final GarbageCollectionLogDiagnosticsRule rule = new GarbageCollectionLogDiagnosticsRule();

    private RuleContext ctx(Path catalinaBase) {
        return new RuleContext(catalinaBase, null, null, "testuser");
    }

    @Test
    void shouldPassWhenGcLogContainsNoWarningIndicators() {
        Path catalinaBase = Path.of("src/test/resources/fixtures/security/gc-logs");

        List<Finding> findings = rule.evaluate(ctx(catalinaBase));

        assertThat(findings).isEmpty();
    }

    @Test
    void shouldFlagWhenGcLogContainsWarningIndicators() {
        Path catalinaBase = Path.of("src/test/resources/fixtures/security/gc-warning-logs");

        List<Finding> findings = rule.evaluate(ctx(catalinaBase));

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).getRuleId()).isEqualTo(RuleId.SEC_009);
        assertThat(findings.get(0).getSeverity()).isEqualTo(Severity.WARN);
        assertThat(findings.get(0).getDetail()).contains("gc-warning.log");
    }

    @Test
    void shouldIgnoreNonGcLogFiles() {
        Path catalinaBase = Path.of("src/test/resources/fixtures/security/gc-non-gc-logs");

        List<Finding> findings = rule.evaluate(ctx(catalinaBase));

        assertThat(findings).isEmpty();
    }

    @Test
    void shouldPassWhenLogsDirectoryDoesNotExist() {
        Path catalinaBase = Path.of("src/test/resources/fixtures/security/gc-no-logs");

        List<Finding> findings = rule.evaluate(ctx(catalinaBase));

        assertThat(findings).isEmpty();
    }
}
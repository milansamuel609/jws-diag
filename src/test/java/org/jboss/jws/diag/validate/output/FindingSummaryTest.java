package org.jboss.jws.diag.validate.output;

import org.jboss.jws.diag.common.RuleId;
import org.jboss.jws.diag.common.Severity;
import org.jboss.jws.diag.validate.model.Finding;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FindingSummaryTest {

    @Test
    void shouldReturnZeroCountsWhenFindingsAreEmpty() {
        FindingSummary summary = new FindingSummary(Collections.emptyList());

        assertThat(summary.getErrors()).isEqualTo(0);
        assertThat(summary.getWarnings()).isEqualTo(0);
        assertThat(summary.getInfo()).isEqualTo(0);
    }

    @Test
    void shouldCountErrorsCorrectly() {
        List<Finding> findings = List.of(
                Finding.builder()
                        .ruleId(RuleId.SEC_001)
                        .category("Security")
                        .severity(Severity.ERROR)
                        .summary("Root User Check")
                        .detail("Checks if the Tomcat process is running as root (UID 0)")
                        .file("Process State")
                        .fix("Run Tomcat as a dedicated, non-root system user")
                        .build(),
                Finding.builder()
                        .ruleId(RuleId.SEC_002)
                        .category("Security")
                        .severity(Severity.ERROR)
                        .summary("Default Credentials Detected")
                        .detail("Checks for known default username/password pairs (like tomcat/tomcat, admin/admin)")
                        .file("tomcat-users.xml")
                        .fix("Change the default passwords or remove the default accounts entirely")
                        .build()
        );

        FindingSummary summary = new FindingSummary(findings);

        assertThat(summary.getErrors()).isEqualTo(2);
        assertThat(summary.getWarnings()).isEqualTo(0);
        assertThat(summary.getInfo()).isEqualTo(0);
    }

    @Test
    void shouldCountWarningsCorrectly() {
        List<Finding> findings = List.of(
                Finding.builder()
                        .ruleId(RuleId.SEC_004)
                        .category("Security")
                        .severity(Severity.WARN)
                        .summary("Version Banner Exposure Check")
                        .detail("Checks if <Connector> elements expose server metadata, or if an <ErrorReportValve> is missing inside the <Host> or <Engine> blocks to suppress versions on error pages")
                        .file("server.xml")
                        .fix("Configure an <ErrorReportValve> with showReport=\"false\" and showServerInfo=\"false\" inside your Host block")
                        .build(),
                Finding.builder()
                        .ruleId(RuleId.SEC_005)
                        .category("Security")
                        .severity(Severity.WARN)
                        .summary("HTTP TRACE Enabled")
                        .detail("Checks if the HTTP TRACE method is allowed, which can leave it open to tracing attacks")
                        .file("server.xml")
                        .fix("Set allowTrace=\"false\" on your active connectors")
                        .build()
        );

        FindingSummary summary = new FindingSummary(findings);

        assertThat(summary.getErrors()).isEqualTo(0);
        assertThat(summary.getWarnings()).isEqualTo(2);
        assertThat(summary.getInfo()).isEqualTo(0);
    }

    @Test
    void shouldCountInfoCorrectly() {
        List<Finding> findings = List.of(
                Finding.builder()
                        .ruleId(RuleId.SEC_006)
                        .category("Security")
                        .severity(Severity.INFO)
                        .summary("Localhost Binding")
                        .detail("Checks if the connector address attribute is restricted to localhost (127.0.0.1)")
                        .file("server.xml")
                        .fix("If you want the server accessible to the public, change address to 0.0.0.0")
                        .build()
        );

        FindingSummary summary = new FindingSummary(findings);

        assertThat(summary.getErrors()).isEqualTo(0);
        assertThat(summary.getWarnings()).isEqualTo(0);
        assertThat(summary.getInfo()).isEqualTo(1);
    }

    @Test
    void shouldCountMixedSeveritiesCorrectly() {
        List<Finding> findings = List.of(
                Finding.builder()
                        .ruleId(RuleId.SEC_001)
                        .category("Security")
                        .severity(Severity.ERROR)
                        .summary("Root User Check")
                        .detail("Checks if the Tomcat process is running as root (UID 0)")
                        .file("Process State")
                        .fix("Run Tomcat as a dedicated, non-root system user")
                        .build(),
                Finding.builder()
                        .ruleId(RuleId.SEC_004)
                        .category("Security")
                        .severity(Severity.WARN)
                        .summary("Version Banner Exposure Check")
                        .detail("Checks if <Connector> elements expose server metadata, or if an <ErrorReportValve> is missing inside the <Host> or <Engine> blocks to suppress versions on error pages")
                        .file("server.xml")
                        .fix("Configure an <ErrorReportValve> with showReport=\"false\" and showServerInfo=\"false\" inside your Host block")
                        .build(),
                Finding.builder()
                        .ruleId(RuleId.SEC_006)
                        .category("Security")
                        .severity(Severity.INFO)
                        .summary("Localhost Binding")
                        .detail("Checks if the connector address attribute is restricted to localhost (127.0.0.1)")
                        .file("server.xml")
                        .fix("If you want the server accessible to the public, change address to 0.0.0.0")
                        .build()
        );

        FindingSummary summary = new FindingSummary(findings);

        assertThat(summary.getErrors()).isEqualTo(1);
        assertThat(summary.getWarnings()).isEqualTo(1);
        assertThat(summary.getInfo()).isEqualTo(1);
    }
}

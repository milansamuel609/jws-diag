package org.jboss.jws.diag.validate.output;

import org.jboss.jws.diag.common.RuleId;
import org.jboss.jws.diag.common.Severity;
import org.jboss.jws.diag.validate.model.Finding;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HumanReadableOutputTest {

    private final HumanReadableOutput output = new HumanReadableOutput();
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private PrintStream originalOut;

    @BeforeEach
    public void setUpStreams() {
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void tearDownStreams() {
        System.setOut(originalOut);
    }

    @Test
    void shouldPrintNoIssuesFoundWhenFindingsAreEmpty() {
        output.print(Collections.emptyList());

        assertThat(outContent.toString()).contains("No issues found");
    }

    @Test
    void shouldPrintErrorSectionWhenErrorFindingExists() {
        List<Finding> findings = List.of(
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

        output.print(findings);
        String result = outContent.toString();

        assertThat(result).contains("ERROR");
        assertThat(result).contains("SEC-002");
        assertThat(result).contains("Default Credentials Detected");
        assertThat(result).contains("tomcat-users.xml");
        assertThat(result).contains("Summary: 1 error(s), 0 warning(s), 0 info(s)");
    }

    @Test
    void shouldPrintWarningSectionWhenWarningFindingExists() {
        List<Finding> findings = List.of(
                Finding.builder()
                        .ruleId(RuleId.SEC_004)
                        .category("Security")
                        .severity(Severity.WARN)
                        .summary("Version Banner Exposure Check")
                        .detail("Checks if <Connector> elements expose server metadata, or if an <ErrorReportValve> is missing inside the <Host> or <Engine> blocks to suppress versions on error pages")
                        .file("server.xml")
                        .fix("Configure an <ErrorReportValve> with showReport=\"false\" and showServerInfo=\"false\" inside your Host block")
                        .build()
        );

        output.print(findings);
        String result = outContent.toString();

        assertThat(result).contains("WARN");
        assertThat(result).contains("SEC-004");
        assertThat(result).contains("Version Banner Exposure Check");
        assertThat(result).contains("server.xml");
        assertThat(result).contains("Summary: 0 error(s), 1 warning(s), 0 info(s)");
    }

    @Test
    void shouldPrintInfoSectionWhenInfoFindingExists() {
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

        output.print(findings);
        String result = outContent.toString();

        assertThat(result).contains("INFO");
        assertThat(result).contains("SEC-006");
        assertThat(result).contains("Localhost Binding");
        assertThat(result).contains("server.xml");
        assertThat(result).contains("Summary: 0 error(s), 0 warning(s), 1 info(s)");
    }

    @Test
    void shouldPrintSummaryWithCorrectCountsForMixedSeverities() {
        List<Finding> findings = List.of(
                Finding.builder()
                        .ruleId(RuleId.SEC_002)
                        .category("Security")
                        .severity(Severity.ERROR)
                        .summary("Default Credentials Detected")
                        .detail("Checks for known default username/password pairs (like tomcat/tomcat, admin/admin)")
                        .file("tomcat-users.xml")
                        .fix("Change the default passwords or remove the default accounts entirely")
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

        output.print(findings);
        String result = outContent.toString();

        assertThat(result).contains("Summary: 1 error(s), 1 warning(s), 1 info(s)");
    }
}

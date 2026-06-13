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

public class JsonOutputTest {
    private final JsonOutput output = new JsonOutput();
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
    void shouldPrintEmptyFindingsArrayWhenNoFindingsArePresent() {
        output.print(Collections.emptyList(), 0);
        String result = outContent.toString();

        assertThat(result).contains("\"findings\"");
        assertThat(result).contains("\"errors\" : 0");
        assertThat(result).contains("\"warnings\" : 0");
        assertThat(result).contains("\"info\" : 0");
        assertThat(result).contains("\"exitCode\" : 0");
    }

    @Test
    void shouldContainCorrectFindingFieldsInJsonOutput() {
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

        output.print(findings, 2);
        String result = outContent.toString();

        assertThat(result).contains("\"ruleId\"");
        assertThat(result).contains("\"SEC-002\"");
        assertThat(result).contains("\"category\"");
        assertThat(result).contains("Security");
        assertThat(result).contains("\"severity\"");
        assertThat(result).contains("ERROR");
        assertThat(result).contains("\"summary\"");
        assertThat(result).contains("Default Credentials Detected");
        assertThat(result).contains("\"file\"");
        assertThat(result).contains("tomcat-users.xml");
    }

    @Test
    void shouldContainCorrectSeverityCountsInJsonOutput() {
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

        output.print(findings, 2);
        String result = outContent.toString();

        assertThat(result).contains("\"errors\" : 1");
        assertThat(result).contains("\"warnings\" : 1");
        assertThat(result).contains("\"info\" : 1");
        assertThat(result).contains("\"exitCode\" : 2");
    }

    @Test
    void shouldContainCorrectExitCodeInJsonOutput() {
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

        output.print(findings, 2);
        String result = outContent.toString();

        assertThat(result).contains("\"exitCode\" : 2");
    }
}
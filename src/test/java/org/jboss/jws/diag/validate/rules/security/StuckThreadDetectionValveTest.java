package org.jboss.jws.diag.validate.rules.security;

import org.jboss.jws.diag.common.RuleId;
import org.jboss.jws.diag.common.Severity;
import org.jboss.jws.diag.validate.RuleContext;
import org.jboss.jws.diag.validate.model.Finding;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StuckThreadDetectionValveTest {

    private final StuckThreadDetectionValveRule rule = new StuckThreadDetectionValveRule();

    private Document parseFixture(String resourcePath) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(getClass().getResourceAsStream(resourcePath));
    }

    @Test
    void shouldPassWhenStuckThreadDetectionValveIsCorrectlyConfigured() throws Exception {
        Document serverXml = parseFixture("/fixtures/security/server-stuck-thread-correct.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        assertThat(rule.evaluate(ctx)).isEmpty();
    }

    @Test
    void shouldFlagWhenStuckThreadDetectionValveIsMissing() throws Exception {
        Document serverXml = parseFixture("/fixtures/security/server-stuck-thread-missing.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).getRuleId()).isEqualTo(RuleId.SEC_007);
        assertThat(findings.get(0).getSeverity()).isEqualTo(Severity.INFO);
    }

    @Test
    void shouldUseDefaultThresholdWhenThresholdIsMissing() throws Exception {
        Document serverXml = parseFixture("/fixtures/security/server-stuck-thread-threshold-missing.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        assertThat(rule.evaluate(ctx)).isEmpty();
    }

    @Test
    void shouldUseDefaultInterruptThreadThresholdWhenMissing() throws Exception {
        Document serverXml = parseFixture("/fixtures/security/server-stuck-thread-interrupt-missing.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        assertThat(rule.evaluate(ctx)).isEmpty();
    }

    @Test
    void shouldFlagWhenThresholdIsNotHigherThanBackgroundProcessorDelay() throws Exception {
        Document serverXml = parseFixture("/fixtures/security/server-stuck-thread-invalid-threshold.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).getRuleId()).isEqualTo(RuleId.SEC_007);
        assertThat(findings.get(0).getSeverity()).isEqualTo(Severity.INFO);
    }

    @Test
    void shouldFlagWhenInterruptThreadThresholdIsBelowThreshold() throws Exception {
        Document serverXml = parseFixture("/fixtures/security/server-stuck-thread-invalid-interrupt.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).getRuleId()).isEqualTo(RuleId.SEC_007);
        assertThat(findings.get(0).getSeverity()).isEqualTo(Severity.INFO);
    }

    @Test
    void shouldPassWhenThresholdIsDisabled() throws Exception {
        Document serverXml = parseFixture("/fixtures/security/server-stuck-thread-disabled.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        assertThat(rule.evaluate(ctx)).isEmpty();
    }

    @Test
    void shouldFlagAllInvalidStuckThreadDetectionValves() throws Exception {
        Document serverXml = parseFixture("/fixtures/security/server-stuck-thread-multiple-invalid.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).hasSize(2);
        assertThat(findings).allMatch(finding -> finding.getRuleId().equals(RuleId.SEC_007));
        assertThat(findings).allMatch(finding -> finding.getSeverity().equals(Severity.INFO));
    }

    @Test
    void shouldPassWhenServerXmlIsNull() {
        RuleContext ctx = new RuleContext(Path.of("/dummy"), null, null,
                "testuser");

        assertThat(rule.evaluate(ctx)).isEmpty();
    }
}
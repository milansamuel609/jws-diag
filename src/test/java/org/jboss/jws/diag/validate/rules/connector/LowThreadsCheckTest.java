package org.jboss.jws.diag.validate.rules.connector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.jws.diag.common.RuleId;
import org.jboss.jws.diag.common.Severity;
import org.jboss.jws.diag.validate.RuleContext;
import org.jboss.jws.diag.validate.model.Finding;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LowThreadsCheckTest {

    private final LowThreadsCheckRule rule = new LowThreadsCheckRule(() -> 4);

    private Document parseFixture(String resourcePath) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(getClass().getResourceAsStream(resourcePath));
    }

    @Test
    void shouldPassWhenMaxThreadsMeetsOrExceedsRecommendedMinimum() throws Exception {
        Document serverXml = parseFixture("/fixtures/connector/server-pass-max-threads.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        assertThat(rule.evaluate(ctx)).isEmpty();
    }

    @Test
    void shouldPassWhenMaxThreadsExactlyEqualsRecommendedMinimum() throws Exception {
        Document serverXml = parseFixture("/fixtures/connector/server-max-threads-boundary.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        assertThat(rule.evaluate(ctx)).isEmpty();
    }

    @Test
    void shouldFlagWhenMaxThreadsIsLessThanRecommendedMinimum() throws Exception {
        Document serverXml = parseFixture("/fixtures/connector/server-low-max-threads.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).getRuleId()).isEqualTo(RuleId.CONN_001);
        assertThat(findings.get(0).getSeverity()).isEqualTo(Severity.WARN);
        assertThat(findings.get(0).getDetail()).contains("less than the recommended minimum");
    }

    @Test
    void shouldPassWhenMaxThreadsAttributeIsMissing() throws Exception {
        Document serverXml = parseFixture("/fixtures/connector/server-no-max-threads.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        assertThat(rule.evaluate(ctx)).isEmpty();
    }

    @Test
    void shouldPassWhenServerXmlIsNull() {
        RuleContext ctx = new RuleContext(Path.of("/dummy"), null, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).isEmpty();
    }
}
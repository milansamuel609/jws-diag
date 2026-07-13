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

public class MissingRedirectPortTest {

    private final MissingRedirectPortRule rule =  new MissingRedirectPortRule();

    private Document parseFixture(String resourcePath) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(getClass().getResourceAsStream(resourcePath));
    }

    @Test
    void shouldPassWhenRedirectPortIsPresent() throws Exception {
        Document serverXml = parseFixture("/fixtures/connector/server-redirect-port-present.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        assertThat(rule.evaluate(ctx)).isEmpty();
    }

    @Test
    void shouldFlagWhenRedirectPortIsMissing() throws Exception {
        Document serverXml = parseFixture("/fixtures/connector/server-redirect-port-missing.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).getRuleId()).isEqualTo(RuleId.CONN_004);
        assertThat(findings.get(0).getSeverity()).isEqualTo(Severity.INFO);
        assertThat(findings.get(0).getDetail()).contains("redirectPort is missing");
    }

    @Test
    void shouldFlagWhenRedirectPortIsEmpty() throws Exception {
        Document serverXml = parseFixture("/fixtures/connector/server-redirect-port-empty.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).getRuleId()).isEqualTo(RuleId.CONN_004);
        assertThat(findings.get(0).getSeverity()).isEqualTo(Severity.INFO);
        assertThat(findings.get(0).getDetail()).contains("redirectPort is missing");
    }

    @Test
    void shouldFlagWhenSslEnabledAndRedirectPortIsMissing() throws Exception {
        Document serverXml = parseFixture("/fixtures/connector/server-ssl-no-redirect-port.xml");
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

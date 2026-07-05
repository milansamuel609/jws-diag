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

public class ProxyMismatchTest {

    private final ProxyMismatchRule rule = new ProxyMismatchRule();

    private Document parseFixture(String resourcePath) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(getClass().getResourceAsStream(resourcePath));
    }

    @Test
    void shouldPassWhenBothAttributesAreDefined() throws Exception {
        Document serverXml = parseFixture("/fixtures/security/server-clean.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        assertThat(rule.evaluate(ctx)).isEmpty();
    }

    @Test
    void shouldFlagWhenProxyNameIsPresentButProxyPortIsMissing() throws Exception {
        Document serverXml = parseFixture("/fixtures/connector/server-proxy-port-missing.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).getRuleId()).isEqualTo(RuleId.CONN_003);
        assertThat(findings.get(0).getSeverity()).isEqualTo(Severity.WARN);
        assertThat(findings.get(0).getDetail()).contains("proxyName");
        assertThat(findings.get(0).getDetail()).contains("proxyPort is missing");
    }

    @Test
    void shouldFlagWhenProxyPortIsPresentButProxyNameIsMissing() throws Exception {
        Document serverXml = parseFixture("/fixtures/connector/server-proxy-name-missing.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).getRuleId()).isEqualTo(RuleId.CONN_003);
        assertThat(findings.get(0).getSeverity()).isEqualTo(Severity.WARN);
        assertThat(findings.get(0).getDetail()).contains("proxyPort");
        assertThat(findings.get(0).getDetail()).contains("proxyName is missing");
    }

    @Test
    void shouldFlagWhenProxyNameIsPresentButProxyPortIsEmpty() throws Exception {
        Document serverXml = parseFixture("/fixtures/connector/server-proxy-port-empty.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).getRuleId()).isEqualTo(RuleId.CONN_003);
        assertThat(findings.get(0).getSeverity()).isEqualTo(Severity.WARN);
        assertThat(findings.get(0).getDetail()).contains("proxyName");
        assertThat(findings.get(0).getDetail()).contains("proxyPort is missing");
    }

    @Test
    void shouldFlagWhenProxyPortIsPresentButProxyNameIsEmpty() throws Exception {
        Document serverXml = parseFixture("/fixtures/connector/server-proxy-name-empty.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).getRuleId()).isEqualTo(RuleId.CONN_003);
        assertThat(findings.get(0).getSeverity()).isEqualTo(Severity.WARN);
        assertThat(findings.get(0).getDetail()).contains("proxyPort");
        assertThat(findings.get(0).getDetail()).contains("proxyName is missing");
    }

    @Test
    void shouldPassWhenServerXmlIsNull() {
        RuleContext ctx = new RuleContext(Path.of("/dummy"), null, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).isEmpty();
    }
}

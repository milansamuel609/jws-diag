package org.jboss.jws.diag.validate.rules.tls;

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

public class DeprecatedProtocolsTest {

    private final DeprecatedProtocolsRule rule = new DeprecatedProtocolsRule();

    private Document parseFixture(String resourcePath) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(getClass().getResourceAsStream(resourcePath));
    }

    @Test
    void shouldPassWhenNoDeprecatedProtocolsArePresent() throws Exception {
        Document serverXml = parseFixture("/fixtures/security/server-clean.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testUser");

        assertThat(rule.evaluate(ctx)).isEmpty();
    }

    @Test
    void shouldFlagWhenDeprecatedProtocolsArePresent() throws Exception {
        Document serverXml = parseFixture("/fixtures/tls/server-deprecated-tls.xml");
        RuleContext ctx = new RuleContext(Path.of("/dummy"), serverXml, null, "testUser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).getRuleId()).isEqualTo(RuleId.TLS_001);
        assertThat(findings.get(0).getSeverity()).isEqualTo(Severity.WARN);
    }

    @Test
    void shouldPassWhenServerXmlIsNull() {
        RuleContext ctx = new RuleContext(Path.of("/dummy"), null, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).isEmpty();
    }
}

package org.jboss.jws.diag.validate.rules.tls;

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

public class CertificateChainVerificationTest {

    private final CertificateChainVerificationRule rule = new CertificateChainVerificationRule();

    private Document parseFixture(String resourcePath) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(getClass().getResourceAsStream(resourcePath));
    }

    @Test
    void shouldPassWhenJksCertificateChainIsValid() throws Exception {
        Path catalinaBase = Path.of("src/test/resources/fixtures/tls/tls-chain-keystores");
        Document serverXml = parseFixture("/fixtures/tls/server-cert-chain-valid-complete-jks.xml");
        RuleContext ctx = new RuleContext(catalinaBase, serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).isEmpty();
    }

    @Test
    void shouldFlagWhenJksIntermediateCertificateIsMissing() throws Exception {
        Path catalinaBase = Path.of("src/test/resources/fixtures/tls/tls-chain-keystores");
        Document serverXml = parseFixture(
                "/fixtures/tls/server-cert-chain-missing-intermediate-jks.xml");
        RuleContext ctx = new RuleContext(catalinaBase, serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).getRuleId()).isEqualTo(RuleId.TLS_007);
        assertThat(findings.get(0).getSeverity()).isEqualTo(Severity.WARN);
        assertThat(findings.get(0).getDetail()).contains("missing intermediate");
    }

    @Test
    void shouldPassWhenPkcs12CertificateChainIsValid() throws Exception {
        Path catalinaBase = Path.of("src/test/resources/fixtures/tls/tls-chain-keystores");
        Document serverXml = parseFixture("/fixtures/tls/server-cert-chain-valid-complete-pkcs12.xml");
        RuleContext ctx = new RuleContext(catalinaBase, serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).isEmpty();
    }

    @Test
    void shouldFlagWhenPkcs12IntermediateCertificateIsMissing() throws Exception {
        Path catalinaBase = Path.of("src/test/resources/fixtures/tls/tls-chain-keystores");
        Document serverXml = parseFixture("/fixtures/tls/server-cert-chain-missing-intermediate-pkcs12.xml");
        RuleContext ctx = new RuleContext(catalinaBase, serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).getRuleId()).isEqualTo(RuleId.TLS_007);
        assertThat(findings.get(0).getSeverity()).isEqualTo(Severity.WARN);
        assertThat(findings.get(0).getDetail()).contains("missing intermediate");
    }

    @Test
    void shouldPassWhenCertificateIsSelfSigned() throws Exception {
        Path catalinaBase = Path.of("src/test/resources/fixtures/tls/keystores");
        Document serverXml = parseFixture("/fixtures/tls/server-cert-chain-self-signed-jks.xml");
        RuleContext ctx = new RuleContext(catalinaBase, serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).isEmpty();
    }

    @Test
    void shouldFlagAllInvalidCertificateChains() throws Exception {
        Path catalinaBase = Path.of("src/test/resources/fixtures/tls/tls-chain-keystores");
        Document serverXml = parseFixture(
                "/fixtures/tls/server-cert-chain-multiple.xml");
        RuleContext ctx = new RuleContext(catalinaBase, serverXml, null, "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).hasSize(2);
        assertThat(findings)
                .allMatch(finding -> finding.getRuleId().equals(RuleId.TLS_007));
        assertThat(findings)
                .allMatch(finding -> finding.getSeverity().equals(Severity.WARN));
    }

    @Test
    void shouldPassWhenServerXmlIsNull() {
        RuleContext ctx = new RuleContext(Path.of("/dummy"), null, null,
                "testuser"
        );

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).isEmpty();
    }
}
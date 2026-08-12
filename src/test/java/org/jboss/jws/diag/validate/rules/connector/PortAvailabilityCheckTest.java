package org.jboss.jws.diag.validate.rules.connector;

import org.jboss.jws.diag.common.RuleId;
import org.jboss.jws.diag.common.Severity;
import org.jboss.jws.diag.validate.RuleContext;
import org.jboss.jws.diag.validate.model.Finding;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PortAvailabilityCheckTest {

    private final PortAvailabilityCheckRule rule = new PortAvailabilityCheckRule();

    private Document parseFixture(String resourcePath) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        DocumentBuilder db = dbf.newDocumentBuilder();

        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            return db.parse(inputStream);
        }
    }

    private Document parseXml(String xml) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        return db.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private RuleContext contextFor(Document serverXml) {
        return new RuleContext(Path.of("/dummy"), serverXml, null, "testuser");
    }

    @Test
    void shouldPassWhenPortIsAvailable() throws Exception {
        int port;

        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }

        String serverXml = String.format(
                "<Server port=\"-1\" shutdown=\"SHUTDOWN\">"
                        + "<Service name=\"Catalina\">"
                        + "<Connector port=\"%d\"/>"
                        + "</Service>"
                        + "</Server>",
                port);

        Document document = parseXml(serverXml);

        List<Finding> findings = rule.evaluate(contextFor(document));

        assertThat(findings).isEmpty();
    }

    @Test
    void shouldFlagWhenPortIsAlreadyInUse() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {

            int port = socket.getLocalPort();

            String serverXml = String.format(
                    "<Server port=\"-1\" shutdown=\"SHUTDOWN\">"
                            + "<Service name=\"Catalina\">"
                            + "<Connector port=\"%d\"/>"
                            + "</Service>"
                            + "</Server>",
                    port);

            Document document = parseXml(serverXml);

            List<Finding> findings = rule.evaluate(contextFor(document));

            assertThat(findings).hasSize(1);
            assertThat(findings.get(0).getRuleId()).isEqualTo(RuleId.CONN_006);
            assertThat(findings.get(0).getSeverity()).isEqualTo(Severity.WARN);
            assertThat(findings.get(0).getDetail()).contains(String.valueOf(port));
        }
    }

    @Test
    void shouldIgnoreConnectorWithoutPort() throws Exception {
        Document serverXml = parseFixture("/fixtures/connector/server-port-missing.xml");

        List<Finding> findings = rule.evaluate(contextFor(serverXml));

        assertThat(findings).isEmpty();
    }

    @Test
    void shouldIgnoreConnectorWithInvalidPort() throws Exception {
        Document serverXml = parseFixture("/fixtures/connector/server-port-invalid.xml");

        List<Finding> findings = rule.evaluate(contextFor(serverXml));

        assertThat(findings).isEmpty();
    }

    @Test
    void shouldIgnoreDisabledConnector() throws Exception {
        Document serverXml =
                parseFixture("/fixtures/connector/server-port-disabled.xml");

        List<Finding> findings = rule.evaluate(contextFor(serverXml));

        assertThat(findings).isEmpty();
    }

    @Test
    void shouldFlagWhenConfiguredAddressIsAlreadyInUse() throws Exception {
        int port;

        try (ServerSocket probe = new ServerSocket(0)) {
            port = probe.getLocalPort();
        }

        try (ServerSocket socket = new ServerSocket()) {
            socket.bind(new InetSocketAddress("127.0.0.1", port));

            Document serverXml =
                    parseFixture("/fixtures/connector/server-port-address.xml");

            // The fixture uses port 8080, so replace it with the occupied port.
            serverXml.getElementsByTagName("Connector")
                    .item(0)
                    .getAttributes()
                    .getNamedItem("port")
                    .setNodeValue(String.valueOf(port));

            List<Finding> findings = rule.evaluate(contextFor(serverXml));

            assertThat(findings).hasSize(1);
            assertThat(findings.get(0).getRuleId()).isEqualTo(RuleId.CONN_006);
            assertThat(findings.get(0).getDetail()).contains(String.valueOf(port));
        }
    }

    @Test
    void shouldFlagAllOccupiedPorts() throws Exception {
        try (ServerSocket socket1 = new ServerSocket(0);
             ServerSocket socket2 = new ServerSocket(0)) {

            int port1 = socket1.getLocalPort();
            int port2 = socket2.getLocalPort();

            String serverXml = String.format(
                    "<Server port=\"-1\" shutdown=\"SHUTDOWN\">"
                            + "<Service name=\"Catalina\">"
                            + "<Connector port=\"%d\"/>"
                            + "<Connector port=\"%d\"/>"
                            + "</Service>"
                            + "</Server>",
                    port1, port2);

            Document document = parseXml(serverXml);

            List<Finding> findings = rule.evaluate(contextFor(document));

            assertThat(findings).hasSize(2);
            assertThat(findings)
                    .allMatch(finding ->
                            finding.getRuleId().equals(RuleId.CONN_006));
            assertThat(findings)
                    .allMatch(finding -> finding.getSeverity().equals(Severity.WARN));
        }
    }

    @Test
    void shouldPassWhenServerXmlIsNull() {
        RuleContext ctx = new RuleContext(Path.of("/dummy"), null, null,
                "testuser");

        List<Finding> findings = rule.evaluate(ctx);

        assertThat(findings).isEmpty();
    }
}
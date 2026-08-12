package org.jboss.jws.diag.validate.rules.connector;

import org.jboss.jws.diag.common.RuleId;
import org.jboss.jws.diag.common.Severity;
import org.jboss.jws.diag.validate.Rule;
import org.jboss.jws.diag.validate.RuleContext;
import org.jboss.jws.diag.validate.model.Finding;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import java.util.ArrayList;
import java.util.List;

public class PortAvailabilityCheckRule implements Rule {

    @Override
    public List<Finding> evaluate(RuleContext ctx) {
        Document doc = ctx.getServerXml();

        if (doc == null) {
            return List.of();
        }

        NodeList connectors = doc.getElementsByTagName("Connector");
        List<Finding> findings = new ArrayList<>();

        for (int i = 0; i < connectors.getLength(); i++) {
            Node connector = connectors.item(i);

            Node portAttr = connector.getAttributes().getNamedItem("port");
            Node addressAttr = connector.getAttributes().getNamedItem("address");

            if (portAttr == null) {
                continue;
            }

            int port;

            try {
                port = Integer.parseInt(portAttr.getNodeValue().trim());
            } catch (NumberFormatException e) {
                continue;
            }

            if (port < 0) {
                continue;
            }

            String address = addressAttr != null ? addressAttr.getNodeValue().trim() : "0.0.0.0";

            try (ServerSocket socket = new ServerSocket()) {
                socket.bind(new InetSocketAddress(address, port));
                // Port is available.
            } catch (IOException e) {
                findings.add(Finding.builder()
                        .ruleId(RuleId.CONN_006)
                        .category("Connector")
                        .severity(Severity.WARN)
                        .summary("Port Availability Check")
                        .detail("Port " + port + " is already used by another process.")
                        .file("Process State")
                        .fix("Change the Tomcat port in server.xml or stop the process using the port.")
                        .build());
            }

        }

        return findings;
    }
}

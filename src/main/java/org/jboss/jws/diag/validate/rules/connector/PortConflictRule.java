package org.jboss.jws.diag.validate.rules.connector;

import org.jboss.jws.diag.validate.Rule;
import org.jboss.jws.diag.validate.RuleContext;
import org.jboss.jws.diag.validate.model.Finding;
import org.jboss.jws.diag.common.RuleId;
import org.jboss.jws.diag.common.Severity;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class PortConflictRule implements Rule {

    @Override
    public List<Finding> evaluate(RuleContext ctx) {
        Document doc = ctx.getServerXml();

        if (doc == null) {
            return List.of();
        }

        NodeList connectors = doc.getElementsByTagName("Connector");
        List<Finding> findings = new ArrayList<>();

        List<String> seenPorts = new ArrayList<>();
        List<String> duplicatePorts = new ArrayList<>();

        for (int i = 0; i < connectors.getLength(); i++) {
            Node connector = connectors.item(i);
            Node portAttr = connector.getAttributes().getNamedItem("port");

            if (portAttr == null) continue;

            String port = portAttr.getNodeValue();

            if (seenPorts.contains(port)) {
                if (!duplicatePorts.contains(port)) {
                    duplicatePorts.add(port);
                }
            } else {
                seenPorts.add(port);
            }
        }

        for (String port : duplicatePorts) {
            findings.add(Finding.builder()
                    .ruleId(RuleId.CONN_002)
                    .category("Connector")
                    .severity(Severity.ERROR)
                    .summary("Port Conflict")
                    .detail("Multiple connectors are bound to " + port)
                    .file("server.xml")
                    .fix("Assign unique, non-overlapping port numbers to each connector block")
                    .build());
        }

        return findings;
    }
}

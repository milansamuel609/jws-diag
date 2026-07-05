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

public class ProxyMismatchRule implements Rule {

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
            Node proxyNameAttr = connector.getAttributes().getNamedItem("proxyName");
            Node proxyPortAttr = connector.getAttributes().getNamedItem("proxyPort");

            String proxyName = (proxyNameAttr != null) ? proxyNameAttr.getNodeValue() : null;
            String proxyPort = (proxyPortAttr != null) ? proxyPortAttr.getNodeValue() : null;

            boolean namePresent = proxyName != null && !proxyName.trim().isEmpty();
            boolean portPresent = proxyPort != null && !proxyPort.trim().isEmpty();

            if (namePresent && !portPresent) {
                findings.add(Finding.builder()
                        .ruleId(RuleId.CONN_003)
                        .category("Connector")
                        .severity(Severity.WARN)
                        .summary("Proxy Mismatch")
                        .detail("proxyName (" + proxyName + ") is defined but proxyPort is missing")
                        .file("server.xml")
                        .fix("Define the proxyPort for the created proxyName")
                        .build());
            } else if (portPresent && !namePresent) {
                findings.add(Finding.builder()
                        .ruleId(RuleId.CONN_003)
                        .category("Connector")
                        .severity(Severity.WARN)
                        .summary("Proxy Mismatch")
                        .detail("proxyPort (" + proxyPort + ") is defined but proxyName is missing")
                        .file("server.xml")
                        .fix("Define the proxyName for the created proxyPort")
                        .build());
            }
        }

        return findings;
    }
}

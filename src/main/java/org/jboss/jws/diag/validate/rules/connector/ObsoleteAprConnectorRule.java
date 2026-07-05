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

public class ObsoleteAprConnectorRule implements Rule {

    private static final String APR_CONN = "org.apache.coyote.http11.Http11AprProtocol";

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
            Node protocolAttr = connector.getAttributes().getNamedItem("protocol");

            if (protocolAttr == null) continue;

            String protocol = protocolAttr.getNodeValue();

            if (APR_CONN.equals(protocol)) {
                findings.add(Finding.builder()
                        .ruleId(RuleId.CONN_005)
                        .category("Connector")
                        .severity(Severity.WARN)
                        .summary("Obsolete APR Connector")
                        .detail("The protocol " + protocol + " is obsolete")
                        .file("server.xml")
                        .fix("Remove legacy APR tags and transition to native NIO or NIO2 configurations")
                        .build());
            }
        }

        return findings;
    }
}

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

public class MissingRedirectPortRule implements Rule {

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
            Node redirectPortAttr = connector.getAttributes().getNamedItem("redirectPort");

            String redirectPort = (redirectPortAttr != null) ? redirectPortAttr.getNodeValue() : null;
            boolean redirectPortPresent = redirectPort != null && !redirectPort.trim().isEmpty();

            if (!redirectPortPresent) {
                findings.add(Finding.builder()
                        .ruleId(RuleId.CONN_004)
                        .category("Connector")
                        .severity(Severity.INFO)
                        .summary("Missing Redirect Port")
                        .detail("The redirectPort is missing")
                        .file("server.xml")
                        .fix("Add redirectPort=\"8443\" to allow automatic HTTPS redirection fields")
                        .build());
            }
        }

        return findings;
    }
}

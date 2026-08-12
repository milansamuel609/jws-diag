package org.jboss.jws.diag.validate.rules.security;

import org.jboss.jws.diag.validate.Rule;
import org.jboss.jws.diag.validate.RuleContext;
import org.jboss.jws.diag.validate.model.Finding;
import org.jboss.jws.diag.common.RuleId;
import org.jboss.jws.diag.common.Severity;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class LocalhostBindingRule implements Rule {

    @Override
    public List<Finding> evaluate(RuleContext ctx) {
        Document doc = ctx.getServerXml();

        if (doc == null) {
            return List.of();
        }

        NodeList connectors = doc.getElementsByTagName("Connector");
        List<Finding> findings = new ArrayList<>();

        for (int i = 0; i < connectors.getLength(); i++) {
            String address = connectors.item(i).getAttributes()
                    .getNamedItem("address") != null
                    ? connectors.item(i).getAttributes().getNamedItem("address").getNodeValue() : "0.0.0.0";

            if (!"127.0.0.1".equals(address)) {
                findings.add(Finding.builder()
                        .ruleId(RuleId.SEC_006)
                        .category("Security")
                        .severity(Severity.INFO)
                        .summary("Localhost Binding")
                        .detail("Connector is not restricted to localhost and may be accessible "
                                + "on other network interfaces.")
                        .file("server.xml")
                        .fix("To restrict this connector to localhost only, add address=\"127.0.0.1\" to the <Connector> element. " +
                                "Leave address unset or set to 0.0.0.0 if public access is intentional.")
                        .build());
            }
        }

        return findings;
    }
}

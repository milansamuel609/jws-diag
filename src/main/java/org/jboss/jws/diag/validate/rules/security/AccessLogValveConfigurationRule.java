package org.jboss.jws.diag.validate.rules.security;

import org.jboss.jws.diag.common.RuleId;
import org.jboss.jws.diag.common.Severity;
import org.jboss.jws.diag.validate.Rule;
import org.jboss.jws.diag.validate.RuleContext;
import org.jboss.jws.diag.validate.model.Finding;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class AccessLogValveConfigurationRule implements Rule {

    @Override
    public List<Finding> evaluate(RuleContext ctx) {
        Document doc = ctx.getServerXml();

        if (doc == null) {
            return List.of();
        }

        NodeList valves = doc.getElementsByTagName("Valve");
        List<Finding> findings = new ArrayList<>();

        boolean accessLogValveFound = false;

        for (int i = 0; i < valves.getLength(); i++) {
            Node valve = valves.item(i);

            Node classNameAttr = valve.getAttributes().getNamedItem("className");

            if (classNameAttr != null && "org.apache.catalina.valves.AccessLogValve"
                    .equals(classNameAttr.getNodeValue())) {
                accessLogValveFound = true;
                break;
            }
        }

        if (!accessLogValveFound) {
            findings.add(Finding.builder()
                    .ruleId(RuleId.SEC_008)
                    .category("Security")
                    .severity(Severity.WARN)
                    .summary("Access Log Valve Configuration")
                    .detail("No AccessLogValve is configured, leaving the server without "
                            + "configured access logging for audit purposes.")
                    .file("server.xml")
                    .fix("Configure an AccessLogValve with appropriate logging, rotation, "
                            + "retention, and output settings.")
                    .build());
        }

        return findings;
    }
}
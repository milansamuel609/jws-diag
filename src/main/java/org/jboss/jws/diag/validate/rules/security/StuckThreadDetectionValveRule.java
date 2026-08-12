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

public class StuckThreadDetectionValveRule implements Rule {

    @Override
    public List<Finding> evaluate(RuleContext ctx) {
        Document doc = ctx.getServerXml();

        if (doc == null) {
            return List.of();
        }

        NodeList valves = doc.getElementsByTagName("Valve");
        List<Finding> findings = new ArrayList<>();

        boolean valveFound = false;

        for (int i = 0; i < valves.getLength(); i++) {
            Node valve = valves.item(i);

            Node classNameAttr =
                    valve.getAttributes().getNamedItem("className");

            if (classNameAttr == null
                    || !"org.apache.catalina.valves.StuckThreadDetectionValve"
                    .equals(classNameAttr.getNodeValue())) {
                continue;
            }

            valveFound = true;
            boolean violation = false;

            Node thresholdAttr =
                    valve.getAttributes().getNamedItem("threshold");

            Node interruptAttr =
                    valve.getAttributes()
                            .getNamedItem("interruptThreadThreshold");

            try {
                int threshold = thresholdAttr != null
                        ? Integer.parseInt(thresholdAttr.getNodeValue())
                        : 600;

                if (threshold != 0) {
                    int interruptThreadThreshold = interruptAttr != null
                            ? Integer.parseInt(interruptAttr.getNodeValue())
                            : -1;

                    Node container = valve.getParentNode();
                    Node delayAttr = null;

                    while (container != null) {
                        if (container.getAttributes() != null) {
                            delayAttr = container.getAttributes()
                                    .getNamedItem("backgroundProcessorDelay");

                            if (delayAttr != null) {
                                break;
                            }
                        }

                        container = container.getParentNode();
                    }

                    if (delayAttr != null) {
                        int backgroundProcessorDelay =
                                Integer.parseInt(delayAttr.getNodeValue());

                        if (threshold <= backgroundProcessorDelay) {
                            violation = true;
                        }
                    }

                    if (interruptThreadThreshold != -1
                            && interruptThreadThreshold < threshold) {
                        violation = true;
                    }
                }

            } catch (NumberFormatException e) {
                violation = true;
            }

            if (violation) {
                findings.add(Finding.builder()
                        .ruleId(RuleId.SEC_007)
                        .category("Security")
                        .severity(Severity.INFO)
                        .summary("Stuck Thread Detection Valve Configuration")
                        .detail("Checks whether a StuckThreadDetectionValve is configured with a valid threshold and interruptThreadThreshold.")
                        .file("server.xml")
                        .fix("Set threshold higher than the Container's backgroundProcessorDelay, and set interruptThreadThreshold to -1 or >= threshold.")
                        .build());
            }
        }

        if (!valveFound) {
            findings.add(Finding.builder()
                    .ruleId(RuleId.SEC_007)
                    .category("Security")
                    .severity(Severity.INFO)
                    .summary("Stuck Thread Detection Valve Configuration")
                    .detail("Checks whether a StuckThreadDetectionValve is configured with a valid threshold and interruptThreadThreshold.")
                    .file("server.xml")
                    .fix("Set threshold higher than the Container's backgroundProcessorDelay, and set interruptThreadThreshold to -1 or >= threshold.")
                    .build());
        }

        return findings;
    }
}
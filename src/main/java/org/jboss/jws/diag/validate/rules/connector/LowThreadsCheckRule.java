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
import java.util.function.IntSupplier;

public class LowThreadsCheckRule implements Rule {

    private static final int THREADS_PER_CORE_THRESHOLD = 25;

    private final IntSupplier availableCoresSupplier;

    public LowThreadsCheckRule() {
        this(Runtime.getRuntime()::availableProcessors);
    }

    LowThreadsCheckRule(IntSupplier availableCoresSupplier) {
        this.availableCoresSupplier = availableCoresSupplier;
    }

    @Override
    public List<Finding> evaluate(RuleContext ctx) {
        Document doc = ctx.getServerXml();

        if (doc == null) {
            return List.of();
        }

        NodeList connectors = doc.getElementsByTagName("Connector");
        List<Finding> findings = new ArrayList<>();
        int availableCores = availableCoresSupplier.getAsInt();
        int minRecommendedThreads = availableCores * THREADS_PER_CORE_THRESHOLD;

        for (int i = 0; i < connectors.getLength(); i++) {
            Node connector = connectors.item(i);
            Node maxThreadAttr = connector.getAttributes().getNamedItem("maxThreads");

            if (maxThreadAttr != null) {
                try {
                    int maxThreads = Integer.parseInt(maxThreadAttr.getNodeValue());

                    if (maxThreads < minRecommendedThreads) {
                        findings.add(Finding.builder()
                                .ruleId(RuleId.CONN_001)
                                .category("Connector")
                                .severity(Severity.WARN)
                                .summary("Low Threads Check")
                                .detail("The maxThreads value (" + maxThreads + ") is less than the recommended minimum ("
                                        + minRecommendedThreads + ") for " + availableCores + " available CPU cores.")
                                .file("server.xml")
                                .fix("Adjust maxThreads upward to match your host hardware specifications")
                                .build());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return findings;
    }
}
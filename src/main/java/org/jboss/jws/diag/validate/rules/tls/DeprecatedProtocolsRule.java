package org.jboss.jws.diag.validate.rules.tls;

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
import java.util.Set;

public class DeprecatedProtocolsRule implements Rule {

    private static final Set<String> DEPRECATED_PROTOCOLS = Set.of(
            "SSLv2", "SSLv3", "TLSv1", "TLSv1.1"
    );

    @Override
    public List<Finding> evaluate(RuleContext ctx) {
        Document doc = ctx.getServerXml();

        if (doc == null) {
            return List.of();
        }

        NodeList sslHostConfigs  = doc.getElementsByTagName("SSLHostConfig");
        List<Finding> findings = new ArrayList<>();

        for (int i = 0; i < sslHostConfigs.getLength(); i++) {
            Node sslHostConfig =  sslHostConfigs.item(i);

            Node protocolsAttribute = sslHostConfig.getAttributes().getNamedItem("sslEnabledProtocols");

            if (protocolsAttribute == null) {
                continue;
            }

            String[] protocols = protocolsAttribute.getNodeValue().split(",");

            List<String> foundDeprecated = new ArrayList<>();

            for (String protocol : protocols) {
                if (DEPRECATED_PROTOCOLS.contains(protocol.trim())) {
                    foundDeprecated.add(protocol.trim());
                }
            }

            if (!foundDeprecated.isEmpty()) {
                findings.add(Finding.builder()
                        .ruleId(RuleId.TLS_001)
                        .category("TLS")
                        .severity(Severity.WARN)
                        .summary("Deprecated Protocols")
                        .detail("Obsolete TLS versions detected: " + String.join(", ", foundDeprecated))
                        .file("server.xml")
                        .fix("Update configuration to allow only modern TLSv1.2 or TLSv1.3")
                        .build()
                );
            }
        }

        return findings;
    }
}

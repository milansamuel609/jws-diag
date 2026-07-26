package org.jboss.jws.diag.bundle.redact;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;
import org.jboss.jws.diag.common.RedactionLevel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.regex.Pattern;

public final class IpAddressRedactor implements Redactor {

    public static final String MASK = "[REDACTED]";

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}"
                    + "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b");

    @Override
    public boolean supports(CollectedFile file) {
        return file.getType() == CollectedFile.Type.XML_CONFIG;
    }

    @Override
    public CollectedFile redact(CollectedFile file, BundleContext context) {
        if (context.getRedactionLevel() != RedactionLevel.STRICT) {
            return file;
        }

        try {
            Document document = XmlAttributeRedactor.parseXml(file.getContent());
            maskIpAddresses(document.getDocumentElement());
            String redactedXml = XmlAttributeRedactor.writeXml(document);
            return file.withContent(redactedXml);
        } catch (Exception e) {
            throw new RedactionException(
                    "Failed to redact IP addresses in " + file.getRelativeArchivePath(), e);
        }
    }

    private void maskIpAddresses(Element element) {
        if (element == null) {
            return;
        }

        NamedNodeMap attributes = element.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                String value = attribute.getNodeValue();
                if (value != null && IPV4_PATTERN.matcher(value).find()) {
                    attribute.setNodeValue(IPV4_PATTERN.matcher(value).replaceAll(MASK));
                }
            }
        }

        Node child = element.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                maskIpAddresses((Element) child);
            }
            child = child.getNextSibling();
        }
    }
}
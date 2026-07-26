package org.jboss.jws.diag.bundle.redact;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;
import org.jboss.jws.diag.common.RedactionLevel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class HostnameRedactor implements Redactor {

    public static final String MASK = "[REDACTED]";

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
            maskHostnames(document.getDocumentElement());
            String redactedXml = XmlAttributeRedactor.writeXml(document);
            return file.withContent(redactedXml);
        } catch (Exception e) {
            throw new RedactionException(
                    "Failed to redact hostnames in " + file.getRelativeArchivePath(), e);
        }
    }

    private void maskHostnames(Element element) {
        if (element == null) {
            return;
        }

        NamedNodeMap attributes = element.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);

                String attributeName = attribute.getNodeName();

                if ("proxyName".equals(attributeName)) {
                    attribute.setNodeValue(MASK);
                } else if ("defaultHost".equals(attributeName)) {
                    String attributeValue = attribute.getNodeValue();

                    if (!"localhost".equalsIgnoreCase(attributeValue)) {
                        attribute.setNodeValue(MASK);
                    }
                }
            }
        }

        String localName = element.getLocalName();
        if (localName == null) {
            localName = element.getTagName();
        }

        if ("Host".equals(localName) && element.hasAttribute("name")) {
            String value = element.getAttribute("name");

            if (!"localhost".equalsIgnoreCase(value)) {
                element.setAttribute("name", MASK);
            }
        }

        if ("Alias".equals(localName)) {
            String value = element.getTextContent();

            if (value != null && !value.isBlank()) {
                element.setTextContent(MASK);
            }
        }

        Node child = element.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                maskHostnames((Element) child);
            }
            child = child.getNextSibling();
        }
    }
}
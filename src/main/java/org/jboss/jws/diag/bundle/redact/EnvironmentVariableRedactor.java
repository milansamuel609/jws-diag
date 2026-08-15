package org.jboss.jws.diag.bundle.redact;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;
import org.jboss.jws.diag.common.RedactionLevel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EnvironmentVariableRedactor implements Redactor {

    public static final String MASK = "[REDACTED]";

    private static final Pattern PROPERTY_PATTERN =
            Pattern.compile("\\$\\{([^}:]+)(?::-[^}]*)?}");

    private static final Set<String> SAFE_PROPERTIES = Set.of(
            "catalina.base",
            "catalina.home",
            "java.home",
            "java.io.tmpdir",
            "user.dir"
    );

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
            maskEnvironmentVariables(document.getDocumentElement());
            String redactedXml = XmlAttributeRedactor.writeXml(document);
            return file.withContent(redactedXml);
        } catch (Exception e) {
            throw new RedactionException(
                    "Failed to redact environment variables in "
                            + file.getRelativeArchivePath(), e);
        }
    }

    private void maskEnvironmentVariables(Element element) {
        if (element == null) {
            return;
        }

        NamedNodeMap attributes = element.getAttributes();

        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                attribute.setNodeValue(redactProperties(attribute.getNodeValue()));
            }
        }

        Node child = element.getFirstChild();

        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                maskEnvironmentVariables((Element) child);
            }
            child = child.getNextSibling();
        }
    }

    private String redactProperties(String value) {
        if (value == null) {
            return null;
        }

        Matcher matcher = PROPERTY_PATTERN.matcher(value);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String property = matcher.group(1);

            if (SAFE_PROPERTIES.contains(property)) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement(MASK));
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
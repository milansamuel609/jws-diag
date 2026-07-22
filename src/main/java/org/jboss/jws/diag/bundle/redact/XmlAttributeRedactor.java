package org.jboss.jws.diag.bundle.redact;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class XmlAttributeRedactor implements Redactor {

    public static final String MASK = "[REDACTED]";

    private static final String[] SENSITIVE_KEYWORDS = {
            "password",
            "pass",
            "secret",
            "credential"
    };

    @Override
    public boolean supports(CollectedFile file) {
        return file.getType() == CollectedFile.Type.XML_CONFIG;
    }

    @Override
    public CollectedFile redact(CollectedFile file, BundleContext context) {
        try {
            Document document = parse(file.getContent());
            redactAttributesRecursively(document.getDocumentElement());
            String redactedXml = serialize(document);
            return file.withContent(redactedXml);
        } catch (Exception e) {
            throw new RedactionException(
                    "Failed to redact XML attributes in " + file.getRelativeArchivePath(), e);
        }
    }

    private boolean isSensitive(String attributeName) {
        String lower =  attributeName.toLowerCase(Locale.ROOT);
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void redactAttributesRecursively(Element element) {
        if (element == null) {
            return;
        }

        NamedNodeMap attributes = element.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attr = attributes.item(i);
                if (isSensitive(attr.getNodeName())) {
                    attr.setNodeValue(MASK);
                }
            }
        }

        Node child = element.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                redactAttributesRecursively((Element) child);
            }
            child = child.getNextSibling();
        }
    }

    private Document parse(String xmlContent) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
    }

    private String serialize(Document document) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }
}

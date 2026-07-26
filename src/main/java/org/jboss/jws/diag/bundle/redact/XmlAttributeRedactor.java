package org.jboss.jws.diag.bundle.redact;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class XmlAttributeRedactor implements Redactor {

    public static final String MASK = "[REDACTED]";

    private static final String[] SENSITIVE_KEYWORDS = {
            "password",
            "keystorepass",
            "truststorepass",
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
            Document document = parseXml(file.getContent());
            maskSensitiveAttributes(document.getDocumentElement());
            String redactedXml = writeXml(document);
            return file.withContent(redactedXml);
        } catch (IOException | TransformerException e) {
            throw new RedactionException(
                    "Failed to redact XML attributes in " + file.getRelativeArchivePath(), e);
        }
    }

    private boolean isSensitiveAttribute(String attributeName) {
        String lower = attributeName.toLowerCase(Locale.ROOT);
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void maskSensitiveAttributes(Element element) {
        if (element == null) {
            return;
        }

        NamedNodeMap attributes = element.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                if (isSensitiveAttribute(attribute.getNodeName())) {
                    attribute.setNodeValue(MASK);
                }
            }
        }

        Node child = element.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                maskSensitiveAttributes((Element) child);
            }
            child = child.getNextSibling();
        }
    }

    static Document parseXml(String xmlContent) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (ParserConfigurationException e) {
            throw new IOException("Failed to configure XXE-safe parser", e);
        }
        factory.setExpandEntityReferences(false);
        factory.setNamespaceAware(false);

        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IOException("Failed to create DocumentBuilder", e);
        }

        try {
            return builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
        } catch (SAXException e) {
            throw new IOException("Failed to parse XML content: " + e.getMessage(), e);
        }
    }

    static String writeXml(Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }
}
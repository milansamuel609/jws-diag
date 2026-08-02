package org.jboss.jws.diag.bundle.redact;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;
import org.jboss.jws.diag.common.RedactionLevel;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PropertiesRedactor implements Redactor {

    public static final String MASK = XmlAttributeRedactor.MASK;

    private static final Pattern KEY_VALUE_LINE = Pattern.compile("^(\\s*[^=:#\\s][^=:]*)([=:])(.*)$");

    @Override
    public boolean supports(CollectedFile file) {
        return file.getType() == CollectedFile.Type.PROPERTIES;
    }

    @Override
    public CollectedFile redact(CollectedFile file, BundleContext context) {
        String normalizedContent = file.getContent().replace("\r\n", "\n").replace("\r", "\n");
        String[] lines = normalizedContent.split("\n", -1);

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            result.append(redactLine(lines[i], context));
            if (i < lines.length - 1) {
                result.append("\n");
            }
        }

        return file.withContent(result.toString());
    }

    private String redactLine(String line, BundleContext context) {
        Matcher matcher = KEY_VALUE_LINE.matcher(line);
        if (!matcher.matches()) {
            return line;
        }

        String key = matcher.group(1);
        String separator = matcher.group(2);

        String value = matcher.group(3);

        if (isSensitive(key)) {
            return key + separator + MASK;
        }

        if (context.getRedactionLevel() == RedactionLevel.STRICT) {
            value = IpAddressMasker.mask(value, MASK);
            value = HostnameMasker.mask(value, MASK);
            value = EnvironmentVariableMasker.mask(value, MASK);
        }

        return key + separator + value;
    }

    private boolean isSensitive(String key) {
        String lower = key.toLowerCase(Locale.ROOT);
        for (String keyword : SensitiveKeywords.KEYWORDS) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
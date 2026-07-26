package org.jboss.jws.diag.bundle.redact;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PropertiesRedactor implements Redactor {

    public static final String MASK = XmlAttributeRedactor.MASK;

    private static final String[] SENSITIVE_KEYWORDS = {
            "password",
            "keystorepass",
            "truststorepass",
            "secret",
            "credential"
    };

    private static final Pattern KEY_VALUE_LINE = Pattern.compile("^(\\s*[^=:#\\s][^=:]*)([=:])(.*)$");

    @Override
    public boolean supports(CollectedFile file) {
        return file.getType() == CollectedFile.Type.PROPERTIES;
    }

    @Override
    public CollectedFile redact(CollectedFile file, BundleContext context) {
        StringBuilder result = new StringBuilder();
        String[] lines = file.getContent().split("\n", -1);

        for (int i = 0; i < lines.length; i++) {
            result.append(redactLine(lines[i]));
            if (i < lines.length - 1) {
                result.append("\n");
            }
        }

        return file.withContent(result.toString());
    }

    private String redactLine(String line) {
        Matcher matcher = KEY_VALUE_LINE.matcher(line);
        if (!matcher.matches()) {
            return line;
        }

        String key = matcher.group(1);
        String separator = matcher.group(2);

        if (isSensitive(key)) {
            return key + separator + MASK;
        }
        return line;
    }

    private boolean isSensitive(String key) {
        String lower = key.toLowerCase(Locale.ROOT);
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
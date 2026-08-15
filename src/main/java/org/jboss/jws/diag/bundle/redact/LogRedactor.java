package org.jboss.jws.diag.bundle.redact;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LogRedactor implements Redactor {

    private static final String MASK = XmlAttributeRedactor.MASK;

    private static final Pattern SENSITIVE_VALUE_PATTERN =
            Pattern.compile(
                    "(?i)((?:password|keystorepass|truststorepass|secret|credential)"
                            + "\\s*[=:]\\s*)([^\\s,;]+)");

    @Override
    public boolean supports(CollectedFile file) {
        return file.getType() == CollectedFile.Type.LOG;
    }

    @Override
    public CollectedFile redact(CollectedFile file, BundleContext context) {
        String content = file.getContent();

        Matcher matcher = SENSITIVE_VALUE_PATTERN.matcher(content);

        String redacted = matcher.replaceAll(
                "$1" + Matcher.quoteReplacement(MASK));

        return file.withContent(redacted);
    }
}
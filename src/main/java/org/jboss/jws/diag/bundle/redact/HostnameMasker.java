package org.jboss.jws.diag.bundle.redact;

import java.util.Set;
import java.util.regex.Pattern;

public final class HostnameMasker {

    private static final Set<String> RESERVED_HOSTS = Set.of(
            "localhost"
    );

    private static final Pattern ENV_PATTERN =
            Pattern.compile("^\\$\\{[^}]+}$");

    private HostnameMasker() {
    }

    public static String mask(String value, String replacement) {
        if (value == null || value.isBlank()) {
            return value;
        }

        // Skip environment variable expressions.
        if (ENV_PATTERN.matcher(value).matches()) {
            return value;
        }

        if (RESERVED_HOSTS.contains(value.toLowerCase())) {
            return value;
        }

        if (looksLikeHostname(value)) {
            return replacement;
        }

        return value;
    }

    private static boolean looksLikeHostname(String value) {
        return value.contains(".");
    }
}
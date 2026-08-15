package org.jboss.jws.diag.bundle.redact;

import java.util.regex.Pattern;

public final class IpAddressMasker {

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}"
                    + "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b");

    private IpAddressMasker() {
    }

    public static String mask(String value, String replacement) {
        if (value == null) {
            return null;
        }

        return IPV4_PATTERN.matcher(value).replaceAll(replacement);
    }

    public static boolean containsIpAddress(String value) {
        return value != null && IPV4_PATTERN.matcher(value).find();
    }
}
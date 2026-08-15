package org.jboss.jws.diag.bundle.redact;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EnvironmentVariableMasker {

    private static final Pattern ENV_PATTERN =
            Pattern.compile("\\$\\{([^}]+)}");

    private static final Set<String> SAFE_VARIABLES = Set.of(
            "catalina.home",
            "catalina.base",
            "java.home",
            "java.io.tmpdir",
            "user.dir"
    );

    private EnvironmentVariableMasker() {
    }

    public static String mask(String value, String replacement) {
        if (value == null) {
            return null;
        }

        Matcher matcher = ENV_PATTERN.matcher(value);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String variable = matcher.group(1);

            if (SAFE_VARIABLES.contains(variable)) {
                matcher.appendReplacement(
                        result,
                        Matcher.quoteReplacement(matcher.group()));
            } else {
                matcher.appendReplacement(
                        result,
                        Matcher.quoteReplacement(replacement));
            }
        }

        matcher.appendTail(result);

        return result.toString();
    }

    public static boolean containsEnvironmentVariable(String value) {
        return value != null && ENV_PATTERN.matcher(value).find();
    }
}
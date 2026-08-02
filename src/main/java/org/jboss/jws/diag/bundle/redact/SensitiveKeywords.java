package org.jboss.jws.diag.bundle.redact;

public final class SensitiveKeywords {

    private SensitiveKeywords() {
    }

    public static final String[] KEYWORDS = {
            "password",
            "keystorepass",
            "truststorepass",
            "secret",
            "credential"
    };
}
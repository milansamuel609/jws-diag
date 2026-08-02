package org.jboss.jws.diag.bundle.redact;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EnvironmentVariableMaskerTest {

    @Test
    void shouldMaskEnvironmentVariables() {

        String result = EnvironmentVariableMasker.mask(
                "${JAVA_HOME}",
                "[REDACTED]");

        assertThat(result)
                .isEqualTo("[REDACTED]");
    }

    @Test
    void shouldNotMaskCatalinaVariables() {

        String result = EnvironmentVariableMasker.mask(
                "${catalina.home}",
                "[REDACTED]");

        assertThat(result)
                .isEqualTo("${catalina.home}");
    }

    @Test
    void shouldDetectEnvironmentVariables() {

        assertThat(
                EnvironmentVariableMasker.containsEnvironmentVariable("${JAVA_HOME}"))
                .isTrue();

        assertThat(
                EnvironmentVariableMasker.containsEnvironmentVariable("plain-text"))
                .isFalse();
    }
}
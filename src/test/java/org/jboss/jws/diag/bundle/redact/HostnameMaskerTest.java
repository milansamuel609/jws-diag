package org.jboss.jws.diag.bundle.redact;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HostnameMaskerTest {

    @Test
    void shouldMaskHostname() {

        assertThat(
                HostnameMasker.mask(
                        "example.com",
                        "[REDACTED]"))
                .isEqualTo("[REDACTED]");
    }

    @Test
    void shouldNotMaskLocalhost() {

        assertThat(
                HostnameMasker.mask(
                        "localhost",
                        "[REDACTED]"))
                .isEqualTo("localhost");
    }

    @Test
    void shouldNotMaskCatalinaVariable() {

        assertThat(
                HostnameMasker.mask(
                        "${catalina.home}",
                        "[REDACTED]"))
                .isEqualTo("${catalina.home}");
    }
}
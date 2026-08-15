package org.jboss.jws.diag.bundle.redact;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IpAddressMaskerTest {

    @Test
    void shouldMaskIpAddresses() {

        String result = IpAddressMasker.mask(
                "Server=192.168.1.100",
                "[REDACTED]");

        assertThat(result)
                .isEqualTo("Server=[REDACTED]");
    }

    @Test
    void shouldDetectIpAddress() {

        assertThat(
                IpAddressMasker.containsIpAddress("10.0.0.1"))
                .isTrue();

        assertThat(
                IpAddressMasker.containsIpAddress("localhost"))
                .isFalse();
    }
}
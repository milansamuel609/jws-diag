package org.jboss.jws.diag.bundle.redact;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;
import org.jboss.jws.diag.common.RedactionLevel;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class HostnameRedactorTest {

    private static CollectedFile createXmlFile(String xml) {
        return CollectedFile.builder()
                .relativeArchivePath("server.xml")
                .sourcePath(Path.of("server.xml"))
                .type(CollectedFile.Type.XML_CONFIG)
                .content(xml)
                .build();
    }

    private final HostnameRedactor redactor = new HostnameRedactor();

    private final BundleContext strictContext = new BundleContext(
            Path.of("base"),
            Path.of("home"),
            Path.of("staging"),
            RedactionLevel.STRICT
    );

    @Test
    void shouldRedactHostName() {

        String xml =
                "<Server>\n" +
                        "  <Service>\n" +
                        "    <Engine defaultHost=\"prod.company.internal\">\n" +
                        "      <Host name=\"prod.company.internal\"/>\n" +
                        "    </Engine>\n" +
                        "  </Service>\n" +
                        "</Server>";

        CollectedFile output = redactor.redact(createXmlFile(xml), strictContext);

        assertThat(output.getContent())
                .contains("defaultHost=\"[REDACTED]\"")
                .contains("name=\"[REDACTED]\"");
    }

    @Test
    void shouldRedactProxyName() {

        String xml =
                "<Server>\n" +
                        "  <Service>\n" +
                        "    <Connector proxyName=\"api.company.internal\"/>\n" +
                        "  </Service>\n" +
                        "</Server>";

        CollectedFile output = redactor.redact(createXmlFile(xml), strictContext);

        assertThat(output.getContent())
                .contains("proxyName=\"[REDACTED]\"");
    }

    @Test
    void shouldRedactAliasElement() {

        String xml =
                "<Server>\n" +
                        "  <Service>\n" +
                        "    <Engine>\n" +
                        "      <Host name=\"prod.company.internal\">\n" +
                        "        <Alias>www.company.internal</Alias>\n" +
                        "      </Host>\n" +
                        "    </Engine>\n" +
                        "  </Service>\n" +
                        "</Server>";

        CollectedFile output = redactor.redact(createXmlFile(xml), strictContext);

        assertThat(output.getContent())
                .contains("<Alias>[REDACTED]</Alias>");
    }

    @Test
    void shouldNotRedactLocalhost() {

        String xml =
                "<Server>\n" +
                        "  <Service>\n" +
                        "    <Engine defaultHost=\"localhost\">\n" +
                        "      <Host name=\"localhost\"/>\n" +
                        "    </Engine>\n" +
                        "  </Service>\n" +
                        "</Server>";

        CollectedFile output = redactor.redact(createXmlFile(xml), strictContext);

        assertThat(output.getContent())
                .contains("defaultHost=\"localhost\"")
                .contains("name=\"localhost\"");
    }
}
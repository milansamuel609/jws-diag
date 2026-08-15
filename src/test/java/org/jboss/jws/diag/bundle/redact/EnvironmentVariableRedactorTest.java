package org.jboss.jws.diag.bundle.redact;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;
import org.jboss.jws.diag.common.RedactionLevel;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentVariableRedactorTest {

    private static CollectedFile createXmlFile(String xml) {
        return CollectedFile.builder()
                .relativeArchivePath("server.xml")
                .sourcePath(Path.of("server.xml"))
                .type(CollectedFile.Type.XML_CONFIG)
                .content(xml)
                .build();
    }

    private final EnvironmentVariableRedactor redactor = new EnvironmentVariableRedactor();

    private final BundleContext strictContext = new BundleContext(
            Path.of("base"),
            Path.of("home"),
            Path.of("staging"),
            RedactionLevel.STRICT
    );

    private final BundleContext defaultContext = new BundleContext(
            Path.of("base"),
            Path.of("home"),
            Path.of("staging"),
            RedactionLevel.DEFAULT
    );

    @Test
    void shouldRedactEnvironmentVariable() {

        String xml =
                "<Server>\n" +
                        "  <Environment value=\"${DB_PASSWORD}\"/>\n" +
                        "</Server>";

        CollectedFile output = redactor.redact(createXmlFile(xml), strictContext);

        assertThat(output.getContent())
                .contains("value=\"[REDACTED]\"");
    }

    @Test
    void shouldRedactEnvironmentVariableWithDefaultValue() {

        String xml =
                "<Server>\n" +
                        "  <Environment value=\"${JWT_SECRET:-default}\"/>\n" +
                        "</Server>";

        CollectedFile output = redactor.redact(createXmlFile(xml), strictContext);

        assertThat(output.getContent())
                .contains("value=\"[REDACTED]\"");
    }

    @Test
    void shouldNotRedactStandardTomcatProperties() {

        String xml =
                "<Server>\n" +
                        "  <Environment home=\"${catalina.home}\"/>\n" +
                        "  <Environment base=\"${catalina.base}\"/>\n" +
                        "</Server>";

        CollectedFile output = redactor.redact(createXmlFile(xml), strictContext);

        assertThat(output.getContent())
                .contains("${catalina.home}")
                .contains("${catalina.base}");
    }

    @Test
    void shouldNotRedactInDefaultMode() {

        String xml =
                "<Server>\n" +
                        "  <Environment value=\"${DB_PASSWORD}\"/>\n" +
                        "</Server>";

        CollectedFile output = redactor.redact(createXmlFile(xml), defaultContext);

        assertThat(output.getContent())
                .contains("${DB_PASSWORD}");
    }
}
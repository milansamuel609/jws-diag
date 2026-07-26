package org.jboss.jws.diag.bundle.redact;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;
import org.jboss.jws.diag.common.RedactionLevel;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class IpAddressRedactorTest {

    private static final String MASK = IpAddressRedactor.MASK;

    private final IpAddressRedactor redactor = new IpAddressRedactor();

    private BundleContext contextWithLevel(RedactionLevel level) {
        return new BundleContext(Paths.get("/dummy"), Paths.get("/dummy"), Paths.get("/dummy-staging"), level);
    }

    private CollectedFile xmlFile(String content) {
        return CollectedFile.builder()
                .relativeArchivePath("conf/server.xml")
                .sourcePath(Path.of("conf/server.xml"))
                .type(CollectedFile.Type.XML_CONFIG)
                .content(content)
                .build();
    }

    @Test
    void shouldSupportXmlConfigType() {
        assertThat(redactor.supports(xmlFile("<Server/>"))).isTrue();
    }

    @Test
    void shouldReturnUnchangedFileWhenLevelIsDefault() {
        CollectedFile file = xmlFile("<Connector address=\"192.168.1.10\"/>");

        CollectedFile result = redactor.redact(file, contextWithLevel(RedactionLevel.DEFAULT));

        assertThat(result.getContent()).isEqualTo("<Connector address=\"192.168.1.10\"/>");
    }

    @Test
    void shouldRedactIpAddressWhenLevelIsStrict() {
        CollectedFile file = xmlFile("<Connector address=\"192.168.1.10\"/>");

        CollectedFile result = redactor.redact(file, contextWithLevel(RedactionLevel.STRICT));

        assertThat(result.getContent()).contains(MASK).doesNotContain("192.168.1.10");
    }

    @Test
    void shouldRedactMultipleIpAddressesInDifferentElements() {
        CollectedFile file = xmlFile(
                "<Server><Connector address=\"10.0.0.5\"/><Connector address=\"10.0.0.6\"/></Server>");

        CollectedFile result = redactor.redact(file, contextWithLevel(RedactionLevel.STRICT));

        assertThat(result.getContent())
                .doesNotContain("10.0.0.5")
                .doesNotContain("10.0.0.6");
    }

    @Test
    void shouldNotAffectNonIpAttributeValues() {
        CollectedFile file = xmlFile("<Connector port=\"8080\" protocol=\"HTTP/1.1\"/>");

        CollectedFile result = redactor.redact(file, contextWithLevel(RedactionLevel.STRICT));

        assertThat(result.getContent()).contains("port=\"8080\"").contains("protocol=\"HTTP/1.1\"");
    }
}
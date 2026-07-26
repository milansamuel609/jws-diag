package org.jboss.jws.diag.bundle.redact;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;
import org.jboss.jws.diag.common.RedactionLevel;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertiesRedactorTest {

    private static final String MASK = PropertiesRedactor.MASK;

    private final PropertiesRedactor redactor = new PropertiesRedactor();

    private final BundleContext dummyContext = new BundleContext(
            Paths.get("/dummy"), Paths.get("/dummy"), Paths.get("/dummy-staging"), RedactionLevel.DEFAULT);

    private CollectedFile propertiesFile(String content) {
        return CollectedFile.builder()
                .relativeArchivePath("conf/catalina.properties")
                .sourcePath(Path.of("conf/catalina.properties"))
                .type(CollectedFile.Type.PROPERTIES)
                .content(content)
                .build();
    }

    @Test
    void shouldSupportPropertiesType() {
        assertThat(redactor.supports(propertiesFile("key=value"))).isTrue();
    }

    @Test
    void shouldNotSupportXmlConfigType() {
        CollectedFile xmlFile = CollectedFile.builder()
                .relativeArchivePath("conf/server.xml")
                .sourcePath(Path.of("conf/server.xml"))
                .type(CollectedFile.Type.XML_CONFIG)
                .content("<Server/>")
                .build();

        assertThat(redactor.supports(xmlFile)).isFalse();
    }

    @Test
    void shouldRedactValueWhenKeyContainsPassword() {
        CollectedFile file = propertiesFile("db.password=changeit");

        CollectedFile result = redactor.redact(file, dummyContext);

        assertThat(result.getContent()).isEqualTo("db.password=" + MASK);
    }

    @Test
    void shouldRedactValueWhenKeyContainsSecret() {
        CollectedFile file = propertiesFile("client.secret=abc123");

        CollectedFile result = redactor.redact(file, dummyContext);

        assertThat(result.getContent()).isEqualTo("client.secret=" + MASK);
    }

    @Test
    void shouldNotRedactNonSensitiveKeys() {
        CollectedFile file = propertiesFile("server.port=8080");

        CollectedFile result = redactor.redact(file, dummyContext);

        assertThat(result.getContent()).isEqualTo("server.port=8080");
    }

    @Test
    void shouldPreserveCommentsAndBlankLines() {
        CollectedFile file = propertiesFile("# a comment\n\nserver.port=8080");

        CollectedFile result = redactor.redact(file, dummyContext);

        assertThat(result.getContent()).isEqualTo("# a comment\n\nserver.port=8080");
    }

    @Test
    void shouldHandleMultipleLinesIndependently() {
        CollectedFile file = propertiesFile("db.password=changeit\nserver.port=8080\napi.credential=xyz");

        CollectedFile result = redactor.redact(file, dummyContext);

        assertThat(result.getContent())
                .contains("db.password=" + MASK)
                .contains("server.port=8080")
                .contains("api.credential=" + MASK);
    }

    @Test
    void shouldMarkResultAsRedacted() {
        CollectedFile file = propertiesFile("db.password=changeit");

        CollectedFile result = redactor.redact(file, dummyContext);

        assertThat(result.isRedacted()).isTrue();
    }
}
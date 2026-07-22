package org.jboss.jws.diag.bundle.redact;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;
import org.jboss.jws.diag.common.RedactionLevel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlAttributeRedactorTest {

    private static final String MASK = XmlAttributeRedactor.MASK;

    private final XmlAttributeRedactor redactor = new XmlAttributeRedactor();

    private final BundleContext dummyContext = new BundleContext(
            Paths.get("/dummy"), Paths.get("/dummy"), Paths.get("/dummy"), RedactionLevel.DEFAULT);

    private String loadFixture(String resourcePath) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Fixture not found on classpath: " + resourcePath);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private CollectedFile fixtureAsCollectedFile(String resourcePath) throws IOException {
        return CollectedFile.builder()
                .relativeArchivePath("conf/server.xml")
                .sourcePath(Path.of("conf/server.xml"))
                .type(CollectedFile.Type.XML_CONFIG)
                .content(loadFixture(resourcePath))
                .build();
    }

    @Test
    void shouldSupportXmlConfigType() {
        CollectedFile file = CollectedFile.builder()
                .relativeArchivePath("conf/server.xml")
                .sourcePath(Path.of("conf/server.xml"))
                .type(CollectedFile.Type.XML_CONFIG)
                .content("<Server/>")
                .build();

        assertThat(redactor.supports(file)).isTrue();
    }

    @Test
    void shouldNotSupportPropertiesType() {
        CollectedFile file = CollectedFile.builder()
                .relativeArchivePath("conf/catalina.properties")
                .sourcePath(Path.of("conf/catalina.properties"))
                .type(CollectedFile.Type.PROPERTIES)
                .content("key=value")
                .build();

        assertThat(redactor.supports(file)).isFalse();
    }

    @Test
    void shouldNotSupportLogType() {
        CollectedFile file = CollectedFile.builder()
                .relativeArchivePath("logs/catalina.out")
                .sourcePath(Path.of("logs/catalina.out"))
                .type(CollectedFile.Type.LOG)
                .content("some log line")
                .build();

        assertThat(redactor.supports(file)).isFalse();
    }

    @Test
    void shouldRedactCertificateKeystorePasswordInServerXml() throws IOException {
        CollectedFile file = fixtureAsCollectedFile("/fixtures/bundle/server-clean-bundle.xml");

        CollectedFile result = redactor.redact(file, dummyContext);

        assertThat(result.getContent())
                .contains("certificateKeystorePassword=\"" + MASK + "\"")
                .doesNotContain("changeit");
    }

    @Test
    void shouldNotRedactNonSensitiveAttributesInServerXml() throws IOException {
        CollectedFile file = fixtureAsCollectedFile("/fixtures/bundle/server-clean-bundle.xml");

        CollectedFile result = redactor.redact(file, dummyContext);

        assertThat(result.getContent())
                .contains("certificateKeystoreFile=\"conf/localhost-rsa.jks\"")
                .contains("port=\"8080\"")
                .contains("address=\"127.0.0.1\"")
                .contains("maxThreads=\"150\"");
    }

    @Test
    void shouldRedactPasswordInTomcatUsersXml() throws IOException {
        CollectedFile file = CollectedFile.builder()
                .relativeArchivePath("conf/tomcat-users.xml")
                .sourcePath(Path.of("conf/tomcat-users.xml"))
                .type(CollectedFile.Type.XML_CONFIG)
                .content(loadFixture("/fixtures/bundle/tomcat-users-clean-bundle.xml"))
                .build();

        CollectedFile result = redactor.redact(file, dummyContext);

        assertThat(result.getContent())
                .contains("password=\"" + MASK + "\"")
                .doesNotContain("uNique@7736#");
    }

    @Test
    void shouldNotRedactUsernameInTomcatUsersXml() throws IOException {
        CollectedFile file = CollectedFile.builder()
                .relativeArchivePath("conf/tomcat-users.xml")
                .sourcePath(Path.of("conf/tomcat-users.xml"))
                .type(CollectedFile.Type.XML_CONFIG)
                .content(loadFixture("/fixtures/bundle/tomcat-users-clean-bundle.xml"))
                .build();

        CollectedFile result = redactor.redact(file, dummyContext);

        assertThat(result.getContent()).contains("username=\"current222\"");
    }

    @Test
    void shouldMarkResultAsRedacted() throws IOException {
        CollectedFile file = fixtureAsCollectedFile("/fixtures/bundle/server-clean-bundle.xml");

        CollectedFile result = redactor.redact(file, dummyContext);

        assertThat(result.isRedacted()).isTrue();
    }

    @Test
    void shouldNotMutateOriginalCollectedFile() throws IOException {
        CollectedFile file = fixtureAsCollectedFile("/fixtures/bundle/server-clean-bundle.xml");

        redactor.redact(file, dummyContext);

        assertThat(file.isRedacted()).isFalse();
        assertThat(file.getContent()).contains("changeit");
    }
}
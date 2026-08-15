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

    private final BundleContext strictContext = new BundleContext(
            Paths.get("/dummy"),
            Paths.get("/dummy"),
            Paths.get("/dummy-staging"),
            RedactionLevel.STRICT);

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

    @Test
    void shouldRedactIpAddressInStrictMode() {
        CollectedFile file = propertiesFile("server.ip=192.168.10.25");

        CollectedFile result = redactor.redact(file, strictContext);

        assertThat(result.getContent())
                .isEqualTo("server.ip=" + MASK);
    }

    @Test
    void shouldRedactHostnameInStrictMode() {
        CollectedFile file = propertiesFile("proxy.host=prod.company.internal");

        CollectedFile result = redactor.redact(file, strictContext);

        assertThat(result.getContent())
                .isEqualTo("proxy.host=" + MASK);
    }

    @Test
    void shouldRedactEnvironmentVariableInStrictMode() {
        CollectedFile file = propertiesFile("jwt.secret=${JWT_SECRET}");

        CollectedFile result = redactor.redact(file, strictContext);

        assertThat(result.getContent())
                .isEqualTo("jwt.secret=" + MASK);
    }

    @Test
    void shouldPreserveTomcatEnvironmentVariablesInStrictMode() {
        CollectedFile file = propertiesFile(
                "tomcat.home=${catalina.home}\n" +
                        "tomcat.base=${catalina.base}");

        CollectedFile result = redactor.redact(file, strictContext);

        assertThat(result.getContent())
                .isEqualTo(
                        "tomcat.home=${catalina.home}\n" +
                                "tomcat.base=${catalina.base}");
    }
}
package org.jboss.jws.diag.bundle;

import org.jboss.jws.diag.common.RedactionLevel;
import org.jboss.jws.diag.bundle.redact.XmlAttributeRedactor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class BundleEngineTest {

    private static final String MASK = XmlAttributeRedactor.MASK;

    private final BundleEngine engine = new BundleEngine();

    private void writeConfFile(Path catalinaBase, String fileName, String content) throws IOException {
        Path confDir = catalinaBase.resolve("conf");
        Files.createDirectories(confDir);
        Files.writeString(confDir.resolve(fileName), content, StandardCharsets.UTF_8);
    }

    @Test
    void shouldStageRedactedConfFiles(@TempDir Path catalinaBase, @TempDir Path stagingDir) throws IOException {
        writeConfFile(catalinaBase, "server.xml",
                "<Server><Connector connectionPassword=\"secret123\"/></Server>");
        writeConfFile(catalinaBase, "tomcat-users.xml",
                "<tomcat-users><user username=\"admin\" password=\"changeit\"/></tomcat-users>");

        BundleContext context = new BundleContext(catalinaBase, catalinaBase, stagingDir, RedactionLevel.DEFAULT);
        engine.run(context);

        String stagedServerXml = Files.readString(stagingDir.resolve("conf/server.xml"), StandardCharsets.UTF_8);
        String stagedUsersXml = Files.readString(stagingDir.resolve("conf/tomcat-users.xml"), StandardCharsets.UTF_8);

        assertThat(stagedServerXml)
                .contains(MASK)
                .doesNotContain("secret123");
        assertThat(stagedUsersXml)
                .contains(MASK)
                .doesNotContain("changeit");
    }

    @Test
    void shouldPreserveNonSensitiveAttributesInStagedFiles(@TempDir Path catalinaBase, @TempDir Path stagingDir) throws IOException {
        writeConfFile(catalinaBase, "server.xml",
                "<Server port=\"8080\"><Connector address=\"127.0.0.1\"/></Server>");

        BundleContext context = new BundleContext(catalinaBase, catalinaBase, stagingDir, RedactionLevel.DEFAULT);
        engine.run(context);

        String stagedServerXml = Files.readString(stagingDir.resolve("conf/server.xml"), StandardCharsets.UTF_8);

        assertThat(stagedServerXml)
                .contains("port=\"8080\"")
                .contains("address=\"127.0.0.1\"");
    }

    @Test
    void shouldOnlyStageFilesThatExistOnDisk(@TempDir Path catalinaBase, @TempDir Path stagingDir) throws IOException {
        writeConfFile(catalinaBase, "server.xml", "<Server/>");

        BundleContext context = new BundleContext(catalinaBase, catalinaBase, stagingDir, RedactionLevel.DEFAULT);
        engine.run(context);

        assertThat(stagingDir.resolve("conf/server.xml")).exists();
        assertThat(stagingDir.resolve("conf/web.xml")).doesNotExist();
        assertThat(stagingDir.resolve("conf/tomcat-users.xml")).doesNotExist();
    }

    @Test
    void shouldNotThrowWhenNoConfFilesExist(@TempDir Path catalinaBase, @TempDir Path stagingDir) throws IOException {
        Files.createDirectories(catalinaBase.resolve("conf"));

        BundleContext context = new BundleContext(catalinaBase, catalinaBase, stagingDir, RedactionLevel.DEFAULT);

        engine.run(context);
    }
}
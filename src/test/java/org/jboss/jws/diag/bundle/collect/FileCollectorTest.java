package org.jboss.jws.diag.bundle.collect;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;
import org.jboss.jws.diag.common.RedactionLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FileCollectorTest {

    private final FileCollector collector = new FileCollector();

    private void writeConfFile(Path catalinaBase, String fileName, String content) throws IOException {
        Path confDir = catalinaBase.resolve("conf");
        Files.createDirectories(confDir);
        Files.writeString(confDir.resolve(fileName), content, StandardCharsets.UTF_8);
    }

    private BundleContext contextFor(Path catalinaBase) {
        return new BundleContext(catalinaBase, catalinaBase, catalinaBase.resolve("staging"), RedactionLevel.DEFAULT);
    }

    @Test
    void shouldCollectAllFourConfFilesWhenAllExist(@TempDir Path catalinaBase) throws IOException {
        writeConfFile(catalinaBase, "server.xml", "<Server/>");
        writeConfFile(catalinaBase, "web.xml", "<web-app/>");
        writeConfFile(catalinaBase, "tomcat-users.xml", "<tomcat-users/>");

        List<CollectedFile> files = collector.collectConfFiles(contextFor(catalinaBase));

        assertThat(files).hasSize(3);
        assertThat(files).extracting(CollectedFile::getRelativeArchivePath)
                .containsExactlyInAnyOrder(
                        "conf/server.xml", "conf/web.xml", "conf/tomcat-users.xml");
    }

    @Test
    void shouldSkipMissingFilesWithoutFailing(@TempDir Path catalinaBase) throws IOException {
        writeConfFile(catalinaBase, "server.xml", "<Server/>");
        writeConfFile(catalinaBase, "tomcat-users.xml", "<tomcat-users/>");

        List<CollectedFile> files = collector.collectConfFiles(contextFor(catalinaBase));

        assertThat(files).hasSize(2);
        assertThat(files).extracting(CollectedFile::getRelativeArchivePath)
                .containsExactlyInAnyOrder("conf/server.xml", "conf/tomcat-users.xml");
    }

    @Test
    void shouldReturnEmptyListWhenNoConfFilesExist(@TempDir Path catalinaBase) throws IOException {
        Files.createDirectories(catalinaBase.resolve("conf"));

        List<CollectedFile> files = collector.collectConfFiles(contextFor(catalinaBase));

        assertThat(files).isEmpty();
    }

    @Test
    void shouldPreserveActualFileContent(@TempDir Path catalinaBase) throws IOException {
        String content = "<Server port=\"-1\" shutdown=\"SHUTDOWN\"/>";
        writeConfFile(catalinaBase, "server.xml", content);

        List<CollectedFile> files = collector.collectConfFiles(contextFor(catalinaBase));

        assertThat(files).hasSize(1);
        assertThat(files.get(0).getContent()).isEqualTo(content);
    }

    @Test
    void shouldTagAllCollectedFilesAsXmlConfigType(@TempDir Path catalinaBase) throws IOException {
        writeConfFile(catalinaBase, "server.xml", "<Server/>");
        writeConfFile(catalinaBase, "tomcat-users.xml", "<tomcat-users/>");

        List<CollectedFile> files = collector.collectConfFiles(contextFor(catalinaBase));

        assertThat(files).allMatch(f -> f.getType() == CollectedFile.Type.XML_CONFIG);
    }

    @Test
    void shouldMarkCollectedFilesAsNotYetRedacted(@TempDir Path catalinaBase) throws IOException {
        writeConfFile(catalinaBase, "server.xml", "<Server/>");

        List<CollectedFile> files = collector.collectConfFiles(contextFor(catalinaBase));

        assertThat(files).allMatch(f -> !f.isRedacted());
    }

    @Test
    void shouldSetSourcePathToActualFileLocation(@TempDir Path catalinaBase) throws IOException {
        writeConfFile(catalinaBase, "server.xml", "<Server/>");

        List<CollectedFile> files = collector.collectConfFiles(contextFor(catalinaBase));

        assertThat(files.get(0).getSourcePath())
                .isEqualTo(catalinaBase.resolve("conf/server.xml"));
    }
}
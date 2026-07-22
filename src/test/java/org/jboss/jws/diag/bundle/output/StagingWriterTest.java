package org.jboss.jws.diag.bundle.output;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;
import org.jboss.jws.diag.common.RedactionLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StagingWriterTest {

    private final StagingWriter writer = new StagingWriter();

    private BundleContext contextFor(Path stagingDir) {
        return new BundleContext(
                Path.of("/dummy"), Path.of("/dummy"), stagingDir, RedactionLevel.DEFAULT);
    }

    private CollectedFile redactedFile(String relativeArchivePath, String content) {
        return CollectedFile.builder()
                .relativeArchivePath(relativeArchivePath)
                .sourcePath(Path.of("/dummy"))
                .type(CollectedFile.Type.XML_CONFIG)
                .content("<Server/>")
                .build()
                .withContent(content); // withContent marks it as redacted
    }

    private CollectedFile unredactedFile(String relativeArchivePath, String content) {
        return CollectedFile.builder()
                .relativeArchivePath(relativeArchivePath)
                .sourcePath(Path.of("/dummy"))
                .type(CollectedFile.Type.XML_CONFIG)
                .content(content)
                .build();
    }

    @Test
    void shouldWriteRedactedFileToStagingDir(@TempDir Path stagingDir) throws IOException {
        CollectedFile file = redactedFile("conf/server.xml", "<Server password=\"[REDACTED]\"/>");

        writer.write(file, contextFor(stagingDir));

        Path written = stagingDir.resolve("conf/server.xml");
        assertThat(written).exists();
        assertThat(Files.readString(written, StandardCharsets.UTF_8))
                .isEqualTo("<Server password=\"[REDACTED]\"/>");
    }

    @Test
    void shouldCreateNestedDirectoriesWhenTheyDoNotExist(@TempDir Path stagingDir) throws IOException {
        CollectedFile file = redactedFile("logs/catalina.out", "log content");

        writer.write(file, contextFor(stagingDir));

        assertThat(stagingDir.resolve("logs")).isDirectory();
        assertThat(stagingDir.resolve("logs/catalina.out")).exists();
    }

    @Test
    void shouldThrowWhenFileIsNotRedacted(@TempDir Path stagingDir) {
        CollectedFile file = unredactedFile("conf/server.xml", "<Server password=\"Secret\"/>");

        assertThatThrownBy(() -> writer.write(file, contextFor(stagingDir)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("conf/server.xml");
    }

    @Test
    void shouldNotWriteAnyFileWhenNotRedacted(@TempDir Path stagingDir) {
        CollectedFile file = unredactedFile("conf/server.xml", "<Server password=\"Secret\"/>");

        assertThatThrownBy(() -> writer.write(file, contextFor(stagingDir)));

        assertThat(stagingDir.resolve("conf/server.xml")).doesNotExist();
    }

    @Test
    void shouldWriteMultipleFilesIndependently(@TempDir Path stagingDir) throws IOException {
        writer.write(redactedFile("conf/server.xml", "<Server/>"), contextFor(stagingDir));
        writer.write(redactedFile("conf/tomcat-users.xml", "<tomcat-users/>"), contextFor(stagingDir));

        assertThat(stagingDir.resolve("conf/server.xml")).exists();
        assertThat(stagingDir.resolve("conf/tomcat-users.xml")).exists();
    }
}
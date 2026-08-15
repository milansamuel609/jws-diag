package org.jboss.jws.diag.bundle.output;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ArchiveWriterTest {

    private final ArchiveWriter writer = new ArchiveWriter();

    private void writeFile(Path stagingDir, String relativePath, String content) throws IOException {
        Path file = stagingDir.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }

    @Test
    void shouldCreateTarGzArchive(@TempDir Path stagingDir,
                                  @TempDir Path outputDir) throws IOException {

        writeFile(stagingDir, "conf/server.xml", "<Server/>");

        Path archive = outputDir.resolve("bundle.tar.gz");

        writer.createArchive(stagingDir, archive);

        assertThat(archive).exists();
        assertThat(Files.size(archive)).isGreaterThan(0);
    }

    @Test
    void shouldCreateArchiveContainingMultipleFiles(@TempDir Path stagingDir,
                                                    @TempDir Path outputDir) throws IOException {

        writeFile(stagingDir, "conf/server.xml", "<Server/>");
        writeFile(stagingDir, "conf/tomcat-users.xml", "<tomcat-users/>");
        writeFile(stagingDir, "manifest.json", "{}");

        Path archive = outputDir.resolve("bundle.tar.gz");

        writer.createArchive(stagingDir, archive);

        assertThat(archive).exists();
        assertThat(Files.size(archive)).isGreaterThan(0);
    }

    @Test
    void shouldCreateArchiveWhenStagingDirectoryIsEmpty(@TempDir Path stagingDir,
                                                        @TempDir Path outputDir) throws IOException {

        Path archive = outputDir.resolve("bundle.tar.gz");

        writer.createArchive(stagingDir, archive);

        assertThat(archive).exists();
        assertThat(Files.size(archive)).isGreaterThan(0);
    }

    @Test
    void shouldProduceValidGzipStream(@TempDir Path stagingDir,
                                      @TempDir Path outputDir) throws IOException {

        writeFile(stagingDir, "manifest.json", "{}");

        Path archive = outputDir.resolve("bundle.tar.gz");

        writer.createArchive(stagingDir, archive);

        try (GZIPInputStream in =
                     new GZIPInputStream(Files.newInputStream(archive))) {

            assertThat(in.read()).isNotEqualTo(-1);
        }
    }
}
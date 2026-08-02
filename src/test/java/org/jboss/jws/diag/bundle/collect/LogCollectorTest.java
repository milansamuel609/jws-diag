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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LogCollectorTest {

    private final LogCollector collector = new LogCollector();

    private BundleContext contextFor(Path catalinaBase) {
        return new BundleContext(
                catalinaBase, catalinaBase, catalinaBase.resolve("staging"), RedactionLevel.DEFAULT);
    }

    @Test
    void shouldCollectSupportedLogFiles(@TempDir Path catalinaBase) throws IOException {
        Path logs = catalinaBase.resolve("logs");
        Files.createDirectories(logs);

        Files.writeString(logs.resolve("catalina.out"), "line1");
        Files.writeString(logs.resolve("localhost.2026-08-02.log"), "line2");
        Files.writeString(logs.resolve("manager.2026-08-02.log"), "line3");
        Files.writeString(logs.resolve("host-manager.2026-08-02.log"), "line4");

        List<CollectedFile> files = collector.collectLogFiles(contextFor(catalinaBase));

        assertThat(files).hasSize(4);
        assertThat(files).extracting(CollectedFile::getType).containsOnly(CollectedFile.Type.LOG);
    }

    @Test
    void shouldIgnoreUnsupportedFiles(@TempDir Path catalinaBase) throws IOException {
        Path logs = catalinaBase.resolve("logs");
        Files.createDirectories(logs);

        Files.writeString(logs.resolve("gc.log"), "gc");
        Files.writeString(logs.resolve("random.txt"), "abc");

        List<CollectedFile> files = collector.collectLogFiles(contextFor(catalinaBase));

        assertThat(files).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenLogsDirectoryDoesNotExist(@TempDir Path catalinaBase) throws IOException {
        List<CollectedFile> files = collector.collectLogFiles(contextFor(catalinaBase));

        assertThat(files).isEmpty();
    }

    @Test
    void shouldKeepOnlyLastTenThousandLines(@TempDir Path catalinaBase) throws IOException {
        Path logs = catalinaBase.resolve("logs");
        Files.createDirectories(logs);

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= 15000; i++) {
            builder.append("Line ").append(i).append(System.lineSeparator());
        }

        Files.writeString(logs.resolve("catalina.out"), builder.toString(), StandardCharsets.UTF_8);

        List<CollectedFile> files = collector.collectLogFiles(contextFor(catalinaBase));

        assertThat(files).hasSize(1);

        List<String> keptLines = Arrays.asList(files.get(0).getContent().split(System.lineSeparator()));

        assertThat(keptLines).hasSize(10_000);
        assertThat(keptLines.get(0)).isEqualTo("Line 5001");
        assertThat(keptLines.get(keptLines.size() - 1)).isEqualTo("Line 15000");
        assertThat(keptLines).doesNotContain("Line 5000");
    }

    @Test
    void shouldSetRelativeArchivePathUnderLogsDirectory(@TempDir Path catalinaBase) throws IOException {
        Path logs = catalinaBase.resolve("logs");
        Files.createDirectories(logs);
        Files.writeString(logs.resolve("catalina.out"), "some log content");

        List<CollectedFile> files = collector.collectLogFiles(contextFor(catalinaBase));

        assertThat(files).hasSize(1);
        assertThat(files.get(0).getRelativeArchivePath()).isEqualTo("logs/catalina.out");
    }

    @Test
    void shouldMarkCollectedLogFilesAsNotYetRedacted(@TempDir Path catalinaBase) throws IOException {
        Path logs = catalinaBase.resolve("logs");
        Files.createDirectories(logs);
        Files.writeString(logs.resolve("catalina.out"), "some log content");

        List<CollectedFile> files = collector.collectLogFiles(contextFor(catalinaBase));

        assertThat(files)
                .extracting(CollectedFile::isRedacted)
                .containsOnly(false);
    }
}
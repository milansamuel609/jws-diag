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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class LogCollectorTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(
                    Instant.parse("2026-08-08T12:00:00Z"),
                    ZoneOffset.UTC);

    private final LogCollector collector =
            new LogCollector(FIXED_CLOCK);

    private BundleContext contextFor(Path catalinaBase) {
        return new BundleContext(
                catalinaBase,
                catalinaBase,
                catalinaBase.resolve("staging"),
                RedactionLevel.DEFAULT);
    }

    private String header(String date, String message) {
        return date
                + " 12:00:00.123 INFO [main] "
                + "some.Logger "
                + message;
    }

    private List<String> daysWorthOfLines(
            String date,
            int count,
            String labelPrefix) {

        return IntStream.rangeClosed(1, count)
                .mapToObj(i ->
                        header(
                                date,
                                labelPrefix + " line " + i))
                .collect(Collectors.toList());
    }

    @Test
    void shouldCollectSupportedLogFiles(
            @TempDir Path catalinaBase) throws IOException {

        Path logs = catalinaBase.resolve("logs");
        Files.createDirectories(logs);

        Files.writeString(
                logs.resolve("catalina.out"),
                header("2026-08-07", "line1"));

        Files.writeString(
                logs.resolve("localhost.2026-08-02.log"),
                "line2");

        Files.writeString(
                logs.resolve("manager.2026-08-02.log"),
                "line3");

        Files.writeString(
                logs.resolve("host-manager.2026-08-02.log"),
                "line4");

        List<CollectedFile> files =
                collector.collectLogFiles(
                        contextFor(catalinaBase));

        assertThat(files).hasSize(4);

        assertThat(files)
                .extracting(CollectedFile::getType)
                .containsOnly(CollectedFile.Type.LOG);
    }

    @Test
    void shouldIgnoreUnsupportedFiles(
            @TempDir Path catalinaBase) throws IOException {

        Path logs = catalinaBase.resolve("logs");
        Files.createDirectories(logs);

        Files.writeString(
                logs.resolve("gc.log"),
                "gc");

        Files.writeString(
                logs.resolve("random.txt"),
                "abc");

        List<CollectedFile> files =
                collector.collectLogFiles(
                        contextFor(catalinaBase));

        assertThat(files).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenLogsDirectoryDoesNotExist(
            @TempDir Path catalinaBase) throws IOException {

        List<CollectedFile> files =
                collector.collectLogFiles(
                        contextFor(catalinaBase));

        assertThat(files).isEmpty();
    }

    @Test
    void shouldExcludeTodaysEntriesFromCatalinaOut(
            @TempDir Path catalinaBase) throws IOException {

        Path logs = catalinaBase.resolve("logs");
        Files.createDirectories(logs);

        String content = String.join(
                System.lineSeparator(),
                header(
                        "2026-08-07",
                        "yesterday, should be kept"),
                header(
                        "2026-08-08",
                        "today, should be excluded"));

        Files.writeString(
                logs.resolve("catalina.out"),
                content,
                StandardCharsets.UTF_8);

        List<CollectedFile> files =
                collector.collectLogFiles(
                        contextFor(catalinaBase));

        String kept =
                files.get(0).getContent();

        assertThat(kept)
                .contains("yesterday, should be kept")
                .doesNotContain("today, should be excluded");
    }

    @Test
    void shouldKeepPreviousThreeCalendarDays(@TempDir Path catalinaBase) throws IOException {
        Path logs = catalinaBase.resolve("logs");
        Files.createDirectories(logs);

        List<String> allLines = new ArrayList<>();

        allLines.addAll(daysWorthOfLines("2026-08-04", 100, "Aug4"));
        allLines.addAll(daysWorthOfLines("2026-08-05", 100, "Aug5"));
        allLines.addAll(daysWorthOfLines("2026-08-06", 100, "Aug6"));
        allLines.addAll(daysWorthOfLines("2026-08-07", 100, "Aug7"));
        allLines.addAll(daysWorthOfLines("2026-08-08", 100, "Aug8"));

        Files.writeString(
                logs.resolve("catalina.out"),
                String.join(System.lineSeparator(), allLines),
                StandardCharsets.UTF_8);

        List<CollectedFile> files =
                collector.collectLogFiles(contextFor(catalinaBase));

        assertThat(files).hasSize(1);

        String kept = files.get(0).getContent();

        assertThat(kept)
                .contains("Aug5")
                .contains("Aug6")
                .contains("Aug7")
                .doesNotContain("Aug4")
                .doesNotContain("Aug8");
    }

    @Test
    void shouldStopAtTenThousandLinesWithinThreeDayWindow(
            @TempDir Path catalinaBase) throws IOException {

        Path logs = catalinaBase.resolve("logs");
        Files.createDirectories(logs);

        List<String> allLines =
                new ArrayList<>();

        allLines.addAll(
                daysWorthOfLines(
                        "2026-08-04",
                        500,
                        "Aug4"));

        allLines.addAll(
                daysWorthOfLines(
                        "2026-08-05",
                        6000,
                        "Aug5"));

        allLines.addAll(
                daysWorthOfLines(
                        "2026-08-06",
                        4000,
                        "Aug6"));

        allLines.addAll(
                daysWorthOfLines(
                        "2026-08-07",
                        3000,
                        "Aug7"));

        allLines.addAll(
                daysWorthOfLines(
                        "2026-08-08",
                        2000,
                        "Aug8"));

        Files.writeString(
                logs.resolve("catalina.out"),
                String.join(
                        System.lineSeparator(),
                        allLines),
                StandardCharsets.UTF_8);

        List<CollectedFile> files =
                collector.collectLogFiles(
                        contextFor(catalinaBase));

        List<String> kept =
                Arrays.asList(
                        files.get(0)
                                .getContent()
                                .split(
                                        System.lineSeparator()));

        assertThat(kept).hasSize(10_000);

        assertThat(kept)
                .noneMatch(l -> l.contains("Aug8"));

        assertThat(kept)
                .noneMatch(l -> l.contains("Aug4"));

        assertThat(kept)
                .filteredOn(l -> l.contains("Aug7"))
                .hasSize(3000);

        assertThat(kept)
                .filteredOn(l -> l.contains("Aug6"))
                .hasSize(4000);

        assertThat(kept)
                .filteredOn(l -> l.contains("Aug5"))
                .hasSize(3000);

        assertThat(kept)
                .filteredOn(
                        l -> l.contains("Aug5 line 3001"))
                .hasSize(1);

        assertThat(kept)
                .noneMatch(
                        l -> l.contains("Aug5 line 3000"));
    }

    @Test
    void shouldStopAtThreeDayWindowWhenUnderTenThousandLines(
            @TempDir Path catalinaBase) throws IOException {

        Path logs = catalinaBase.resolve("logs");
        Files.createDirectories(logs);

        List<String> allLines =
                new ArrayList<>();

        allLines.addAll(
                daysWorthOfLines(
                        "2026-08-04",
                        200,
                        "Aug4"));

        allLines.addAll(
                daysWorthOfLines(
                        "2026-08-05",
                        800,
                        "Aug5"));

        allLines.addAll(
                daysWorthOfLines(
                        "2026-08-06",
                        1500,
                        "Aug6"));

        allLines.addAll(
                daysWorthOfLines(
                        "2026-08-07",
                        2000,
                        "Aug7"));

        allLines.addAll(
                daysWorthOfLines(
                        "2026-08-08",
                        1000,
                        "Aug8"));

        Files.writeString(
                logs.resolve("catalina.out"),
                String.join(
                        System.lineSeparator(),
                        allLines),
                StandardCharsets.UTF_8);

        List<CollectedFile> files =
                collector.collectLogFiles(
                        contextFor(catalinaBase));

        List<String> kept =
                Arrays.asList(
                        files.get(0)
                                .getContent()
                                .split(
                                        System.lineSeparator()));

        assertThat(kept).hasSize(4300);

        assertThat(kept)
                .noneMatch(l -> l.contains("Aug4"));

        assertThat(kept)
                .noneMatch(l -> l.contains("Aug8"));

        assertThat(kept)
                .filteredOn(l -> l.contains("Aug5"))
                .hasSize(800);

        assertThat(kept)
                .filteredOn(l -> l.contains("Aug6"))
                .hasSize(1500);

        assertThat(kept)
                .filteredOn(l -> l.contains("Aug7"))
                .hasSize(2000);
    }

    @Test
    void shouldKeepStackTraceLinesGroupedWithTheirHeaderEntry(
            @TempDir Path catalinaBase) throws IOException {

        Path logs = catalinaBase.resolve("logs");
        Files.createDirectories(logs);

        String content = String.join(
                System.lineSeparator(),
                header(
                        "2026-08-04",
                        "old entry, excluded"),

                header(
                        "2026-08-06",
                        "entry with a stack trace"),

                "\tat com.example.Foo.bar(Foo.java:42)",

                "\tat com.example.Baz.qux(Baz.java:17)");

        Files.writeString(
                logs.resolve("catalina.out"),
                content,
                StandardCharsets.UTF_8);

        List<CollectedFile> files =
                collector.collectLogFiles(
                        contextFor(catalinaBase));

        String kept =
                files.get(0).getContent();

        assertThat(kept)
                .doesNotContain("old entry, excluded")
                .contains("entry with a stack trace")
                .contains("com.example.Foo.bar")
                .contains("com.example.Baz.qux");
    }

    @Test
    void shouldNotApplyDateCutoffToNonCatalinaOutFiles(
            @TempDir Path catalinaBase) throws IOException {

        Path logs = catalinaBase.resolve("logs");
        Files.createDirectories(logs);

        Files.writeString(
                logs.resolve("localhost.2026-08-02.log"),
                header(
                        "2020-01-01",
                        "very old entry, but this file has no date-cutoff rule"),
                StandardCharsets.UTF_8);

        List<CollectedFile> files =
                collector.collectLogFiles(
                        contextFor(catalinaBase));

        assertThat(files).hasSize(1);

        assertThat(files.get(0).getContent())
                .contains("very old entry");
    }

    @Test
    void shouldSetRelativeArchivePathUnderLogsDirectory(
            @TempDir Path catalinaBase) throws IOException {

        Path logs = catalinaBase.resolve("logs");
        Files.createDirectories(logs);

        Files.writeString(
                logs.resolve("catalina.out"),
                header(
                        "2026-08-07",
                        "some log content"));

        List<CollectedFile> files =
                collector.collectLogFiles(
                        contextFor(catalinaBase));

        assertThat(files).hasSize(1);

        assertThat(
                files.get(0)
                        .getRelativeArchivePath())
                .isEqualTo("logs/catalina.out");
    }

    @Test
    void shouldMarkCollectedLogFilesAsNotYetRedacted(
            @TempDir Path catalinaBase) throws IOException {

        Path logs = catalinaBase.resolve("logs");
        Files.createDirectories(logs);

        Files.writeString(
                logs.resolve("catalina.out"),
                header(
                        "2026-08-07",
                        "some log content"));

        List<CollectedFile> files =
                collector.collectLogFiles(
                        contextFor(catalinaBase));

        assertThat(files)
                .extracting(CollectedFile::isRedacted)
                .containsOnly(false);
    }
}
package org.jboss.jws.diag.bundle.manifest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OsInfoReaderTest {

    private final OsInfoReader reader = new OsInfoReader();

    private Path writeFakeOsRelease(Path tempDir, List<String> lines) throws IOException {
        Path osReleaseFile = tempDir.resolve("os-release");
        Files.write(osReleaseFile, lines, StandardCharsets.UTF_8);
        return osReleaseFile;
    }

    @Test
    void shouldReadPrettyNameFromOsRelease(@TempDir Path tempDir) throws IOException {
        Path osReleaseFile = writeFakeOsRelease(tempDir, List.of(
                "NAME=\"Red Hat Enterprise Linux\"",
                "VERSION_ID=\"9\"",
                "PRETTY_NAME=\"Red Hat Enterprise Linux 9\""
        ));

        String result = reader.readOsInfo(osReleaseFile);

        assertThat(result).isEqualTo("Red Hat Enterprise Linux 9");
    }

    @Test
    void shouldReadPrettyNameFromUbuntuStyleOsRelease(@TempDir Path tempDir) throws IOException {
        Path osReleaseFile = writeFakeOsRelease(tempDir, List.of(
                "NAME=\"Ubuntu\"",
                "VERSION=\"22.04.3 LTS (Jammy Jellyfish)\"",
                "PRETTY_NAME=\"Ubuntu 22.04.3 LTS\""
        ));

        String result = reader.readOsInfo(osReleaseFile);

        assertThat(result).isEqualTo("Ubuntu 22.04.3 LTS");
    }

    @Test
    void shouldFallBackToOsNamePropertyWhenFileDoesNotExist(@TempDir Path tempDir) {
        Path nonExistentFile = tempDir.resolve("does-not-exist");

        String result = reader.readOsInfo(nonExistentFile);

        assertThat(result).isEqualTo(System.getProperty("os.name"));
    }

    @Test
    void shouldFallBackToOsNamePropertyWhenPrettyNameMissing(@TempDir Path tempDir) throws IOException {
        Path osReleaseFile = writeFakeOsRelease(tempDir, List.of(
                "NAME=\"SomeDistro\"",
                "VERSION_ID=\"1\""
        ));

        String result = reader.readOsInfo(osReleaseFile);

        assertThat(result).isEqualTo(System.getProperty("os.name"));
    }

    @Test
    void shouldHandleUnquotedPrettyNameValue(@TempDir Path tempDir) throws IOException {
        Path osReleaseFile = writeFakeOsRelease(tempDir, List.of(
                "PRETTY_NAME=Alpine Linux v3.19"
        ));

        String result = reader.readOsInfo(osReleaseFile);

        assertThat(result).isEqualTo("Alpine Linux v3.19");
    }

    @Test
    void shouldReadPublicReadOsInfoMethodWithoutThrowing() {
        String result = reader.readOsInfo();

        assertThat(result).isNotNull().isNotBlank();
    }
}
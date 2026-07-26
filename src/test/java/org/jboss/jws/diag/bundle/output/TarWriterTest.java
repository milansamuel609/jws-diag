package org.jboss.jws.diag.bundle.output;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TarWriterTest {

    @Test
    void shouldWriteFileEntry() throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (TarWriter writer = new TarWriter(out)) {
            writer.writeFileEntry("server.xml",
                    "<Server/>".getBytes(StandardCharsets.UTF_8));
            writer.writeEndOfArchive();
        }

        assertThat(out.toByteArray()).isNotEmpty();
    }

    @Test
    void shouldWriteEndOfArchiveBlocks() throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (TarWriter writer = new TarWriter(out)) {
            writer.writeEndOfArchive();
        }

        byte[] archive = out.toByteArray();

        assertThat(archive.length).isEqualTo(1024);
    }

    @Test
    void shouldFlagEntryNameLongerThanUstarLimit() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String longName = "a".repeat(100);

        assertThatThrownBy(() -> {
            try (TarWriter writer = new TarWriter(out)) {
                writer.writeFileEntry(longName,
                        new byte[]{1, 2, 3});
            }
        }).isInstanceOf(IOException.class)
                .hasMessageContaining("Entry name too long");
    }

    @Test
    void shouldReadAllBytes(@TempDir Path tempDir) throws IOException {

        Path file = tempDir.resolve("sample.txt");

        Files.writeString(file,
                "hello",
                StandardCharsets.UTF_8);

        byte[] bytes = TarWriter.readAllBytes(file);

        assertThat(new String(bytes, StandardCharsets.UTF_8))
                .isEqualTo("hello");
    }

    @Test
    void shouldCloseUnderlyingOutputStream() throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TarWriter writer = new TarWriter(out);

        writer.close();

        assertThat(out).isNotNull();
    }
}
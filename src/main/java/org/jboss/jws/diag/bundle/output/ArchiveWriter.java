package org.jboss.jws.diag.bundle.output;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;


public final class ArchiveWriter {

    public void createArchive(Path stagingDir, Path outputArchive) throws IOException {
        try (OutputStream fileOut = Files.newOutputStream(outputArchive);
             GZIPOutputStream gzipOut = new GZIPOutputStream(fileOut);
             TarWriter tarWriter = new TarWriter(gzipOut)) {

            try (Stream<Path> walk = Files.walk(stagingDir)) {
                walk.filter(Files::isRegularFile).forEach(file -> {
                    try {
                        String entryName = stagingDir.relativize(file).toString().replace('\\', '/');
                        byte[] content = TarWriter.readAllBytes(file);
                        tarWriter.writeFileEntry(entryName, content);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }

            tarWriter.writeEndOfArchive();
        }
    }
}
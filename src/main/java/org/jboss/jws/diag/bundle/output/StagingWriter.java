package org.jboss.jws.diag.bundle.output;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class StagingWriter {

    public void write(CollectedFile file, BundleContext context) throws IOException {
        if (!file.isRedacted()) {
            throw new IllegalStateException(
                    "Refusing to stage unredacted file: " + file.getRelativeArchivePath());
        }

        Path destination = context.getStagingDir().resolve(file.getRelativeArchivePath());
        Files.createDirectories(destination.getParent());
        Files.writeString(destination, file.getContent(), StandardCharsets.UTF_8);
    }
}

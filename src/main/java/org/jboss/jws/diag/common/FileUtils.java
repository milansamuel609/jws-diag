package org.jboss.jws.diag.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class FileUtils {

    private FileUtils() {
    }

    public static Optional<String> readFileIfExists(Path path) {
        if (path == null || !Files.isRegularFile(path) || !Files.isReadable(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static Path resolveConfigFile(Path catalinaBase, String relativePath) {
        return catalinaBase.resolve("conf").resolve(relativePath);
    }
}

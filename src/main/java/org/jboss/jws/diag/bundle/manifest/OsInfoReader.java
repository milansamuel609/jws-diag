package org.jboss.jws.diag.bundle.manifest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class OsInfoReader {

    private static final Path OS_RELEASE_PATH = Path.of("/etc/os-release");
    private static final String PRETTY_NAME_KEY = "PRETTY_NAME";

    public String readOsInfo() {
        return readOsInfo(OS_RELEASE_PATH);
    }

    String readOsInfo(Path osReleasePath) {
        if (!Files.exists(osReleasePath)) {
            return System.getProperty("os.name");
        }

        try {
            List<String> lines = Files.readAllLines(osReleasePath);
            for (String line : lines) {
                if (line.startsWith(PRETTY_NAME_KEY + "=")) {
                    return removeQuotes(line.substring((PRETTY_NAME_KEY + "=").length()));
                }
            }
        } catch (IOException e) {
            System.err.println("[WARN] Could not read " + osReleasePath + ": " + e.getMessage());
        }

        return System.getProperty("os.name");
    }

    private String removeQuotes(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
}
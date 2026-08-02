package org.jboss.jws.diag.bundle.collect;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class LogCollector {

    private static final int MAX_LINES = 10_000;

    private static final String LOG_DIRECTORY = "logs";

    private static final String[] LOG_PREFIXES = {
            "catalina",
            "localhost",
            "manager",
            "host-manager"
    };

    public List<CollectedFile> collectLogFiles(BundleContext context) throws IOException {

        List<CollectedFile> files = new ArrayList<>();

        Path logsDir = context.getCatalinaBase().resolve(LOG_DIRECTORY);

        if (!Files.exists(logsDir)) {
            System.err.println("[WARN] Logs directory not found, skipping: " + logsDir);
            return files;
        }

        try (Stream<Path> stream = Files.list(logsDir)) {

            stream.filter(Files::isRegularFile)
                    .filter(this::isSupportedLogFile)
                    .forEach(path -> {
                        try {

                            String relativePath =
                                    LOG_DIRECTORY + "/" + path.getFileName();

                            String content = readLastLines(path);

                            files.add(
                                    CollectedFile.builder()
                                            .relativeArchivePath(relativePath)
                                            .sourcePath(path)
                                            .type(CollectedFile.Type.LOG)
                                            .content(content)
                                            .build());

                        } catch (IOException e) {
                            System.err.println(
                                    "[WARN] Could not read log file: "
                                            + path + ": " + e.getMessage());
                        }
                    });
        }

        return files;
    }

    private boolean isSupportedLogFile(Path path) {

        String fileName = path.getFileName().toString().toLowerCase();

        boolean supportedPrefix = false;

        for (String prefix : LOG_PREFIXES) {
            if (fileName.startsWith(prefix)) {
                supportedPrefix = true;
                break;
            }
        }

        if (!supportedPrefix) {
            return false;
        }

        return fileName.endsWith(".log")
                || fileName.equals("catalina.out");
    }

    private String readLastLines(Path path) throws IOException {

        ArrayDeque<String> lines = new ArrayDeque<>(MAX_LINES);

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (lines.size() == MAX_LINES) {
                    lines.removeFirst();
                }

                lines.addLast(line);
            }
        }

        return String.join(System.lineSeparator(), lines);
    }
}
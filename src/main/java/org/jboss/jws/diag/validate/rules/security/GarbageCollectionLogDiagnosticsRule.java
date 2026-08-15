package org.jboss.jws.diag.validate.rules.security;

import org.jboss.jws.diag.common.RuleId;
import org.jboss.jws.diag.common.Severity;
import org.jboss.jws.diag.validate.Rule;
import org.jboss.jws.diag.validate.RuleContext;
import org.jboss.jws.diag.validate.model.Finding;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GarbageCollectionLogDiagnosticsRule implements Rule {

    @Override
    public List<Finding> evaluate(RuleContext ctx) {
        Path logsDirectory = ctx.getCatalinaBase().resolve("logs");
        List<Finding> findings = new ArrayList<>();

        if (!Files.isDirectory(logsDirectory)) {
            return findings;
        }

        try (var files = Files.list(logsDirectory)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);

                        return fileName.startsWith("gc") && fileName.endsWith(".log");
                    })
                    .forEach(path -> {
                        try {
                            String content =
                                    Files.readString(path);

                            if (content.contains("OutOfMemoryError")
                                    || content.contains("Full GC")
                                    || content.contains("Pause Full")
                                    || content.contains("Allocation Failure")
                                    || content.contains("to-space exhausted")
                                    || content.contains("GC overhead limit exceeded")) {

                                findings.add(Finding.builder()
                                        .ruleId(RuleId.SEC_009)
                                        .category("Security")
                                        .severity(Severity.WARN)
                                        .summary("Garbage Collection Log Diagnostics")
                                        .detail("GC log contains operational warning indicators: "
                                                + path.getFileName())
                                        .file(path.toString())
                                        .fix("Investigate repeated Full GC, allocation failures, "
                                                + "excessive GC pauses, or memory exhaustion indicators.")
                                        .build());
                            }

                        } catch (IOException e) {
                            System.err.println(
                                    "[WARN] Could not read GC log: "
                                            + path + ": " + e.getMessage());
                        }
                    });

        } catch (IOException e) {
            System.err.println(
                    "[WARN] Could not scan GC logs: "
                            + e.getMessage());
        }

        return findings;
    }
}
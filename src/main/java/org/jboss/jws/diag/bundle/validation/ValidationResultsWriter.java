package org.jboss.jws.diag.bundle.validation;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.common.ExitCodes;
import org.jboss.jws.diag.common.Severity;
import org.jboss.jws.diag.validate.ValidationEngine;
import org.jboss.jws.diag.validate.model.Finding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ValidationResultsWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private static final DefaultPrettyPrinter PRINTER;

    static {
        PRINTER = new DefaultPrettyPrinter();
        PRINTER.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
    }

    private final ValidationEngine validationEngine;

    public ValidationResultsWriter() {
        this.validationEngine = new ValidationEngine();
    }

    public void write(BundleContext context) throws IOException {

        List<Finding> findings =
                validationEngine.validate(context.getCatalinaBase());

        int exitCode = determineExitCode(findings);

        long errors = findings.stream()
                .filter(f -> f.getSeverity() == Severity.ERROR)
                .count();

        long warnings = findings.stream()
                .filter(f -> f.getSeverity() == Severity.WARN)
                .count();

        long info = findings.stream()
                .filter(f -> f.getSeverity() == Severity.INFO)
                .count();

        Map<String, Object> summaryMap = new LinkedHashMap<>();
        summaryMap.put("errors", errors);
        summaryMap.put("warnings", warnings);
        summaryMap.put("info", info);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("findings", findings);
        output.put("summary", summaryMap);
        output.put("exitCode", exitCode);

        String json = MAPPER.writer(PRINTER).writeValueAsString(output);

        Path destination = context.getStagingDir()
                .resolve("validation-results.json");

        Files.writeString(destination, json, StandardCharsets.UTF_8);
    }

    private int determineExitCode(List<Finding> findings) {
        int highestCode = ExitCodes.OK;

        for (Finding finding : findings) {
            if (finding.getSeverity() == Severity.ERROR) {
                highestCode = ExitCodes.ERRORS;
            } else if (finding.getSeverity() == Severity.WARN
                    && highestCode < ExitCodes.ERRORS) {
                highestCode = ExitCodes.WARNINGS;
            }
        }

        return highestCode;
    }
}
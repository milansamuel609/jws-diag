package org.jboss.jws.diag.validate;

import org.jboss.jws.diag.common.ExitCodes;
import org.jboss.jws.diag.common.OutputFormatMixin;
import org.jboss.jws.diag.common.Severity;
import org.jboss.jws.diag.validate.model.Finding;
import org.jboss.jws.diag.validate.output.HumanReadableOutput;
import org.jboss.jws.diag.validate.output.JsonOutput;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Command(name = "validate",
        description = "Run diagnostic rules against configuration and report findings (INFO/WARN/ERROR)",
        mixinStandardHelpOptions = true)
public class ValidateCommand implements Runnable {

    @CommandLine.Option(names = "--catalina-base", description = "Path to CATALINA_BASE (defaults to $CATALINA_BASE env var)")
    private Path catalinaBase;

    @Mixin
    private OutputFormatMixin outputFormat;

    @Override
    public void run() {
        Path resolvedCatalinaBase = resolveCatalinaBase();

        ValidationEngine validationEngine = new ValidationEngine();

        List<Finding> findings =
                validationEngine.validate(resolvedCatalinaBase);

        int exitCode = determineExitCode(findings);

        switch (outputFormat.getFormat()) {
            case HUMAN:
                new HumanReadableOutput().print(findings);
                break;
            case JSON:
                new JsonOutput().print(findings, exitCode);
                break;
        }

        System.exit(exitCode);
    }

    private Path resolveCatalinaBase() {
        if (catalinaBase != null) {
            return catalinaBase;
        }

        String envValue = System.getenv("CATALINA_BASE");
        if (envValue == null || envValue.isBlank()) {
            System.err.println("[ERROR] Could not determine CATALINA_BASE. "
                    + "Use --catalina-base, or set the CATALINA_BASE environment variable.");
            System.exit(ExitCodes.ERRORS);
        }

        return Paths.get(envValue);
    }

    public int determineExitCode(List<Finding> findings) {
        int highestCode = ExitCodes.OK;

        for (Finding finding : findings) {
            if (finding.getSeverity() == Severity.ERROR) {
                highestCode = ExitCodes.ERRORS;
            } else if (finding.getSeverity() == Severity.WARN && highestCode < ExitCodes.ERRORS) {
                highestCode = ExitCodes.WARNINGS;
            }
        }

        return highestCode;
    }
}
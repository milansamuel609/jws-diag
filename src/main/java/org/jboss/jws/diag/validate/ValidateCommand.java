package org.jboss.jws.diag.validate;

import org.jboss.jws.diag.common.ExitCodes;
import org.jboss.jws.diag.common.OutputFormatMixin;
import org.jboss.jws.diag.common.Severity;
import org.jboss.jws.diag.validate.model.Finding;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.util.ArrayList;
import java.util.List;

@Command(name = "validate",
        description = "Run diagnostic rules against configuration and report findings (INFO/WARN/ERROR)",
        mixinStandardHelpOptions = true)
public class ValidateCommand implements Runnable {

    @Mixin
    private OutputFormatMixin outputFormat;

    @Override
    public void run() {
        List<Finding> findings = new ArrayList<>();

        int exitCode = determineExitCode(findings);
        System.exit(exitCode);
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

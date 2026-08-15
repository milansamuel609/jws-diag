package org.jboss.jws.diag.validate;

import org.jboss.jws.diag.common.ExitCodes;
import org.jboss.jws.diag.common.Severity;
import org.jboss.jws.diag.validate.model.Finding;

import java.util.List;

public final class ExitCodeCalculator {

    private ExitCodeCalculator() {
    }

    public static int determineExitCode(List<Finding> findings) {
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
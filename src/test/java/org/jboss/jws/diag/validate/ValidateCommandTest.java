package org.jboss.jws.diag.validate;

import org.jboss.jws.diag.common.ExitCodes;
import org.jboss.jws.diag.common.SeverityLevels;

import org.jboss.jws.diag.validate.model.Finding;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidateCommandTest {

    private final ValidateCommand validateCommand = new ValidateCommand();

    @Test
    void shouldReturnOkWhenNoFindingsArePresent() {
        int result = validateCommand.determineExitCode(Collections.emptyList());
        assertThat(result).isEqualTo(ExitCodes.OK);
    }

    @Test
    void shouldReturnWarningWhenFindingsContainOnlyWarnings() {
        List<Finding> warningFindings = List.of(
                new Finding("CONN-001", "Connector", SeverityLevels.WARN, "Low threads check", "Compares maxThreads against available CPU cores rather than using a rigid static number"
                , "server.xml", "Adjust maxThreads upward to match your host hardware specifications.")
        );

        int result = validateCommand.determineExitCode(warningFindings);
        assertThat(result).isEqualTo(ExitCodes.WARNINGS);
    }

    @Test
    void shouldReturnErrorWhenAnyFindingsHasError() {
        List<Finding> errorFindings = List.of(
                new Finding("SEC-001", "Security", SeverityLevels.ERROR, "Root user check"
                , "Checks if the Tomcat process is running as root (UID 0)", "process state"
                        , "Run Tomcat as a dedicated, non-root system user.")
        );

        int result = validateCommand.determineExitCode(errorFindings);
        assertThat(result).isEqualTo(ExitCodes.ERRORS);
    }
}

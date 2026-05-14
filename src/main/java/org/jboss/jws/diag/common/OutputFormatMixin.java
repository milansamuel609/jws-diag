package org.jboss.jws.diag.common;

import picocli.CommandLine.Option;

public class OutputFormatMixin {

    @Option(names = {"--format", "-f"},
            description = "Output format: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})",
            defaultValue = "HUMAN")
    private OutputFormat format = OutputFormat.HUMAN;

    public OutputFormat getFormat() {
        return format;
    }
}

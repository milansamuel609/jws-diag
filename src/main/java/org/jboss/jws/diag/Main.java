package org.jboss.jws.diag;

import org.jboss.jws.diag.bundle.BundleCommand;
import org.jboss.jws.diag.common.ExitCodes;
import org.jboss.jws.diag.config.ConfigCommand;
import org.jboss.jws.diag.diff.DiffCommand;
import org.jboss.jws.diag.instances.InstancesCommand;
import org.jboss.jws.diag.logs.LogsCommand;
import org.jboss.jws.diag.modcluster.ModClusterCommand;
import org.jboss.jws.diag.summary.SummaryCommand;
import org.jboss.jws.diag.validate.ValidateCommand;
import picocli.CommandLine;

import java.io.PrintWriter;

@CommandLine.Command(
        name = "jws-diag",
        description = "Diagnostic and configuration validation toolkit for JBoss Web Server / Apache Tomcat",
        mixinStandardHelpOptions = true,
        version = "jws-diag 0.1.0-SNAPSHOT",
        subcommands = {
                SummaryCommand.class,
                ConfigCommand.class,
                ValidateCommand.class,
                BundleCommand.class,
                LogsCommand.class,
                InstancesCommand.class,
                ModClusterCommand.class,
                DiffCommand.class
        }
)
public class Main implements Runnable {

    private static final String ISSUES_URL = "https://github.com/web-servers/jws-diag/issues";

    @Override
    public void run() {
        new CommandLine(this).usage(System.out);
    }

    public static void main(String[] args) {
        System.exit(commandLine().execute(args));
    }

    /** The root command line, with picocli's error paths following {@link ExitCodes}. */
    static CommandLine commandLine() {
        CommandLine commandLine = new CommandLine(new Main());
        // The docs, the examples and the design document all write --format json.
        // Accept any casing rather than rejecting the spelling people type.
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        return withExitCodeContract(commandLine);
    }

    /**
     * Makes picocli's own error handling follow the exit code contract.
     *
     * <p>By default picocli exits {@code 2} for argument errors and {@code 1} when a command
     * throws. The contract reads those as error findings and warnings, but both mean the
     * tool could not run, which is {@link ExitCodes#TOOL_FAILURE}.
     *
     * <p>Handlers reach the subcommands registered when this is called, so call it after
     * the command hierarchy is complete.
     */
    static CommandLine withExitCodeContract(CommandLine commandLine) {
        commandLine.setParameterExceptionHandler(Main::handleParameterException);
        commandLine.setExecutionExceptionHandler(Main::handleExecutionException);
        return commandLine;
    }

    // Same output as picocli's default handler: the message, then either
    // "Did you mean" suggestions or the usage text. Only the exit code differs.
    private static int handleParameterException(CommandLine.ParameterException ex, String[] args) {
        CommandLine cmd = ex.getCommandLine();
        PrintWriter err = cmd.getErr();
        err.println(cmd.getColorScheme().errorText(ex.getMessage()));
        if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, err)) {
            cmd.usage(err, cmd.getColorScheme());
        }
        return ExitCodes.TOOL_FAILURE;
    }

    // Commands handle the failures they expect, so anything reaching here is a bug.
    // Keep the stack trace: it is what a useful bug report needs.
    private static int handleExecutionException(Exception ex, CommandLine cmd, CommandLine.ParseResult parseResult) {
        PrintWriter err = cmd.getErr();
        err.println("ERROR: " + cmd.getCommandSpec().qualifiedName() + " failed unexpectedly: " + ex);
        err.println("Please report this at " + ISSUES_URL + " and include the output below.");
        ex.printStackTrace(err);
        err.flush();
        return ExitCodes.TOOL_FAILURE;
    }
}

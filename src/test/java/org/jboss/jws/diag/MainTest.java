package org.jboss.jws.diag;

import org.jboss.jws.diag.common.ExitCodes;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class MainTest {

    @Test
    void helpExitsWithZero() {
        CommandLine cmd = new CommandLine(new Main());
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));
        int exitCode = cmd.execute("--help");
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void helpListsAllSubcommands() {
        CommandLine cmd = new CommandLine(new Main());
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));
        cmd.execute("--help");
        String output = sw.toString();
        assertThat(output).contains("summary");
        assertThat(output).contains("config");
        assertThat(output).contains("validate");
        assertThat(output).contains("bundle");
    }

    @Test
    void versionExitsWithZero() {
        CommandLine cmd = new CommandLine(new Main());
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));
        int exitCode = cmd.execute("--version");
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void summaryHelpExitsWithZero() {
        CommandLine cmd = new CommandLine(new Main());
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));
        int exitCode = cmd.execute("summary", "--help");
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void configHelpExitsWithZero() {
        CommandLine cmd = new CommandLine(new Main());
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));
        int exitCode = cmd.execute("config", "--help");
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void validateHelpExitsWithZero() {
        CommandLine cmd = new CommandLine(new Main());
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));
        int exitCode = cmd.execute("validate", "--help");
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void bundleHelpExitsWithZero() {
        CommandLine cmd = new CommandLine(new Main());
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));
        int exitCode = cmd.execute("bundle", "--help");
        assertThat(exitCode).isEqualTo(0);
    }

    // Argument errors and unexpected exceptions are tool failures under the exit code
    // contract. Picocli's defaults, 2 and 1, would read as error findings and warnings.

    @Test
    void unknownOption_exitsWithToolFailure() {
        assertThat(executeQuietly("summary", "--bogus")).isEqualTo(ExitCodes.TOOL_FAILURE);
    }

    @Test
    void invalidOptionValue_exitsWithToolFailure() {
        assertThat(executeQuietly("validate", "--format", "YAML")).isEqualTo(ExitCodes.TOOL_FAILURE);
    }

    @Test
    void unknownCommand_exitsWithToolFailure() {
        assertThat(executeQuietly("nosuchcommand")).isEqualTo(ExitCodes.TOOL_FAILURE);
    }

    @Test
    void subcommandOptionBeforeCommand_exitsWithToolFailure() {
        assertThat(executeQuietly("--format", "JSON", "summary")).isEqualTo(ExitCodes.TOOL_FAILURE);
    }

    @Test
    void parseError_keepsMessageAndUsage() {
        StringWriter err = new StringWriter();
        CommandLine cmd = Main.commandLine();
        cmd.setErr(new PrintWriter(err));

        cmd.execute("summary", "--bogus");

        assertThat(err.toString())
                .contains("Unknown option: '--bogus'")
                .contains("Usage: jws-diag summary");
    }

    @Test
    void unknownCommand_keepsSuggestions() {
        StringWriter err = new StringWriter();
        CommandLine cmd = Main.commandLine();
        cmd.setErr(new PrintWriter(err));

        cmd.execute("summry");

        assertThat(err.toString()).contains("Did you mean").contains("summary");
    }

    @Test
    void unexpectedException_exitsWithToolFailureAndAsksForReport() {
        CommandLine cmd = new CommandLine(new Main());
        cmd.addSubcommand("explode", new ExplodingCommand());
        Main.withExitCodeContract(cmd);
        StringWriter err = new StringWriter();
        cmd.setErr(new PrintWriter(err));

        int exitCode = cmd.execute("explode");

        assertThat(exitCode).isEqualTo(ExitCodes.TOOL_FAILURE);
        assertThat(err.toString())
                .contains("jws-diag explode failed unexpectedly")
                .contains("IllegalStateException: boom")
                .contains("github.com/web-servers/jws-diag/issues");
    }

    @Test
    void help_stillExitsWithZero() {
        assertThat(executeQuietly("--help")).isEqualTo(ExitCodes.OK);
        assertThat(executeQuietly("summary", "--help")).isEqualTo(ExitCodes.OK);
    }

    private static int executeQuietly(String... args) {
        CommandLine cmd = Main.commandLine();
        cmd.setOut(new PrintWriter(new StringWriter()));
        cmd.setErr(new PrintWriter(new StringWriter()));
        return cmd.execute(args);
    }

    @CommandLine.Command(name = "explode")
    static class ExplodingCommand implements Runnable {
        @Override
        public void run() {
            throw new IllegalStateException("boom");
        }
    }

    // Enum option values are matched without regard to case, because every example in
    // the README and docs writes --format json rather than --format JSON.

    @Test
    void formatValue_isAcceptedInAnyCase() {
        for (String value : new String[] {"json", "JSON", "Json", "human", "HUMAN"}) {
            CommandLine cmd = Main.commandLine();
            assertThatCode(() -> cmd.parseArgs("config", "--format", value))
                    .as("--format %s", value)
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void redactionLevelValue_isAcceptedInAnyCase() {
        for (String value : new String[] {"strict", "STRICT", "Default"}) {
            CommandLine cmd = Main.commandLine();
            assertThatCode(() -> cmd.parseArgs("bundle", "--redaction-level", value))
                    .as("--redaction-level %s", value)
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void unknownFormatValue_isStillRejected() {
        assertThat(executeQuietly("config", "--format", "yaml")).isEqualTo(ExitCodes.TOOL_FAILURE);
    }
}

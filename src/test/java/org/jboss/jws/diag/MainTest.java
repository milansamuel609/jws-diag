package org.jboss.jws.diag;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

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
}

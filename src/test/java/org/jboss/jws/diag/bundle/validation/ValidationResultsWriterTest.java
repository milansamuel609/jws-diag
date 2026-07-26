package org.jboss.jws.diag.bundle.validation;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.common.RedactionLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationResultsWriterTest {

    private final ValidationResultsWriter writer = new ValidationResultsWriter();

    private BundleContext contextFor(Path catalinaBase, Path stagingDir) {
        return new BundleContext(
                catalinaBase,
                catalinaBase,
                stagingDir,
                RedactionLevel.DEFAULT);
    }

    private void writeServerXml(Path catalinaBase) throws IOException {
        Path confDir = catalinaBase.resolve("conf");
        Files.createDirectories(confDir);

        Files.writeString(
                confDir.resolve("server.xml"),
                "<Server port=\"8005\" shutdown=\"SHUTDOWN\">" +
                        "<Service>" +
                        "<Connector port=\"8080\"/>" +
                        "</Service>" +
                        "</Server>",
                StandardCharsets.UTF_8);
    }

    @Test
    void shouldCreateValidationResultsJson(
            @TempDir Path catalinaBase,
            @TempDir Path stagingDir) throws IOException {

        writeServerXml(catalinaBase);

        writer.write(contextFor(catalinaBase, stagingDir));

        assertThat(stagingDir.resolve("validation-results.json")).exists();
    }

    @Test
    void shouldContainFindingsArray(
            @TempDir Path catalinaBase,
            @TempDir Path stagingDir) throws IOException {

        writeServerXml(catalinaBase);

        writer.write(contextFor(catalinaBase, stagingDir));

        String json = Files.readString(
                stagingDir.resolve("validation-results.json"));

        assertThat(json).contains("\"findings\"");
    }

    @Test
    void shouldContainSummarySection(
            @TempDir Path catalinaBase,
            @TempDir Path stagingDir) throws IOException {

        writeServerXml(catalinaBase);

        writer.write(contextFor(catalinaBase, stagingDir));

        String json = Files.readString(
                stagingDir.resolve("validation-results.json"));

        assertThat(json)
                .contains("\"summary\"")
                .contains("\"errors\"")
                .contains("\"warnings\"")
                .contains("\"info\"");
    }

    @Test
    void shouldContainExitCode(
            @TempDir Path catalinaBase,
            @TempDir Path stagingDir) throws IOException {

        writeServerXml(catalinaBase);

        writer.write(contextFor(catalinaBase, stagingDir));

        String json = Files.readString(
                stagingDir.resolve("validation-results.json"));

        assertThat(json).contains("\"exitCode\"");
    }
}
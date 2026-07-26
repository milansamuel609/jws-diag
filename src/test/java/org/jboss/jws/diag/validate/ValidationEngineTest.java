package org.jboss.jws.diag.validate;

import org.jboss.jws.diag.validate.model.Finding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationEngineTest {

    private final ValidationEngine engine = new ValidationEngine();

    private void writeServerXml(Path catalinaBase, String content) throws IOException {
        Path confDir = catalinaBase.resolve("conf");
        Files.createDirectories(confDir);
        Files.writeString(
                confDir.resolve("server.xml"),
                content,
                StandardCharsets.UTF_8
        );
    }

    @Test
    void shouldReturnListOfFindings(@TempDir Path catalinaBase) throws IOException {

        writeServerXml(catalinaBase,
                "<Server port=\"8005\" shutdown=\"SHUTDOWN\">" +
                        "<Service>" +
                        "<Connector port=\"8080\"/>" +
                        "</Service>" +
                        "</Server>");

        List<Finding> findings = engine.validate(catalinaBase);

        assertThat(findings).isNotNull();
    }

    @Test
    void shouldReturnEmptyListOrFindingsWithoutThrowing(@TempDir Path catalinaBase) throws IOException {

        Files.createDirectories(catalinaBase.resolve("conf"));

        List<Finding> findings = engine.validate(catalinaBase);

        assertThat(findings).isNotNull();
    }

    @Test
    void shouldEvaluateRulesAgainstCatalinaBase(@TempDir Path catalinaBase) throws IOException {

        writeServerXml(catalinaBase,
                "<Server port=\"8005\" shutdown=\"SHUTDOWN\">" +
                        "<Service>" +
                        "<Connector port=\"8080\"/>" +
                        "</Service>" +
                        "</Server>");

        List<Finding> findings = engine.validate(catalinaBase);

        assertThat(findings).isInstanceOf(List.class);
    }
}
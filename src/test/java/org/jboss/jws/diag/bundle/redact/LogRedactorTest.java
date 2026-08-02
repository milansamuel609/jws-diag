package org.jboss.jws.diag.bundle.redact;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;
import org.jboss.jws.diag.common.RedactionLevel;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class LogRedactorTest {

    private final LogRedactor redactor = new LogRedactor();

    private final BundleContext context = new BundleContext(
            Path.of("/dummy"),
            Path.of("/dummy"),
            Path.of("/dummy"),
            RedactionLevel.DEFAULT);

    @Test
    void shouldSupportLogFiles() {

        CollectedFile file = CollectedFile.builder()
                .relativeArchivePath("logs/catalina.out")
                .sourcePath(Path.of("logs/catalina.out"))
                .type(CollectedFile.Type.LOG)
                .content("hello")
                .build();

        assertThat(redactor.supports(file)).isTrue();
    }

    @Test
    void shouldLeaveContentUnchangedAndMarkAsRedacted() {

        CollectedFile file = CollectedFile.builder()
                .relativeArchivePath("logs/catalina.out")
                .sourcePath(Path.of("logs/catalina.out"))
                .type(CollectedFile.Type.LOG)
                .content("hello")
                .build();

        CollectedFile result = redactor.redact(file, context);

        assertThat(result.getContent()).isEqualTo("hello");
        assertThat(result.isRedacted()).isTrue();
    }
}
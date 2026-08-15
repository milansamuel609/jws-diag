package org.jboss.jws.diag.bundle.model;

import org.jboss.jws.diag.bundle.redact.XmlAttributeRedactor;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CollectedFileTest {

    private static final String MASK = XmlAttributeRedactor.MASK;

    @Test
    void shouldSetAllFieldsWhenBuilt() {
        Path source = Paths.get("conf/server.xml");

        CollectedFile file = CollectedFile.builder()
                .relativeArchivePath("conf/server.xml")
                .sourcePath(source)
                .type(CollectedFile.Type.XML_CONFIG)
                .content("<Server/>")
                .redacted(true)
                .build();

        assertThat(file.getRelativeArchivePath()).isEqualTo("conf/server.xml");
        assertThat(file.getSourcePath()).isEqualTo(source);
        assertThat(file.getType()).isEqualTo(CollectedFile.Type.XML_CONFIG);
        assertThat(file.getContent()).isEqualTo("<Server/>");
        assertThat(file.isRedacted()).isTrue();
    }

    @Test
    void shouldDefaultRedactedToFalseWhenBuilt() {
        CollectedFile file = minimalFile("<Server/>");

        assertThat(file.isRedacted()).isFalse();
    }

    @Test
    void shouldThrowWhenRequiredFieldIsMissing() {
        assertThatThrownBy(() -> CollectedFile.builder()
                .sourcePath(Paths.get("conf/server.xml"))
                .type(CollectedFile.Type.XML_CONFIG)
                .content("<Server/>")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("relativeArchivePath");
    }

    @Test
    void shouldReturnNewInstanceMarkedRedactedWhenContentIsReplaced() {
        CollectedFile original = minimalFile("<Server password=\"secret\"/>");

        CollectedFile redacted = original.withContent("<Server password=\"" + MASK + "\"/>");

        assertThat(redacted.getContent()).isEqualTo("<Server password=\"" + MASK + "\"/>");
        assertThat(redacted.isRedacted()).isTrue();
    }

    @Test
    void shouldNotMutateOriginalWhenContentIsReplaced() {
        CollectedFile original = minimalFile("<Server password=\"secret\"/>");

        original.withContent("<Server password=\"" + MASK + "\"/>");

        assertThat(original.getContent()).isEqualTo("<Server password=\"secret\"/>");
        assertThat(original.isRedacted()).isFalse();
    }

    @Test
    void shouldPreservePathAndTypeWhenContentIsReplaced() {
        CollectedFile original = minimalFile("<Server/>");

        CollectedFile redacted = original.withContent("<Server/>");

        assertThat(redacted.getRelativeArchivePath()).isEqualTo(original.getRelativeArchivePath());
        assertThat(redacted.getSourcePath()).isEqualTo(original.getSourcePath());
        assertThat(redacted.getType()).isEqualTo(original.getType());
    }

    private CollectedFile minimalFile(String content) {
        return CollectedFile.builder()
                .relativeArchivePath("conf/server.xml")
                .sourcePath(Paths.get("conf/server.xml"))
                .type(CollectedFile.Type.XML_CONFIG)
                .content(content)
                .build();
    }
}
package org.jboss.jws.diag.bundle.model;

import java.nio.file.Path;
import java.util.Objects;

public final class CollectedFile {

    public enum Type {
        XML_CONFIG,
        PROPERTIES,
        LOG
    }

    private final String relativeArchivePath;
    private final Path sourcePath;
    private final Type type;
    private final String content;
    private final boolean redacted;

    private CollectedFile(Builder builder) {
        this.relativeArchivePath = Objects.requireNonNull(builder.relativeArchivePath, "relativeArchivePath");
        this.sourcePath = Objects.requireNonNull(builder.sourcePath, "sourcePath");
        this.type = Objects.requireNonNull(builder.type, "type");
        this.content = Objects.requireNonNull(builder.content, "content");
        this.redacted = builder.redacted;
    }

    public String getRelativeArchivePath() {
        return relativeArchivePath;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public boolean isRedacted() {
        return redacted;
    }

    public CollectedFile withContent(String newContent) {
        return new Builder()
                .relativeArchivePath(this.relativeArchivePath)
                .sourcePath(this.sourcePath)
                .type(this.type)
                .content(newContent)
                .redacted(true)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String relativeArchivePath;
        private Path sourcePath;
        private Type type;
        private String content;
        private boolean redacted = false;

        public Builder relativeArchivePath(String relativeArchivePath) {
            this.relativeArchivePath = relativeArchivePath;
            return this;
        }

        public Builder sourcePath(Path sourcePath) {
            this.sourcePath = sourcePath;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder redacted(boolean redacted) {
            this.redacted = redacted;
            return this;
        }

        public CollectedFile build() {
            return new CollectedFile(this);
        }
    }
}
package org.jboss.jws.diag.bundle;

import org.jboss.jws.diag.common.RedactionLevel;

import java.nio.file.Path;
import java.util.Objects;

public final class BundleContext {

    private final Path catalinaBase;
    private final Path catalinaHome;
    private final Path stagingDir;
    private final RedactionLevel redactionLevel;

    public BundleContext(Path catalinaBase, Path catalinaHome, Path stagingDir, RedactionLevel redactionLevel) {
        this.catalinaBase = Objects.requireNonNull(catalinaBase, "catalinaBase");
        this.catalinaHome =  Objects.requireNonNull(catalinaHome, "catalinaHome");
        this.stagingDir = Objects.requireNonNull(stagingDir, "stagingDir");
        this.redactionLevel = Objects.requireNonNull(redactionLevel, "redactionLevel");
    }

    public Path getCatalinaBase() {
        return catalinaBase;
    }

    public Path getCatalinaHome() {
        return catalinaHome;
    }

    public Path getStagingDir() {
        return stagingDir;
    }

    public RedactionLevel getRedactionLevel() {
        return redactionLevel;
    }
}
package org.jboss.jws.diag.bundle;

import org.jboss.jws.diag.common.RedactionLevel;
import org.jboss.jws.diag.bundle.manifest.ManifestGenerator;
import org.jboss.jws.diag.common.ExitCodes;
import org.jboss.jws.diag.common.OutputFormatMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Command(name = "bundle",
        description = "Generate a redacted .tar.gz support bundle with configs, version manifest, and optional logs",
        mixinStandardHelpOptions = true)
public class BundleCommand implements Runnable {

    @Mixin
    private OutputFormatMixin outputFormat;

    @Option(names = "--catalina-home", description = "Path to CATALINA_HOME (defaults to $CATALINA_HOME env var)")
    private Path catalinaHome;

    @Option(names = "--catalina-base", description = "Path to CATALINA_BASE (defaults to $CATALINA_BASE env var)")
    private Path catalinaBase;

    @Option(names = {"--redaction-level"},
            description = "Redaction level: DEFAULT or STRICT. Default: DEFAULT")
    private RedactionLevel redactionLevel = RedactionLevel.DEFAULT;

    @Option(names = {"--staging-dir"},
            description = "Directory to stage bundle contents before archiving. Defaults to a temp directory.")
    private String stagingDirOption;

    @Override
    public void run() {
        System.exit(execute());
    }

    public int execute() {
        Path resolvedCatalinaBase;
        try {
            resolvedCatalinaBase = resolveCatalinaBase();
        } catch (IllegalStateException e) {
            System.err.println("[ERROR] " + e.getMessage());
            return ExitCodes.ERRORS;
        }

        Path resolvedCatalinaHome = resolveCatalinaHome(resolvedCatalinaBase);

        try {
            Path resolvedStagingDir = resolveStagingDir();
            BundleContext context = new BundleContext(
                    resolvedCatalinaBase, resolvedCatalinaHome, resolvedStagingDir, redactionLevel);

            new BundleEngine().run(context);
            new ManifestGenerator().writeToStagingDir(context);

            System.out.println("Bundle staged at: " + resolvedStagingDir);
            return ExitCodes.OK;
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to generate bundle: " + e.getMessage());
            return ExitCodes.ERRORS;
        }
    }

    private Path resolveCatalinaHome(Path resolvedCatalinaBase) {
        if (catalinaHome != null) {
            return catalinaHome;
        }
        String envValue = System.getenv("CATALINA_HOME");
        if (envValue != null && !envValue.isBlank()) {
            return Paths.get(envValue);
        }
        return resolvedCatalinaBase;
    }

    private Path resolveCatalinaBase() {
        if (catalinaBase != null) {
            return catalinaBase;
        }

        String envValue = System.getenv("CATALINA_BASE");
        if (envValue == null || envValue.isBlank()) {
            throw new IllegalStateException(
                    "Could not determine CATALINA_BASE. Use --catalina-base, "
                            + "or set the CATALINA_BASE environment variable.");
        }

        return Paths.get(envValue);
    }

    Path resolveStagingDir() throws IOException {
        if (stagingDirOption != null) {
            Path dir = Paths.get(stagingDirOption);
            Files.createDirectories(dir);
            return dir;
        }
        return Files.createTempDirectory("jws-diag-bundle-");
    }
}
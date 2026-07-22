package org.jboss.jws.diag.bundle.manifest;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.common.RedactionLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ManifestGeneratorTest {

    private final ManifestGenerator generator = new ManifestGenerator();

    private void writeFakeCatalinaJar(Path catalinaHome, String serverInfoValue) throws IOException {
        Path libDir = catalinaHome.resolve("lib");
        Files.createDirectories(libDir);

        try (OutputStream fileOut = Files.newOutputStream(libDir.resolve("catalina.jar"));
             JarOutputStream jarOut = new JarOutputStream(fileOut)) {
            jarOut.putNextEntry(new JarEntry("org/apache/catalina/util/ServerInfo.properties"));
            jarOut.write(("server.info=" + serverInfoValue + "\n").getBytes());
            jarOut.closeEntry();
        }
    }

    private BundleContext contextFor(Path catalinaBase, Path catalinaHome, Path stagingDir) {
        return new BundleContext(catalinaBase, catalinaHome, stagingDir, RedactionLevel.DEFAULT);
    }

    @Test
    void shouldIncludeAllRequiredManifestFields(
            @TempDir Path catalinaBase, @TempDir Path catalinaHome, @TempDir Path stagingDir) throws IOException {
        writeFakeCatalinaJar(catalinaHome, "Apache Tomcat/10.1.34");

        Map<String, Object> manifest = generator.buildManifest(contextFor(catalinaBase, catalinaHome, stagingDir));

        assertThat(manifest)
                .containsKeys("bundleTimestamp", "tomcatVersion", "javaVersion", "os", "catalinaBase", "cpuCores");
    }

    @Test
    void shouldUseTomcatVersionReaderResult(
            @TempDir Path catalinaBase, @TempDir Path catalinaHome, @TempDir Path stagingDir) throws IOException {
        writeFakeCatalinaJar(catalinaHome, "Apache Tomcat/10.1.34");

        Map<String, Object> manifest = generator.buildManifest(contextFor(catalinaBase, catalinaHome, stagingDir));

        assertThat(manifest.get("tomcatVersion")).isEqualTo("10.1.34");
    }

    @Test
    void shouldReportUnknownTomcatVersionWhenCatalinaJarMissing(
            @TempDir Path catalinaBase, @TempDir Path catalinaHome, @TempDir Path stagingDir) {

        Map<String, Object> manifest = generator.buildManifest(contextFor(catalinaBase, catalinaHome, stagingDir));

        assertThat(manifest.get("tomcatVersion")).isEqualTo("unknown");
    }

    @Test
    void shouldUseRealCatalinaBasePathInManifest(
            @TempDir Path catalinaBase, @TempDir Path catalinaHome, @TempDir Path stagingDir) {
        Map<String, Object> manifest = generator.buildManifest(contextFor(catalinaBase, catalinaHome, stagingDir));

        assertThat(manifest.get("catalinaBase")).isEqualTo(catalinaBase.toString());
    }

    @Test
    void shouldReportPositiveCpuCoreCount(
            @TempDir Path catalinaBase, @TempDir Path catalinaHome, @TempDir Path stagingDir) {
        Map<String, Object> manifest = generator.buildManifest(contextFor(catalinaBase, catalinaHome, stagingDir));

        assertThat((Integer) manifest.get("cpuCores")).isGreaterThan(0);
    }

    @Test
    void shouldProduceValidJsonOutput(
            @TempDir Path catalinaBase, @TempDir Path catalinaHome, @TempDir Path stagingDir) throws IOException {
        Map<String, Object> manifest = generator.buildManifest(contextFor(catalinaBase, catalinaHome, stagingDir));

        String json = generator.toJson(manifest);

        assertThat(json).contains("\"bundleTimestamp\"", "\"cpuCores\"");
    }

    @Test
    void shouldWriteManifestJsonFileToStagingDir(
            @TempDir Path catalinaBase, @TempDir Path catalinaHome, @TempDir Path stagingDir) throws IOException {
        generator.writeToStagingDir(contextFor(catalinaBase, catalinaHome, stagingDir));

        Path manifestFile = stagingDir.resolve("manifest.json");
        assertThat(manifestFile).exists();
        assertThat(Files.readString(manifestFile, StandardCharsets.UTF_8)).contains("bundleTimestamp");
    }
}
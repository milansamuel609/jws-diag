package org.jboss.jws.diag.bundle.manifest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class TomcatVersionReaderTest {

    private final TomcatVersionReader reader = new TomcatVersionReader();

    private void writeFakeCatalinaJar(Path catalinaHome, String serverInfoValue) throws IOException {
        Path libDir = catalinaHome.resolve("lib");
        Files.createDirectories(libDir);

        try (OutputStream fileOut = Files.newOutputStream(libDir.resolve("catalina.jar"));
             JarOutputStream jarOut = new JarOutputStream(fileOut)) {

            jarOut.putNextEntry(new JarEntry("org/apache/catalina/util/ServerInfo.properties"));
            String propertiesContent = "server.info=" + serverInfoValue + "\n";
            jarOut.write(propertiesContent.getBytes());
            jarOut.closeEntry();
        }
    }

    @Test
    void shouldReadVersionFromServerInfoProperties(@TempDir Path catalinaHome) throws IOException {
        writeFakeCatalinaJar(catalinaHome, "Apache Tomcat/10.1.34");

        String version = reader.readVersion(catalinaHome);

        assertThat(version).isEqualTo("10.1.34");
    }

    @Test
    void shouldStripServerNamePrefixFromVersion(@TempDir Path catalinaHome) throws IOException {
        writeFakeCatalinaJar(catalinaHome, "Apache Tomcat/9.0.85");

        String version = reader.readVersion(catalinaHome);

        assertThat(version).doesNotContain("Apache Tomcat").isEqualTo("9.0.85");
    }

    @Test
    void shouldReturnUnknownWhenCatalinaJarDoesNotExist(@TempDir Path catalinaHome) {
        // lib/catalina.jar intentionally never created

        String version = reader.readVersion(catalinaHome);

        assertThat(version).isEqualTo("unknown");
    }

    @Test
    void shouldReturnUnknownWhenServerInfoEntryIsMissingFromJar(@TempDir Path catalinaHome) throws IOException {
        Path libDir = catalinaHome.resolve("lib");
        Files.createDirectories(libDir);

        try (OutputStream fileOut = Files.newOutputStream(libDir.resolve("catalina.jar"));
             JarOutputStream jarOut = new JarOutputStream(fileOut)) {
            jarOut.putNextEntry(new JarEntry("some/other/File.class"));
            jarOut.write("dummy".getBytes());
            jarOut.closeEntry();
        }

        String version = reader.readVersion(catalinaHome);

        assertThat(version).isEqualTo("unknown");
    }

    @Test
    void shouldNotThrowWhenCatalinaHomeDoesNotExistAtAll() {
        Path nonExistentPath = Path.of("this/path/does/not/exist/anywhere");

        String version = reader.readVersion(nonExistentPath);

        assertThat(version).isEqualTo("unknown");
    }
}
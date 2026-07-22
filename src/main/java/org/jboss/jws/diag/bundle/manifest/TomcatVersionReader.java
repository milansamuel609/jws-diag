package org.jboss.jws.diag.bundle.manifest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public final class TomcatVersionReader {

    private static final String CATALINA_JAR_RELATIVE_PATH = "lib/catalina.jar";
    private static final String SERVER_INFO_ENTRY =
            "org/apache/catalina/util/ServerInfo.properties";
    private static final String SERVER_INFO_KEY = "server.info";
    private static final String UNKNOWN_VERSION = "unknown";

    public String readVersion(Path catalinaHome) {
        Path catalinaJarPath = catalinaHome.resolve(CATALINA_JAR_RELATIVE_PATH);

        try (JarFile jarFile = new JarFile(catalinaJarPath.toFile())) {
            ZipEntry entry = jarFile.getEntry(SERVER_INFO_ENTRY);
            if (entry == null) {
                System.err.println("[WARN] ServerInfo.properties not found in " + catalinaJarPath);
                return UNKNOWN_VERSION;
            }

            try (InputStream in = jarFile.getInputStream(entry)) {
                Properties props = new Properties();
                props.load(in);
                String serverInfo = props.getProperty(SERVER_INFO_KEY, UNKNOWN_VERSION);
                return extractVersion(serverInfo);
            }
        } catch (IOException e) {
            System.err.println("[WARN] Could not read catalina.jar at " + catalinaJarPath + ": " + e.getMessage());
            return UNKNOWN_VERSION;
        }
    }

    private String extractVersion(String serverInfo) {
        int slashIndex = serverInfo.indexOf('/');
        if (slashIndex == -1 || slashIndex == serverInfo.length() - 1) {
            return serverInfo;
        }
        return serverInfo.substring(slashIndex + 1);
    }
}
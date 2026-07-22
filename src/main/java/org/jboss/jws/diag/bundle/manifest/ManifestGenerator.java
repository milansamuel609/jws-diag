package org.jboss.jws.diag.bundle.manifest;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jboss.jws.diag.bundle.BundleContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ManifestGenerator {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private static final DefaultPrettyPrinter PRINTER;

    static {
        PRINTER = new DefaultPrettyPrinter();
        PRINTER.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
    }

    private final TomcatVersionReader tomcatVersionReader;
    private final OsInfoReader osInfoReader;

    public ManifestGenerator() {
        this.tomcatVersionReader = new TomcatVersionReader();
        this.osInfoReader = new OsInfoReader();
    }

    public Map<String, Object> buildManifest(BundleContext context) {
        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("bundleTimestamp", Instant.now().toString());
        manifest.put("tomcatVersion", tomcatVersionReader.readVersion(context.getCatalinaHome()));
        manifest.put("javaVersion", resolveJavaVersion());
        manifest.put("os", osInfoReader.readOsInfo());
        manifest.put("catalinaBase", context.getCatalinaBase().toString());
        manifest.put("cpuCores", Runtime.getRuntime().availableProcessors());
        return manifest;
    }

    public String toJson(Map<String, Object> manifest) throws IOException {
        return MAPPER.writer(PRINTER).writeValueAsString(manifest);
    }

    public void writeToStagingDir(BundleContext context) throws IOException {
        Map<String, Object> manifest = buildManifest(context);
        String json = toJson(manifest);

        Path destination = context.getStagingDir().resolve("manifest.json");
        Files.writeString(destination, json, StandardCharsets.UTF_8);
    }

    private String resolveJavaVersion() {
        String vendor = System.getProperty("java.vm.vendor", "");
        String version = System.getProperty("java.version", "unknown");
        String majorVersion = version.contains(".") ? version.substring(0, version.indexOf('.')) : version;
        return vendor.isBlank() ? majorVersion : vendor.split(" ")[0] + " " + majorVersion;
    }
}
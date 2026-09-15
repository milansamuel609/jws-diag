package org.jboss.jws.diag.bundle.output;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jboss.jws.diag.common.SchemaVersions;

import java.nio.file.Path;

/**
 * Formats the result of {@code bundle --format JSON}.
 *
 * <pre>
 * {
 *   "schemaVersion": "1.0",
 *   "archive": "/work/jws-support-bundle-2026-09-15T05-53-36Z.tar.gz",
 *   "skippedFiles": 0
 * }
 * </pre>
 */
public class BundleJsonFormatter {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * @param archive      the written archive. Reported as an absolute path so a script can
     *                     use it regardless of the directory the command ran in.
     * @param skippedFiles files that were present but could not be collected
     */
    public String format(Path archive, int skippedFiles) {
        try {
            ObjectNode root = MAPPER.createObjectNode();
            root.put("schemaVersion", SchemaVersions.BUNDLE);
            root.put("archive", archive.toAbsolutePath().normalize().toString().replace('\\', '/'));
            root.put("skippedFiles", skippedFiles);
            return MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize bundle result to JSON", e);
        }
    }
}

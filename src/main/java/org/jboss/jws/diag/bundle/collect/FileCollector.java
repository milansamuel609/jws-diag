package org.jboss.jws.diag.bundle.collect;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FileCollector {

    private final static String[] CONF_FILES = {
            "conf/server.xml",
            "conf/web.xml",
            "conf/tomcat-users.xml",
    };

    public List<CollectedFile> collectConfFiles(BundleContext context) throws IOException {
        List<CollectedFile> files = new ArrayList<>();

        for (String relativePath : CONF_FILES) {
            Path sourcePath = context.getCatalinaBase().resolve(relativePath);

            if (!Files.exists(sourcePath)) {
                System.err.println("[WARN] File not found, skipping: " + sourcePath);
                continue;
            }

            String content = Files.readString(sourcePath, StandardCharsets.UTF_8);

            files.add(CollectedFile.builder()
                    .relativeArchivePath(relativePath)
                    .sourcePath(sourcePath)
                    .type(CollectedFile.Type.XML_CONFIG)
                    .content(content)
                    .build());
        }

        return files;
    }
}

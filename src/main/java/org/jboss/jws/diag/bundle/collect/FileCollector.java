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

    private static final class ConfFile {
        final String relativePath;
        final CollectedFile.Type type;

        ConfFile(String relativePath, CollectedFile.Type type) {
            this.relativePath = relativePath;
            this.type = type;
        }
    }

    private static final ConfFile[] CONF_FILES = {
            new ConfFile("conf/server.xml", CollectedFile.Type.XML_CONFIG),
            new ConfFile("conf/web.xml", CollectedFile.Type.XML_CONFIG),
            new ConfFile("conf/context.xml", CollectedFile.Type.XML_CONFIG),
            new ConfFile("conf/tomcat-users.xml", CollectedFile.Type.XML_CONFIG),
            new ConfFile("conf/catalina.properties", CollectedFile.Type.PROPERTIES),
    };

    public List<CollectedFile> collectConfFiles(BundleContext context) throws IOException {
        List<CollectedFile> files = new ArrayList<>();

        for (ConfFile confFile : CONF_FILES) {
            Path sourcePath = context.getCatalinaBase().resolve(confFile.relativePath);

            if (!Files.exists(sourcePath)) {
                System.err.println("[WARN] File not found, skipping: " + sourcePath);
                continue;
            }

            String content = Files.readString(sourcePath, StandardCharsets.UTF_8);

            files.add(CollectedFile.builder()
                    .relativeArchivePath(confFile.relativePath)
                    .sourcePath(sourcePath)
                    .type(confFile.type)
                    .content(content)
                    .build());
        }

        return files;
    }
}
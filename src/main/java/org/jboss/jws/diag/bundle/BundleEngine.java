package org.jboss.jws.diag.bundle;

import org.jboss.jws.diag.bundle.collect.FileCollector;
import org.jboss.jws.diag.bundle.model.CollectedFile;
import org.jboss.jws.diag.bundle.output.StagingWriter;
import org.jboss.jws.diag.bundle.redact.Redactor;
import org.jboss.jws.diag.bundle.redact.XmlAttributeRedactor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BundleEngine {

    private final FileCollector fileCollector;
    private final StagingWriter stagingWriter;
    private final List<Redactor> redactors;

    public BundleEngine() {
        this.fileCollector = new FileCollector();
        this.stagingWriter = new StagingWriter();
        this.redactors = new ArrayList<>();
        this.redactors.add(new XmlAttributeRedactor());
    }

    public void run(BundleContext context) throws IOException {
        List<CollectedFile> files = fileCollector.collectConfFiles(context);

        for (CollectedFile file : files) {
            CollectedFile redactedFile = applyRedactors(file, context);
            stagingWriter.write(redactedFile, context);
        }
    }

    private CollectedFile applyRedactors(CollectedFile file, BundleContext context) {
        CollectedFile current = file;

        for (Redactor redactor : redactors) {
            if (redactor.supports(current)) {
                current = redactor.redact(current, context);
            }
        }

        return current;
    }
}
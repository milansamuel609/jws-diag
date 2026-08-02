package org.jboss.jws.diag.bundle.redact;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;

public final class LogRedactor implements Redactor {

    @Override
    public boolean supports(CollectedFile file) {
        return file.getType() == CollectedFile.Type.LOG;
    }

    @Override
    public CollectedFile redact(CollectedFile file, BundleContext context) {
        return file.withContent(file.getContent());
    }
}
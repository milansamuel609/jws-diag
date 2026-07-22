package org.jboss.jws.diag.bundle.redact;

import org.jboss.jws.diag.bundle.BundleContext;
import org.jboss.jws.diag.bundle.model.CollectedFile;

public interface Redactor {

    boolean supports(CollectedFile file);

    CollectedFile redact(CollectedFile file, BundleContext context);
}
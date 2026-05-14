package org.jboss.jws.diag.bundle;

import org.jboss.jws.diag.common.ExitCodes;
import org.jboss.jws.diag.common.OutputFormatMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "bundle",
        description = "Generate a redacted .tar.gz support bundle with configs, version manifest, and optional logs",
        mixinStandardHelpOptions = true)
public class BundleCommand implements Runnable {

    @Mixin
    private OutputFormatMixin outputFormat;

    @Override
    public void run() {
        System.out.println("jws-diag bundle: not yet implemented");
        System.exit(ExitCodes.OK);
    }
}

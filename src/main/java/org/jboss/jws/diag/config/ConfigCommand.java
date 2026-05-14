package org.jboss.jws.diag.config;

import org.jboss.jws.diag.common.ExitCodes;
import org.jboss.jws.diag.common.OutputFormatMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "config",
        description = "Parse and display effective connector, TLS, proxy, and executor configuration",
        mixinStandardHelpOptions = true)
public class ConfigCommand implements Runnable {

    @Mixin
    private OutputFormatMixin outputFormat;

    @Override
    public void run() {
        System.out.println("jws-diag config: not yet implemented");
        System.exit(ExitCodes.OK);
    }
}

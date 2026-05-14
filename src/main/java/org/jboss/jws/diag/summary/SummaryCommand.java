package org.jboss.jws.diag.summary;

import org.jboss.jws.diag.common.ExitCodes;
import org.jboss.jws.diag.common.OutputFormatMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "summary",
        description = "Show installed versions, JVM info, OS/container detection, and native library status",
        mixinStandardHelpOptions = true)
public class SummaryCommand implements Runnable {

    @Mixin
    private OutputFormatMixin outputFormat;

    @Override
    public void run() {
        System.out.println("jws-diag summary: not yet implemented");
        System.exit(ExitCodes.OK);
    }
}

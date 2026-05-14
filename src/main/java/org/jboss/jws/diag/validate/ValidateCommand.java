package org.jboss.jws.diag.validate;

import org.jboss.jws.diag.common.ExitCodes;
import org.jboss.jws.diag.common.OutputFormatMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "validate",
        description = "Run diagnostic rules against configuration and report findings (INFO/WARN/ERROR)",
        mixinStandardHelpOptions = true)
public class ValidateCommand implements Runnable {

    @Mixin
    private OutputFormatMixin outputFormat;

    @Override
    public void run() {
        System.out.println("jws-diag validate: not yet implemented");
        System.exit(ExitCodes.OK);
    }
}

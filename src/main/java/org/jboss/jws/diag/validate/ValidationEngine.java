package org.jboss.jws.diag.validate;

import org.jboss.jws.diag.validate.model.Finding;
import org.jboss.jws.diag.validate.rules.security.RootUserCheckRule;
import org.jboss.jws.diag.validate.rules.security.UserDefaultCredentialsRule;
import org.jboss.jws.diag.validate.rules.security.ShutdownPortConfigRule;
import org.jboss.jws.diag.validate.rules.security.ErrorValveRule;
import org.jboss.jws.diag.validate.rules.security.TraceEnabledRule;
import org.jboss.jws.diag.validate.rules.security.LocalhostBindingRule;
import org.jboss.jws.diag.validate.rules.tls.DeprecatedProtocolsRule;
import org.jboss.jws.diag.validate.rules.tls.CertificateExpiryRule;
import org.jboss.jws.diag.validate.rules.tls.BadKeystorePathRule;
import org.jboss.jws.diag.validate.rules.tls.MissingSecureFlagRule;
import org.jboss.jws.diag.validate.rules.tls.MissingSslHostConfigRule;
import org.jboss.jws.diag.validate.rules.tls.WeakCipherSuitesRule;
import org.jboss.jws.diag.validate.rules.connector.LowThreadsCheckRule;
import org.jboss.jws.diag.validate.rules.connector.PortConflictRule;
import org.jboss.jws.diag.validate.rules.connector.ProxyMismatchRule;
import org.jboss.jws.diag.validate.rules.connector.MissingRedirectPortRule;
import org.jboss.jws.diag.validate.rules.connector.ObsoleteAprConnectorRule;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ValidationEngine {

    private final List<Rule> rules = List.of(
            new RootUserCheckRule(),
            new UserDefaultCredentialsRule(),
            new ShutdownPortConfigRule(),
            new ErrorValveRule(),
            new TraceEnabledRule(),
            new LocalhostBindingRule(),
            new DeprecatedProtocolsRule(),
            new CertificateExpiryRule(),
            new BadKeystorePathRule(),
            new MissingSecureFlagRule(),
            new MissingSslHostConfigRule(),
            new WeakCipherSuitesRule(),
            new LowThreadsCheckRule(),
            new PortConflictRule(),
            new ProxyMismatchRule(),
            new MissingRedirectPortRule(),
            new ObsoleteAprConnectorRule()
    );

    public List<Finding> validate(Path catalinaBase) {
        RuleContext context = RuleContext.fromDisk(catalinaBase);

        List<Finding> findings = new ArrayList<>();

        for (Rule rule : rules) {
            findings.addAll(rule.evaluate(context));
        }

        return findings;
    }
}
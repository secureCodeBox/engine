package io.securecodebox.scanprocesses.amassnmap;

import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.findings.Severity;
import io.securecodebox.scanprocesses.amassnmap.util.HttpHeaderStrategy;
import io.securecodebox.scanprocesses.amassnmap.util.HttpHeaders;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;


@Component
public class FilterHttpSecurityHeaders implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(FilterHttpSecurityHeaders.class);

    @Autowired
    ScanProcessExecutionFactory processExecutionFactory;


    private static final HttpHeaderStrategy[] httpStrategies = new HttpHeaderStrategy[] {
            requireHttpServersToRedirectToHttps()
    };

    private static final HttpHeaderStrategy[] httpsStrategies = new HttpHeaderStrategy[] {
            requireStrictTransportSecurityHeader(),
            requireContentSecurityPolicyHeader(),
            requirePermittedCrossDomainPolicies(),
            requireFrameOptionsToBeDenied(),
            requireXssProtectionToBeEnabled(),
            requireContentTypeOptionsToEqualNosniff()
    };


    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        final ScanProcessExecution process = processExecutionFactory.get(delegateExecution);
        final ArrayList<Finding> findings = new ArrayList<>();
        final long T_START = System.currentTimeMillis();
        process.getFindings().stream()
                .filter(finding -> HttpHeaders.headersPresentInFinding(finding))
                .forEach(finding -> {
                    final HttpHeaders headers = HttpHeaders.fromFinding(finding);
                    findings.addAll(applyStrategies (headers, finding));
                });
        final long T_STRATEGIES_APPLIED = System.currentTimeMillis();
        final int numberOfAdditionalFindings = findings.size() - process.getFindings().size();
        clearFindings(process);
        findings.forEach(changedFinding -> process.appendFinding(changedFinding));
        LOG.debug("http-headers strategies yielded {} additional findings; finding them took {}ms, storing them {}ms", numberOfAdditionalFindings, T_STRATEGIES_APPLIED - T_START, System.currentTimeMillis() - T_STRATEGIES_APPLIED);
    }

    private ArrayList<Finding> applyStrategies(HttpHeaders headers, Finding finding) {
        final ArrayList<Finding> changedFindings = new ArrayList<>();
        changedFindings.add(finding);

        final String name = finding.getName();

        if (name.equals("http")) {
            for (HttpHeaderStrategy httpStrategy : httpStrategies) {
                changedFindings.addAll(httpStrategy.apply(headers, finding));
            }
        } else if (name.equals("https")) {
            for (HttpHeaderStrategy httpsStrategy : httpsStrategies) {
                changedFindings.addAll(httpsStrategy.apply(headers, finding));
            }
        }

        return changedFindings;
    }


    private static HttpHeaderStrategy requireHttpServersToRedirectToHttps() {
        return new HttpHeaderStrategy("Location")
                .ifPresent()
                .modifyFinding((value, finding) -> {
                    finding.setSeverity(Severity.INFORMATIONAL);
                    finding.setName(finding.getName() + " with redirect to " + value);
                    finding.setDescription(finding.getDescription() + " with redirect to " + value);
                })
                .ifMissing()
                .createFinding(Severity.HIGH, "missing redirect to https");
    }

    private static HttpHeaderStrategy requireStrictTransportSecurityHeader() {
        return new HttpHeaderStrategy("Strict-Transport-Security")
                .ifMissingCreateFinding(Severity.MEDIUM);
    }

    private static HttpHeaderStrategy requireContentSecurityPolicyHeader() {
        return new HttpHeaderStrategy("Content-Security-Policy")
                .ifMissingCreateFinding(Severity.MEDIUM);
    }

    private static HttpHeaderStrategy requirePermittedCrossDomainPolicies() {
        return new HttpHeaderStrategy("X-Permitted-Cross-Domain-Policies")
                .ifMissingCreateFinding(Severity.MEDIUM);
    }

    private static HttpHeaderStrategy requireFrameOptionsToBeDenied() {
        return new HttpHeaderStrategy("X-Frame-Options")
                .ifMissing()
                .createFinding(Severity.MEDIUM, "X-Frame-Options header missing")
                .ifTrue(value -> !value.toLowerCase().contains("deny"))
                .createFinding(Severity.MEDIUM, "X-Frame-Options header misconfigured", value -> "X-Frame-Options should be 'deny', instead it is set to '" + value + "'");
    }

    private static HttpHeaderStrategy requireXssProtectionToBeEnabled() {
        return new HttpHeaderStrategy("X-XSS-Protection")
                .ifMissing()
                .createFinding(Severity.MEDIUM, "X-XSS-Protection header missing")
                .ifTrue(value -> value.startsWith("0"))
                .createFinding(Severity.MEDIUM, "X-XSS-Protection manually disabled");
    }

    private static HttpHeaderStrategy requireContentTypeOptionsToEqualNosniff() {
        return new HttpHeaderStrategy("X-Content-Type-Options")
                .ifMissing()
                .createFinding(Severity.MEDIUM, "X-Content-Type-Options header missing")
                .ifTrue(value -> !value.equalsIgnoreCase("nosniff"))
                .createFinding(Severity.MEDIUM, "X-Content-Type-Options misconfigured", value -> "X-Conntent-Type-Options should be set to 'nosniff' instead of '" + value + "'");
    }


    private void clearFindings(ScanProcessExecution process) {
        if (!process.getFindings().isEmpty()) {
            LOG.debug("Clearing findings. The process had {}", process.getFindings().size());
            process.clearFindings();
        }
    }


    private static final HttpHeaderStrategy requireHeader (final String header, final Severity severity) {
        return new HttpHeaderStrategy(header)
                .ifMissing()
                .createFinding(severity, header + " header missing");
    }

}

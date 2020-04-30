package io.securecodebox.scanprocess.nmap.delegate;

import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.findings.Severity;
import io.securecodebox.scanprocess.nmap.constants.NmapFindingAttributes;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;


@Component
public class FilterPortFindings implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(FilterPortFindings.class);

    @Value("${securecodebox.process.nmap.filter.portFindingsSeverity:false}")
    private boolean isFilterEnabled;


    @Autowired
    ScanProcessExecutionFactory processExecutionFactory;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        if (isFilterEnabled) {
            LOG.info("nmap-port filter is enabled!");

            final ScanProcessExecution process = processExecutionFactory.get(delegateExecution);
            final ArrayList<Finding> findings = new ArrayList<>();

            final long tStart = System.currentTimeMillis();
            process.getFindings().stream()
                    .forEach(finding -> {
                        if (isPortFound(finding)) {
                            LOG.info("Found port to filter: {} ", finding.getDescription());
                            applyNewSeverity(finding);
                        }
                    });
            final long tStrategiesApplied = System.currentTimeMillis();

            process.appendFindings(findings);
            LOG.debug("nmap-port filter yielded {} took {}ms, storing them {}ms", tStrategiesApplied - tStart, System.currentTimeMillis() - tStrategiesApplied);

        } else {
            LOG.info("nmap-port filter is disabled!");
        }
    }

    /*
     * Checks if the current finding is an nmap finding which contains a open port.
     */
    private boolean isPortFound(Finding finding) {
        boolean result = false;
        if(finding.getAttribute(NmapFindingAttributes.PORT) != null && !finding.getAttribute(NmapFindingAttributes.PORT).toString().isEmpty()) {
            result = true;
        }
        else
        {
            LOG.info("No port found at finding {}", finding.toString());
            LOG.info("No port found at: {}", finding.getAttribute(NmapFindingAttributes.PORT));
        }
        return result;
    }

    private void applyNewSeverity(Finding finding) {
        String openPort = finding.getAttribute(NmapFindingAttributes.PORT).toString();
        // SSH
        if(openPort == "22") {
            LOG.info("Changing port finding severity from {} to {}", finding.getSeverity(), Severity.MEDIUM);
            finding.setSeverity(Severity.HIGH);

        }
        // HTTP
        if(openPort == "80") {
            LOG.info("Changing port finding severity from {} to {}", finding.getSeverity(), Severity.MEDIUM);
            finding.setSeverity(Severity.MEDIUM);
        }
        // HTTP
        if(openPort == "8080") {
            LOG.info("Changing port finding severity from {} to {}", finding.getSeverity(), Severity.MEDIUM);
            finding.setSeverity(Severity.MEDIUM);
        }
        // LDAP
        if(openPort == "389") {
            LOG.info("Changing port finding severity from {} to {}", finding.getSeverity(), Severity.MEDIUM);
            finding.setSeverity(Severity.HIGH);
        }
        // RPC
        if(openPort == "135") {
            LOG.info("Changing port finding severity from {} to {}", finding.getSeverity(), Severity.MEDIUM);
            finding.setSeverity(Severity.LOW);
        }
        // Remote Desktop Protocol (RDP)
        if(openPort == "3389") {
            LOG.info("Changing port finding severity from {} to {}", finding.getSeverity(), Severity.MEDIUM);
            finding.setSeverity(Severity.HIGH);
        }

    }
}

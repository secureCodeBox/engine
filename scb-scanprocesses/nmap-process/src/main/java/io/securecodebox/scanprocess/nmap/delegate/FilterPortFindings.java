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
import java.util.HashMap;
import java.util.Map;


@Component
public class FilterPortFindings implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(FilterPortFindings.class);

    @Value("${securecodebox.process.nmap.filter.portFindingsSeverity:false}")
    private boolean isFilterEnabled;

    // TODO: use a properties config map instead: ${securecodebox.nmap.severity}
    private Map<String, Severity> severityMapping = new HashMap<String, Severity>() {{
        put("21", Severity.HIGH);
        put("23", Severity.HIGH);
        put("25", Severity.HIGH);
        put("389", Severity.HIGH);
        put("3389", Severity.HIGH);
        put("135", Severity.MEDIUM);
        put("22", Severity.MEDIUM);
        put("80", Severity.LOW);
        put("8080", Severity.MEDIUM);
        put("8009", Severity.LOW);
        put("9200", Severity.LOW);
        put("5601", Severity.LOW);
        put("5432", Severity.LOW);
        put("3306", Severity.LOW);
        put("1521", Severity.LOW);
    }};

    @Autowired
    ScanProcessExecutionFactory processExecutionFactory;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        if (isFilterEnabled) {
            LOG.info("nmap-port filter is enabled!");

            final ScanProcessExecution process = processExecutionFactory.get(delegateExecution);
            final ArrayList<Finding> newFindings = new ArrayList<>();

            final long tStart = System.currentTimeMillis();

            for(Finding finding : process.getFindings()) {
                if (isPortFound(finding)) {
                    newFindings.add(applyNewSeverity(finding));
                }
                else {
                    newFindings.add(finding);
                }
            }
            final long tStrategiesApplied = System.currentTimeMillis();

            // Resetting the findings with the new one:
            process.clearFindings();
            process.appendFindings(newFindings);

            LOG.debug("nmap-port filter yielded {} took {}ms, storing them {}ms", tStrategiesApplied - tStart, System.currentTimeMillis() - tStrategiesApplied);

        } else {
            LOG.info("nmap-port filter is disabled!");
        }
    }

    /**
     * Checks if the current finding is an nmap finding which contains a open port.
     * @param finding The finding to check
     * @return True if the findings contains a port, false otherwise.
     */
    private boolean isPortFound(Finding finding) {
        boolean result = false;
        if(finding.getAttribute(NmapFindingAttributes.PORT) != null && !finding.getAttribute(NmapFindingAttributes.PORT).toString().isEmpty()) {
            result = true;
        }
        return result;
    }

    /**
     * Applies a new severity based on the configured severity mapping.
     * @param finding The finding to apply a new severity to.
     * @return The finding with a new severity.
     */
    private Finding applyNewSeverity(Finding finding) {

        Finding result = finding;

        String openPort = finding.getAttribute(NmapFindingAttributes.PORT).toString();

        if(severityMapping.containsKey(openPort)) {
            LOG.info("Port {} found in severityMapping with new severity {}", openPort, severityMapping.get(openPort));
            LOG.info("Changing port finding severity from {} to {}", finding.getSeverity(), severityMapping.get(openPort));
            result.setSeverity(severityMapping.get(openPort));
        } else {
            LOG.info("Port {} not found in severityMapping.", openPort);
        }
//
//        // SSH
//        if(openPort.equals("22")) {
//            LOG.debug("Changing port finding severity from {} to {}", finding.getSeverity(), Severity.HIGH);
//            result.setSeverity(Severity.HIGH);
//            LOG.debug("Applying a new severity {} to finding with port {}", Severity.HIGH, openPort);
//        }
//        // ELASTICSEARCH
//        if(openPort.equals("9200")) {
//            LOG.debug("Changing port finding severity from {} to {}", finding.getSeverity(), Severity.MEDIUM);
//            result.setSeverity(Severity.LOW);
//            LOG.debug("Applying a new severity {} to finding with port {}", Severity.MEDIUM, openPort);
//        }
//        // KIBANA
//        if(openPort.equals("5601")) {
//            LOG.debug("Changing port finding severity from {} to {}", finding.getSeverity(), Severity.MEDIUM);
//            result.setSeverity(Severity.LOW);
//            LOG.debug("Applying a new severity {} to finding with port {}", Severity.MEDIUM, openPort);
//        }
//        // HTTP
//        if(openPort.equals("80")) {
//            LOG.debug("Changing port finding severity from {} to {}", finding.getSeverity(), Severity.MEDIUM);
//            result.setSeverity(Severity.LOW);
//            LOG.debug("Applying a new severity {} to finding with port {}", Severity.MEDIUM, openPort);
//        }
//        // HTTP
//        if(openPort.equals("8080")) {
//            LOG.debug("Changing port finding severity from {} to {}", finding.getSeverity(), Severity.MEDIUM);
//            result.setSeverity(Severity.MEDIUM);
//            LOG.debug("Applying a new severity {} to finding with port {}", Severity.MEDIUM, openPort);
//        }
//        // LDAP
//        if(openPort.equals("389")) {
//            LOG.debug("Changing port finding severity from {} to {}", finding.getSeverity(), Severity.HIGH);
//            result.setSeverity(Severity.HIGH);
//            LOG.debug("Applying a new severity {} to finding with port {}", Severity.HIGH, openPort);
//        }
//        // RPC
//        if(openPort.equals("135")) {
//            LOG.debug("Changing port finding severity from {} to {}", finding.getSeverity(), Severity.LOW);
//            result.setSeverity(Severity.MEDIUM);
//            LOG.debug("Applying a new severity {} to finding with port {}", Severity.LOW, openPort);
//        }
//        // Remote Desktop Protocol (RDP)
//        if(openPort.equals("3389")) {
//            LOG.debug("Changing port finding severity from {} to {}", finding.getSeverity(), Severity.HIGH);
//            result.setSeverity(Severity.HIGH);
//            LOG.debug("Applying a new severity {} to finding with port {}", Severity.HIGH, openPort);
//        }

        return result;
    }
}

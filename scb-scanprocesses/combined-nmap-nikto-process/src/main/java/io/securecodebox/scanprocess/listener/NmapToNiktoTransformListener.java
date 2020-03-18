package io.securecodebox.scanprocess.listener;

import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.scanprocess.ProcessVariableHelper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class NmapToNiktoTransformListener extends TransformFindingsToTargetsListener {

    private final String PORT_DELIMITER = ",";
    private final String DEFAULT_COMBINED_NMAP_NIKTO_PORTS = "80, 443, 8080, 8443";
    protected static final String ATTRIBUTE_BLACKBOX = "BLACKBOX";
    protected static  final String ATTRIBUTE_COMBINED_NMAP_NIKTO_PORTS = "COMBINED_NMAP_NIKTO_PORTS";

    public void notify(DelegateExecution delegateExecution) {
        List<Finding> findings = ProcessVariableHelper.readListFromValue((String) delegateExecution.getVariable(DefaultFields.PROCESS_FINDINGS.name()), Finding.class);
        List<Target> oldTargets = ProcessVariableHelper.readListFromValue((String) delegateExecution.getVariable(DefaultFields.PROCESS_TARGETS.name()), Target.class);
        Set<Target> newTargets = this.nmapToNiktoTransformAction(findings, oldTargets);
        this.startNitktoScan(newTargets, delegateExecution);
    }

    /**
     * Removes all closed ports from specified COMBINED_NMAP_NIKTO_PORTS
     *
     * @param portsToScanByNikto specified COMBINED_NMAP_NIKTO_PORTS
     * @param openPorts          Open ports found by nmap
     * @return portSet
     */
    private Set<String> filterIrrelevantPorts(Set<String> portsToScanByNikto, Set<Finding> openPorts) {
        Set<String> portSet = new HashSet<>();
        openPorts.forEach(finding -> {
            String port = (String) finding.getAttribute(OpenPortAttributes.port);
            if (portsToScanByNikto.contains(port))
                portSet.add(port);
        });
        return portSet;
    }

    /**
     * Extracts the value for the COMBINED_NMAP_NIKTO_PORTS attribute and removes invalid input
     *
     * @param target Target
     * @return Set with validated Ports
     */
    private Set<String> getRelevantPorts(Target target) {
        // Create a Set to ensure every port is only scanned once per host
        Set<String> portsToScanByNikto = new HashSet<>();

        // Transform Comma separated ports into an Array
        String[] combinedNmapNiktoPortsAsArray = this.getCombinedNmapNiktoPorts(target).split(this.PORT_DELIMITER);

        // Remove whitespaces before and after port and add to Collection
        for (String port : combinedNmapNiktoPortsAsArray) {
            portsToScanByNikto.add(port.trim());
        }

        // Move portArray into a Set to ensure every Port is only scanned once for each  host
        portsToScanByNikto = portsToScanByNikto.stream()
                // remove empty entries
                .filter(port -> !port.isEmpty())
                // remove entries that have to long ports or letters or start with zero
                .filter(port -> Pattern.matches("[1-9]+[0-9]{0,4}", port))
                .collect(Collectors.toSet());

        // Use default ports if not specified otherwise
        if (portsToScanByNikto.isEmpty())
            return this.getDefaultPorts();

        return portsToScanByNikto;
    }

    private boolean hasEmptyNiktoPortList(Target target) {
        String niktoPortList = (String) target.getAttributes().get("NIKTO_PORTS");
        return niktoPortList.isEmpty();
    }

    private Map<String, Set<Finding>> getOpenPortsPerTarget(List<Finding> findings) {
        Map<String, Set<Finding>> openPortsPerTarget = new HashMap<>();
        findings.stream()
                .filter(finding -> finding.getCategory().equals("Open Port"))
                .forEach(finding -> {
                    String hostname = (String) finding.getAttribute(OpenPortAttributes.hostname);

                    if (openPortsPerTarget.containsKey(hostname)) {
                        openPortsPerTarget.get(hostname).add(finding);
                    } else {
                        Set<Finding> portSet = new HashSet<>();
                        portSet.add(finding);
                        openPortsPerTarget.put(hostname, portSet);
                    }

                });
        return openPortsPerTarget;
    }

    private Set<Target> collectTargetsWithOpenPorts(List<Target> targets, Map<String, Set<Finding>> openPortsPerTarget) {
        return targets.stream()
                // remove targets with no open ports
                .filter(target -> openPortsPerTarget.containsKey(target.getLocation()))
                .collect(Collectors.toSet());
    }

    private void updateTargetsWithNiktoPorts(Set<Target> targets, Map<String, Set<Finding>> openPortsPerTarget) {
        targets.forEach(target -> {
            StringJoiner niktoPorts = new StringJoiner(this.PORT_DELIMITER);
            if (this.isBlackBoxScan(target)) {
                openPortsPerTarget.get(target.getLocation()).forEach(finding -> {
                    if (finding.getAttribute(OpenPortAttributes.service).equals("http"))
                        niktoPorts.add((String) finding.getAttribute(OpenPortAttributes.port));
                });
            } else {
                Set<String> portsToScanByNikto = this.getRelevantPorts(target);
                Set<String> filteredPorts = this.filterIrrelevantPorts(portsToScanByNikto, openPortsPerTarget.get(target.getLocation()));
                filteredPorts.forEach(niktoPorts::add);
            }
            target.appendOrUpdateAttribute("NIKTO_PORTS", niktoPorts.toString());
        });
    }

    protected Set<Target> nmapToNiktoTransformAction(List<Finding> findings, List<Target> oldTargets) {
        Map<String, Set<Finding>> openPortsPerTarget = this.getOpenPortsPerTarget(findings);

        Set<Target> targets = this.collectTargetsWithOpenPorts(oldTargets, openPortsPerTarget);

        this.updateTargetsWithNiktoPorts(targets, openPortsPerTarget);

        // remove targets with no ports to scan by nikto
        targets = targets.stream().filter(target -> !this.hasEmptyNiktoPortList(target)).collect(Collectors.toSet());

        LOG.info("Created Targets out of Findings: " + targets);

        return targets;

    }

    private void startNitktoScan(Set<Target> targets, DelegateExecution delegateExecution) {
        delegateExecution.setVariable(DefaultFields.PROCESS_TARGETS.name(), ProcessVariableHelper.generateObjectValue(targets));
    }

    private String getCombinedNmapNiktoPorts(Target target) {
        String combinedNmapNiktoPortsAsString = (String) target.getAttributes().get(ATTRIBUTE_COMBINED_NMAP_NIKTO_PORTS);

        // Check if COMBINED_NMAP_NIKTO_PORTS are set at all
        if (combinedNmapNiktoPortsAsString == null)
            return this.DEFAULT_COMBINED_NMAP_NIKTO_PORTS;

        //Use default ports if no ports are specified
        if (combinedNmapNiktoPortsAsString.isEmpty())
            return this.DEFAULT_COMBINED_NMAP_NIKTO_PORTS;

        return combinedNmapNiktoPortsAsString;
    }

    private Set<String> getDefaultPorts() {
        Set<String> portsToScanByNikto = new HashSet<>();
        String[] combinedNmapNiktoPortsAsArray = this.DEFAULT_COMBINED_NMAP_NIKTO_PORTS.split(this.PORT_DELIMITER);

        // Remove whitespaces before and after port and add to Collection
        for (String port : combinedNmapNiktoPortsAsArray) {
            portsToScanByNikto.add(port.trim());
        }
        return portsToScanByNikto;
    }

    private boolean isBlackBoxScan(Target target) {
        if (!target.getAttributes().containsKey(ATTRIBUTE_BLACKBOX))
            return false;

        return target.getAttributes().get(ATTRIBUTE_BLACKBOX).toString().equalsIgnoreCase("true");
    }
}

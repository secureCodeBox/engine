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

    private final Set<String> DEFAULT_PORTS_TO_SCAN_BY_NIKTO = new HashSet<>();
    protected static final String ATTRIBUTE_BLACKBOX = "BLACKBOX";
    protected static final String ATTRIBUTE_COMBINED_NMAP_NIKTO_PORTS = "COMBINED_NMAP_NIKTO_PORTS";
    protected static final String ATTRIBUTE_NIKTO_PARAMETER = "NIKTO_PARAMETER";
    protected static final String ATTRIBUTE_NIKTO_PORTS = "NIKTO_PORTS";

    public NmapToNiktoTransformListener() {
        this.initDefaultPortsToScanByNikto();
    }

    private void initDefaultPortsToScanByNikto() {
        this.DEFAULT_PORTS_TO_SCAN_BY_NIKTO.add("80");
        this.DEFAULT_PORTS_TO_SCAN_BY_NIKTO.add("443");
        this.DEFAULT_PORTS_TO_SCAN_BY_NIKTO.add("8080");
        this.DEFAULT_PORTS_TO_SCAN_BY_NIKTO.add("8443");
    }

    public void notify(DelegateExecution delegateExecution) {
        List<Finding> findings = ProcessVariableHelper.readListFromValue((String) delegateExecution.getVariable(DefaultFields.PROCESS_FINDINGS.name()), Finding.class);
        List<Target> oldTargets = ProcessVariableHelper.readListFromValue((String) delegateExecution.getVariable(DefaultFields.PROCESS_TARGETS.name()), Target.class);
        Set<Target> newTargets = this.nmapToNiktoTransformAction(findings, oldTargets);
        this.startNitktoScan(newTargets, delegateExecution);
    }

    protected Set<Target> nmapToNiktoTransformAction(List<Finding> findings, List<Target> oldTargets) {
        Set<Target> newTargets = new HashSet<>();
        oldTargets.forEach(oldTarget -> {
            String scantype = this.getScanType(oldTarget);

            switch (scantype) {
                case "BLACKBOX":
                    this.transformToBlackBoxScan(oldTarget, findings, newTargets);
                    break;
                case "COMBINED_NMAP_NIKTO_PORTLIST":
                default:
                    this.transformWithPortlist(oldTarget, findings, newTargets);
                    break;
            }
        });

        LOG.info("Created Targets out of Findings: " + newTargets);
        return newTargets;

    }

    private void transformWithPortlist(Target oldTarget, List<Finding> findings, Set<Target> newTargets) {
        Set<String> portsToScanByNikto = this.getPortsToScanByNikto(oldTarget);
        String niktoParameter = String.valueOf(oldTarget.getAttributes().get(ATTRIBUTE_NIKTO_PARAMETER));
        Map<String, Set<String>> relevantPorts = new HashMap<>();
        findings.forEach(finding -> {
            String location = String.valueOf(finding.getAttribute(OpenPortAttributes.hostname));
            String port = String.valueOf(finding.getAttribute(OpenPortAttributes.port));
            if (portsToScanByNikto.contains(port)) {
                if (relevantPorts.containsKey(location)) {
                    relevantPorts.get(location).add(port);
                } else {
                    Set<String> portSet = new HashSet<>();
                    portSet.add(port);
                    relevantPorts.put(location, portSet);
                }
            }
        });
        relevantPorts.forEach((location, portSet) -> {
            StringJoiner joiner = new StringJoiner(" ");
            portSet.forEach(joiner::add);
            Target target = this.createTarget(location, joiner.toString(), niktoParameter);
            newTargets.add(target);
        });
    }

    private Set<String> getPortsToScanByNikto(Target oldTarget) {
        String combinedNmapNiktoPorts = this.getCombinedNmapNiktoPorts(oldTarget);
        if ("".equals(combinedNmapNiktoPorts))
            return DEFAULT_PORTS_TO_SCAN_BY_NIKTO;

        Set<String> portsToScanByNikto = new HashSet<>();
        String PORT_DELIMITER = ",";
        String[] combinedNmapNiktoPortsAsArray = combinedNmapNiktoPorts.split(PORT_DELIMITER);

        // Remove whitespaces before and after port and add to Collection
        for (String port : combinedNmapNiktoPortsAsArray) {
            portsToScanByNikto.add(port.trim());
        }

        // Move portArray into a Set to ensure every Port is only scanned once for each  host
        return portsToScanByNikto.stream()
                // Regex from: https://www.regextester.com/104146 to validate Ports
                .filter(port -> Pattern.matches("^()([1-9]|[1-5]?[0-9]{2,4}|6[1-4][0-9]{3}|65[1-4][0-9]{2}|655[1-2][0-9]|6553[1-5])$", port))
                .collect(Collectors.toSet());
    }

    private void transformToBlackBoxScan(Target oldTarget, List<Finding> findings, Set<Target> newTargets) {
        findings.stream().filter(this::isHttpOrHttpsService).forEach(finding -> {
            String niktoParameter = String.valueOf(oldTarget.getAttributes().get(ATTRIBUTE_NIKTO_PARAMETER));
            String niktoPort = String.valueOf(finding.getAttribute(OpenPortAttributes.port));
            Target newTarget = this.createTarget(finding.getLocation(), niktoPort, niktoParameter);
            newTargets.add(newTarget);
        });
    }

    private Target createTarget(String location, String niktoPort, String niktoParameter) {
        Target target = new Target();
        target.setName("Nikto Scan for: " + location);
        target.setLocation(location);
        target.appendOrUpdateAttribute(ATTRIBUTE_NIKTO_PORTS, niktoPort);
        target.appendOrUpdateAttribute(ATTRIBUTE_NIKTO_PARAMETER, niktoParameter);
        return target;
    }

    private void startNitktoScan(Set<Target> targets, DelegateExecution delegateExecution) {
        delegateExecution.setVariable(DefaultFields.PROCESS_TARGETS.name(), ProcessVariableHelper.generateObjectValue(targets));
    }

    private String getCombinedNmapNiktoPorts(Target target) {
        return String.valueOf(target.getAttributes().get(ATTRIBUTE_COMBINED_NMAP_NIKTO_PORTS));
    }

    private boolean isHttpOrHttpsService(Finding finding) {
        String service = String.valueOf(finding.getAttribute(OpenPortAttributes.service));
        switch (service) {
            case "http":
            case "https":
                return true;
            default:
                return false;
        }
    }

    private String getScanType(Target target) {
        if (target.getAttributes().containsKey(ATTRIBUTE_BLACKBOX))
            return ATTRIBUTE_BLACKBOX;
        if (!String.valueOf(target.getAttributes().get(ATTRIBUTE_COMBINED_NMAP_NIKTO_PORTS)).equals(""))
            return ATTRIBUTE_COMBINED_NMAP_NIKTO_PORTS;
        return "default";
    }
}

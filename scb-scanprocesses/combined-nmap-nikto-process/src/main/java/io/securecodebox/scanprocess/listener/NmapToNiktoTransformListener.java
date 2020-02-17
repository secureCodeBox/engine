package io.securecodebox.scanprocess.listener;

import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.scanprocess.ProcessVariableHelper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;
import sun.rmi.runtime.Log;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class NmapToNiktoTransformListener extends TransformFindingsToTargetsListener {

    public void notify(DelegateExecution delegateExecution) throws Exception {
        List<Finding> findings = ProcessVariableHelper.readListFromValue(
                (String) delegateExecution.getVariable(DefaultFields.PROCESS_FINDINGS.name()),
                Finding.class
        );

        Map<String, Set<String>> openPortsForTarget = new HashMap<>();

        findings.stream()
                .filter(finding -> finding.getCategory().equals("Open Port"))
                .forEach(finding -> {
                    String hostname = (String) finding.getAttribute(OpenPortAttributes.hostname);
                    String port = finding.getAttribute(OpenPortAttributes.port).toString();

                    if (openPortsForTarget.containsKey(hostname)) {
                        openPortsForTarget.get(hostname).add(port);
                    } else {
                        Set<String> portSet = new HashSet<>();
                        portSet.add(port);
                        openPortsForTarget.put(hostname, portSet);
                    }

                });

        List<Target> targets = ProcessVariableHelper.readListFromValue((String) delegateExecution.getVariable(DefaultFields.PROCESS_TARGETS.name()),
                Target.class
        ).stream()
                .filter(target -> openPortsForTarget.containsKey(target.getLocation()))
                .collect(Collectors.toList());

        targets.forEach(target -> {
            Set<String> portsToScanByNikto = this.getSanitizedPortSet(target);
            Set<String> filteredPorts = this.filterIrrelevantPorts(portsToScanByNikto, openPortsForTarget.get(target.getLocation()));
            target.appendOrUpdateAttribute("NIKTO_PORTS", String.join(",", filteredPorts));
        });

        targets.stream().filter(target -> !this.hasEmptyNiktoPortList(target));


        LOG.info("Created Targets out of Findings: " + targets);

        delegateExecution.setVariable(DefaultFields.PROCESS_TARGETS.name(),
                ProcessVariableHelper.generateObjectValue(targets)
        );
    }

    private Set<String> filterIrrelevantPorts(Set<String> portsToScanByNikto, Set<String> openPorts) {
        if (openPorts.equals(portsToScanByNikto))
            return portsToScanByNikto;

        Set<String> portSet = portsToScanByNikto.stream().filter(openPorts::contains).collect(Collectors.toSet());
        return portSet;
    }

    private Set<String> getSanitizedPortSet(Target target) {
        // Create a Set to ensure every port is only scanned once per host
        Set<String> portsToScanByNikto = new HashSet<>();

        // Transform Comma separated ports into an Array
        String[] portArray = ((String) target.getAttributes().get("COMBINED_NMAP_NIKTO_PORTS")).split(",");

        // Remove whitespaces before and after port
        for (String port : portArray) {
            port = port.trim();
        }

        // Move portArray into a Set to ensure every Port is only scanned once for each  host
        Collections.addAll(portsToScanByNikto, portArray);
        return portsToScanByNikto;
    }

    private boolean hasEmptyNiktoPortList(Target target) {
        String niktoPortList = (String) target.getAttributes().get("NIKTO_PORTS");
        return niktoPortList.isEmpty();
    }

}

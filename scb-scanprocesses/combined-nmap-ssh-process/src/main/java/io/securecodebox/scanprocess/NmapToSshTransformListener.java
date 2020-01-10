package io.securecodebox.scanprocess;

import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.scanprocess.listener.TransformFindingsToTargetsListener;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NmapToSshTransformListener extends TransformFindingsToTargetsListener {

    public void notify(DelegateExecution delegateExecution) throws Exception {

        List<Finding> findings = ProcessVariableHelper.readListFromValue(
                (String) delegateExecution.getVariable(DefaultFields.PROCESS_FINDINGS.name()),
                Finding.class
        );

        List<Target> newTargets = findings.stream()
                .filter(finding -> finding.getCategory().equals("Open Port"))
                .filter(finding -> {
                    String service = (String) finding.getAttribute(OpenPortAttributes.service);
                    return "ssh".equals(service);
                })
                .map(finding -> {
                    String hostname = (String) finding.getAttribute(OpenPortAttributes.hostname);
                    String port = finding.getAttribute(OpenPortAttributes.port).toString();

                    Target target = new Target();
                    target.setName("SSH Scan for " + hostname);
                    target.setLocation(hostname + ":" + port);

                    return target;
                }).collect(Collectors.toList());

        LOG.info("Created Targets out of Findings: " + newTargets);

        if (!newTargets.isEmpty() && newTargets.size() > 0) {
            // define the new SSH targets, based on the nmap port scan results
            delegateExecution.setVariable(DefaultFields.PROCESS_TARGETS.name(),
                    ProcessVariableHelper.generateObjectValue(newTargets)
            );
        }
        else {
            // if no new target had been found clear the target parameter (and skip the ssh scan)
            delegateExecution.setVariable(DefaultFields.PROCESS_TARGETS.name(),
                    ""
            );
        }
    }

}

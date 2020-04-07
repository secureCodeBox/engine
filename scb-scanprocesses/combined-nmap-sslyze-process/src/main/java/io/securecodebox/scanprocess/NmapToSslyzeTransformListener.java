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
public class NmapToSslyzeTransformListener extends TransformFindingsToTargetsListener {

    public void notify(DelegateExecution delegateExecution) throws Exception {

        List<Finding> findings = ProcessVariableHelper.readListFromValue(
                (String) delegateExecution.getVariable(DefaultFields.PROCESS_FINDINGS.name()),
                Finding.class
        );

        List<Target> newTargets = createTargetsFromFindings(findings);

        LOG.info("Created Targets out of Findings: " + newTargets);

        if (!newTargets.isEmpty() && newTargets.size() > 0) {
            // define the new SSLyze targets, based on the nmap port scan results
            delegateExecution.setVariable(DefaultFields.PROCESS_TARGETS.name(),
                    ProcessVariableHelper.generateObjectValue(newTargets)
            );
        }
        else {
            // if no new target had been found clear the target parameter (and skip the SSLyze scan)
            delegateExecution.setVariable(DefaultFields.PROCESS_TARGETS.name(),
                    ""
            );
        }
    }

    protected List<Target> createTargetsFromFindings(List<Finding> findings) {
        return findings.stream()
        .filter(finding -> finding.getCategory().equals("Open Port"))
        .filter(finding -> {
            String service = (String) finding.getAttributes().get("service");
            return service.contains("https") || service.contains("ssl") || service.contains("tls");
        })
        .map(finding -> {
            String ipAddress = (String) finding.getAttributes().get("ip_address");
            String hostname = finding.getAttributes().get("hostname").toString();

            String port = finding.getAttributes().get("port").toString();

            Target target = new Target();

            if (hostname == null || "".equals(hostname)){
                target.setLocation(hostname + ":" + port);
            } else {
                target.setLocation(ipAddress + ":" + port);
            }
            target.setName("SSLyze Scan for " + ipAddress);


            return target;
        }).collect(Collectors.toList());
    }

}

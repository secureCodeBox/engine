package io.securecodebox.scanprocess.listener;

import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.scanprocess.ProcessVariableHelper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NmapToNiktoTransformListener extends TransformFindingsToTargetsListener {

    public void notify(DelegateExecution delegateExecution) throws Exception{
        List<Finding> findings = ProcessVariableHelper.readListFromValue(
                (String) delegateExecution.getVariable(DefaultFields.PROCESS_FINDINGS.name()),
                Finding.class
            );

        List<Target> newTargets = findings.stream()
                .filter(finding -> finding.getCategory().equals("Open Port"))
                .map(finding -> {
                    String hostname = (String) finding.getAttribute(OpenPortAttributes.hostname);
                    String port = finding.getAttribute(OpenPortAttributes.port).toString();

                    Target target = new Target();
                    target.setLocation(hostname);
                    target.appendOrUpdateAttribute("NIKTO_PORTS", port);

                    return target;
                }).collect(Collectors.toList());

        LOG.info("Created Targets out of Findings: " + newTargets);

        delegateExecution.setVariable(DefaultFields.PROCESS_TARGETS.name(),
                ProcessVariableHelper.generateObjectValue(newTargets)
        );
    }

}

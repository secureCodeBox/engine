package io.securecodebox.scanprocess.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.Attribute;
import io.securecodebox.model.execution.Target;
import io.securecodebox.scanprocess.ProcessVariableHelper;
import io.securecodebox.scanprocess.listener.TransformFindingsToTargetsListener;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class NmapToNiktoTransformListener extends TransformFindingsToTargetsListener {

    public void notify(DelegateExecution delegateExecution) throws Exception{

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Target> newTargets = new LinkedList<>(ProcessVariableHelper.readListFromValue(
                    (String) delegateExecution.getVariable(DefaultFields.PROCESS_FINDINGS.name()), Target.class));

            for (Target target : newTargets) {
                target.setLocation((String)target.getAttributes().get("hostname"));
                Object port = target.getAttributes().get("port");
                if (port != null) {
                    target.getAttributes().remove("port");
                    target.getAttributes().put("NIKTO_PORTS", port);
                }
            }

            LOG.info("Created Targets out of Findings: " + newTargets);


            ObjectValue objectValue = Variables.objectValue(objectMapper.writeValueAsString(newTargets))
                    .serializationDataFormat(Variables.SerializationDataFormats.JSON)
                    .create();
            delegateExecution.setVariable(DefaultFields.PROCESS_TARGETS.name(), objectValue);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Can't write field to process!", e);
        }
    }

}

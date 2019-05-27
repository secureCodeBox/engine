package io.securecodebox.scanprocess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import io.securecodebox.scanprocess.listener.TransformFindingsToTargetsListener;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CombinedTransformListener extends TransformFindingsToTargetsListener {

    public void notify(DelegateExecution delegateExecution) throws Exception{

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String findingsAsString = objectMapper.writeValueAsString(delegateExecution.getVariable(
                    DefaultFields.PROCESS_FINDINGS.name()));
            List<Target> newTargets = objectMapper.readValue(objectMapper.readValue(findingsAsString, String.class),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Target.class));

            for (Target target : newTargets) {
                if (target.getAttributes().get("DO_SSH").equals(true)) {
                    if (target.getAttributes().get("service").equals(target.getAttributes().get("SSH_SERVICE")) ||
                            target.getAttributes().get("port").equals(target.getAttributes().get("SSH_PORT"))) {
                        target.setLocation(target.getAttributes().get("hostname") + ":" + target.getAttributes().get("port"));
                    }
                }
                if (target.getAttributes().get("DO_SSLYZE").equals(true)) {
                    if (target.getAttributes().get("service").equals(target.getAttributes().get("SSLYZE_SERVICE")) ||
                            target.getAttributes().get("port").equals(target.getAttributes().get("SSLYZE_PORT"))) {
                        target.setLocation(target.getAttributes().get("hostname") + ":" + target.getAttributes().get("port"));
                    }
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

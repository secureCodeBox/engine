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
import java.util.Map;

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
                Map<String, Object> attributes = target.getAttributes();

                if (attributes.get("DO_SSH").equals(true)) {
                    if (attributes.get("service").equals(attributes.get("SSH_SERVICE")) ||
                            attributes.get("port").equals(attributes.get("SSH_PORT"))) {
                        target.setLocation(attributes.get("hostname") + ":" + attributes.get("port"));
                        target.appendOrUpdateAttribute("SECOND_SCAN", "ssh");
                    }
                }
                if (attributes.get("DO_SSLYZE").equals(true)) {
                    if (attributes.get("service").equals(attributes.get("SSLYZE_SERVICE")) ||
                            attributes.get("port").equals(attributes.get("SSLYZE_PORT"))) {
                        target.setLocation(attributes.get("hostname") + ":" + attributes.get("port"));
                        target.appendOrUpdateAttribute("SECOND_SCAN", "sslyze");
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

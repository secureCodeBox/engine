package io.securecodebox.scanprocess.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.Attribute;
import io.securecodebox.model.execution.Target;
import io.securecodebox.scanprocess.listener.TransformFindingsToTargetsListener;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NmapToNiktoTransformListener extends TransformFindingsToTargetsListener {

    public void notify(DelegateExecution delegateExecution) throws Exception{

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String findingsAsString = objectMapper.writeValueAsString(delegateExecution.getVariable(
                    DefaultFields.PROCESS_FINDINGS.name()));
            List<Target> newTargets = objectMapper.readValue(objectMapper.readValue(findingsAsString, String.class),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Target.class));

            if (delegateExecution.getVariable(DefaultFields.PROCESS_ATTRIBUTE_MAPPING.name()) != null) {
                List<Attribute> attributeMapping = objectMapper.readValue((String) delegateExecution.getVariable(
                        DefaultFields.PROCESS_ATTRIBUTE_MAPPING.name()),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Attribute.class));

                for (Target target : newTargets) {
                    target.setLocation((String)target.getAttributes().get("hostname"));
                    for (Attribute attribute : attributeMapping) {
                        Object value = target.getAttributes().get(attribute.getFrom());
                        if (value != null) {
                            target.getAttributes().remove(attribute.getFrom());
                            target.getAttributes().put(attribute.getTo(), value);
                        }
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

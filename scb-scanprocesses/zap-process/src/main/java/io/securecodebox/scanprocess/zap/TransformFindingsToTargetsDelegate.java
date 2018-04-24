package io.securecodebox.scanprocess.zap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransformFindingsToTargetsDelegate implements ExecutionListener {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TransformFindingsToTargetsDelegate.class);

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        String findingsAsString = objectMapper.writeValueAsString(delegateExecution.getVariable(
                DefaultFields.PROCESS_FINDINGS.name()));
        List<Target> newTargets = objectMapper.readValue(objectMapper.readValue(findingsAsString, String.class),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Target.class));

        LOG.info("Created Targets out of Findings: " + newTargets);

        try {
            ObjectValue objectValue = Variables.objectValue(objectMapper.writeValueAsString(newTargets))
                    .serializationDataFormat(Variables.SerializationDataFormats.JSON)
                    .create();
            delegateExecution.setVariable(DefaultFields.PROCESS_TARGETS.name(), objectValue);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Can't write field to process!", e);
        }

    }
}

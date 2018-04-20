package io.securecodebox.scanprocess.zap;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FinishedSpiderTaskListener implements ExecutionListener {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(FinishedSpiderTaskListener.class);

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        String findingsAsString = objectMapper.writeValueAsString(delegateExecution.getVariable(
                DefaultFields.PROCESS_FINDINGS.name()));
        List<Target> newTargets = objectMapper.readValue(objectMapper.readValue(findingsAsString, String.class),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Target.class));

        LOG.info("Created Targets out of Findings: " + newTargets);

        String targetsAsJson = "{\n" +
                "type: 'Object'," +
                "value: " + objectMapper.writeValueAsString(newTargets) + "," +
                "valueType: {\n" +
                    "serializationDataFormat: 'application/json',\n" +
                    "objectTypeName: 'java.lang.String'\n" +
                "}\n" +
                "}";

        LOG.info("Targets as Json: " + targetsAsJson);
        delegateExecution.getVariables().put(DefaultFields.PROCESS_TARGETS.name(), targetsAsJson);

    }
}

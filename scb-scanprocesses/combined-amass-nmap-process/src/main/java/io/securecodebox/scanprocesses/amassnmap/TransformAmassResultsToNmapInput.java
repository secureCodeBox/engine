package io.securecodebox.scanprocesses.amassnmap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TransformAmassResultsToNmapInput implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(TransformAmassResultsToNmapInput.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        LOG.debug("Converting amass output to nmap input");

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String findingsAsString = objectMapper.writeValueAsString(execution.getVariable(DefaultFields.PROCESS_FINDINGS.name()));
            List<Target> newTargets = objectMapper.readValue(objectMapper.readValue(findingsAsString, String.class),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Target.class));

            for (Target target : newTargets) {
                //TODO: this is not correct; Fix location in amass scan and use location instead
                target.getAttributes().put("hostname", target.getName());
                target.setLocation(target.getName());
            }

            LOG.debug("Transformed findings to new targets: " + newTargets);

            ObjectValue objectValue = Variables.objectValue(objectMapper.writeValueAsString(newTargets))
                    .serializationDataFormat(Variables.SerializationDataFormats.JSON)
                    .create();
            execution.setVariable(DefaultFields.PROCESS_TARGETS.name(), objectValue);

            // SET NMAP PROCESS VARIABLES
            execution.setVariable("NMAP_CONFIGURATION_TYPE","default");

            LOG.debug("Finished TransformAmassResultsToNmapInput Service Task. Continue with nmap scan");

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Can't write field to process!", e);
        }
    }

}

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


            String nmapProfile = (String) execution.getVariable(ProcessVariables.NMAP_CONFIGURATION_PROFILE.name());

            for (Target target : newTargets) {
                //TODO: this is not correct; Fix location in amass scan and use location instead
                target.getAttributes().put("hostname", target.getName());
                target.setLocation(target.getName());
                setNmapProfile(nmapProfile, target);
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

    private void setNmapProfile(String nmapProfile, Target target) {
        switch (NmapConfigProfile.valueOf(nmapProfile)) {
            case HTTP_PORTS:
                target.appendOrUpdateAttribute("NMAP_PARAMETER", NmapConfigProfile.HTTP_PORTS.getParameter());
                break;
            case TOP_100_PORTS:
                target.appendOrUpdateAttribute("NMAP_PARAMETER", NmapConfigProfile.TOP_100_PORTS.getParameter());
                break;
            default:
                throw new IllegalArgumentException("Unknown nmap profile for combined scan");
        }
    }

}

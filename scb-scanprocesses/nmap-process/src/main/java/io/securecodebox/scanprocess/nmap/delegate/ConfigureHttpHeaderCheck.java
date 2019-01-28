package io.securecodebox.scanprocess.nmap.delegate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import io.securecodebox.model.execution.Target;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ConfigureHttpHeaderCheck implements JavaDelegate {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigureHttpHeaderCheck.class);

    @Autowired
    ScanProcessExecutionFactory processExecutionFactory;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        LOG.info("Configuring execution profile for http header check...");

        try {

            String targetsAsString = objectMapper.writeValueAsString(execution.getVariable(DefaultFields.PROCESS_TARGETS.name()));
            List<Target> targets = objectMapper.readValue(objectMapper.readValue(targetsAsString, String.class),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Target.class));

            for (Target target : targets) {
                target.appendOrUpdateAttribute("NMAP_PARAMETER", "-Pn -p 80,8080,443,8443 --script=http-headers");
            }

            ObjectValue objectValue = Variables.objectValue(objectMapper.writeValueAsString(targets))
                    .serializationDataFormat(Variables.SerializationDataFormats.JSON)
                    .create();
            execution.setVariable(DefaultFields.PROCESS_TARGETS.name(), objectValue);

            LOG.info("Finished configuring execution profile");

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Can't write field to process!", e);
        }

    }

}

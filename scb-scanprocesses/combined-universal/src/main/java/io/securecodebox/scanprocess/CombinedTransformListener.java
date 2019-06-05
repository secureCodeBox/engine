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

import java.util.*;

@Component
public class CombinedTransformListener extends TransformFindingsToTargetsListener {

    Map<String, String> defaultValues = new HashMap() {{
        put("DEFAULT_SSH_SERVICE", "ssh");
        put("DEFAULT_SSH_PORT", "22");
        put("DEFAULT_SSLYZE_SERVICE", "");
        put("DEFAULT_SSLYZE_PORT", "443");
    }};

    private List<Target> newTargets;
    private List<Target> oldTargets;

    private enum ScannerEnum {
        AMASS,
        ARACHNI,
        NIKTO,
        SSH,
        SSLYZE,
        WPSCAN,
        ZAP
    }

    public void notify(DelegateExecution delegateExecution) throws Exception{

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            //Creating "new" targets out of portscan findings
            String findingsAsString = objectMapper.writeValueAsString(delegateExecution.getVariable(
                    DefaultFields.PROCESS_FINDINGS.name()));
            newTargets = objectMapper.readValue(objectMapper.readValue(findingsAsString, String.class),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Target.class));

            //Retrieving "old" targets for the attributes config
            String oldTargetsAsString = objectMapper.writeValueAsString(delegateExecution.getVariable(
                    DefaultFields.PROCESS_TARGETS.name()));
            oldTargets = objectMapper.readValue(objectMapper.readValue(oldTargetsAsString, String.class),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Target.class));

            ScannerEnum[] scannerNames = {ScannerEnum.SSH, ScannerEnum.SSLYZE};

            for (ScannerEnum scannerName: scannerNames) {
                createTargetsFor(scannerName);
            }

            LOG.info("Created Targets out of Findings: " + newTargets);

            // TODO: SET COLLECTION PROCESS VARIABLE WITH SERIALIZED TARGETS
            Collection<ObjectValue> objectValue = new LinkedList<>();
            for (Target target: newTargets) {
                objectValue.add(Variables.objectValue(objectMapper.writeValueAsString(target))
                        .serializationDataFormat(Variables.SerializationDataFormats.JSON)
                        .create());
            }
            delegateExecution.setVariable(DefaultFields.PROCESS_TARGETS.name(), objectValue);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Can't write field to process!", e);
        }
    }

    private void createTargetsFor (ScannerEnum scannerName) {
        //Setting default values if none were provided
        Map<String, Object> targetAttributes = oldTargets.get(0).getAttributes();
        if (targetAttributes.get(scannerName+"_SERVICE") == null || targetAttributes.get(scannerName+"_SERVICE").equals(""))
            oldTargets.get(0).appendOrUpdateAttribute(scannerName+"_SERVICE", defaultValues.get("DEFAULT_"+scannerName+"_SERVICE"));
        if (targetAttributes.get(scannerName+"_PORT") == null || targetAttributes.get(scannerName+"_PORT").equals(""))
            oldTargets.get(0).appendOrUpdateAttribute("SSH_PORT", defaultValues.get("DEFAULT_SSH_PORT"));
        targetAttributes = oldTargets.get(0).getAttributes();

        List<Target> scannerTargets = new LinkedList<>();

        for (Target target : newTargets) {
            Map<String, Object> attributes = target.getAttributes();

            if (targetAttributes.get("DO_"+scannerName).equals(true)) {
                if (!attributes.containsKey("SECOND_SCAN") && (
                        targetAttributes.get(scannerName+"_SERVICE").toString().contains(attributes.get("service").toString()) ||
                        targetAttributes.get(scannerName+"_PORT").toString().contains(attributes.get("port").toString()))) {
                    Target newTarget = target;
                    newTarget.setLocation(attributes.get("hostname") + ":" + attributes.get("port"));
                    newTarget.appendOrUpdateAttribute("SECOND_SCAN", scannerName);
                    scannerTargets.add(newTarget);
                }
            }
        }

        newTargets.addAll(scannerTargets);
    }
}

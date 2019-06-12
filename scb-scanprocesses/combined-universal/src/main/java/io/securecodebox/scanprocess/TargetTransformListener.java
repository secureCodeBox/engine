package io.securecodebox.scanprocess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.execution.Target;
import io.securecodebox.scanprocess.listener.TransformFindingsToTargetsListener;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component
public class TargetTransformListener extends TransformFindingsToTargetsListener {

    public void notify(DelegateExecution delegateExecution) throws Exception{

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String targetAsString = objectMapper.writeValueAsString(delegateExecution.getVariable("TARGET"));
            Target target = objectMapper.readValue(objectMapper.readValue(targetAsString, String.class),
                    objectMapper.getTypeFactory().constructType(Target.class));


            if (!target.getAttributes().containsKey("SECOND_SCAN")) {
                delegateExecution.setVariable("SECOND_SCAN", "none");
            } else {
                delegateExecution.setVariable("SECOND_SCAN", target.getAttributes().get("SECOND_SCAN").toString());
            }


        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Can't write field to process!", e);
        }
    }
}

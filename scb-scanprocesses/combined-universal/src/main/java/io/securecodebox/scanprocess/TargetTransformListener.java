package io.securecodebox.scanprocess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

@Component
public class TargetTransformListener implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();

        Target target = new LinkedList<>(ProcessVariableHelper.readListFromValue(
                (String) delegateExecution.getVariable("TARGET"), Target.class)).get(0);

        if (!target.getAttributes().containsKey("SECOND_SCAN")) {
            delegateExecution.setVariable("SCAN_TYPE", "none");
        } else {
            delegateExecution.setVariable("SCAN_TYPE", target.getAttributes().get("SECOND_SCAN").toString());
        }

        delegateExecution.setVariable(DefaultFields.PROCESS_TARGETS.name(), target);
    }
}

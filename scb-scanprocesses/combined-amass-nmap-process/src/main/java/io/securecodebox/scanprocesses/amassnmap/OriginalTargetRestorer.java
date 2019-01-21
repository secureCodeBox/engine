/*
 *
 *  SecureCodeBox (SCB)
 *  Copyright 2015-2018 iteratec GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package io.securecodebox.scanprocesses.amassnmap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OriginalTargetRestorer implements JavaDelegate {

    private ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(OriginalTargetRestorer.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOG.debug("Starting to restore original target");
        Target originalTarget = restoreOriginalTargetFromProcessVariable(execution);
        storeTargetInProcessVariable(originalTarget, execution);
        clearOriginalTargetProcessVariable(execution);
        LOG.debug("Finished OriginalTargetRestorer service task.");
    }


    void storeOriginalTargetInSeparateProcessVariable(Target target, DelegateExecution execution){
        try {
            ObjectValue objectValue = Variables.objectValue(objectMapper.writeValueAsString(target))
                    .serializationDataFormat(Variables.SerializationDataFormats.JSON)
                    .create();
            execution.setVariable(AdditionalProcessVariables.ORIGINAL_PROCESS_TARGET.name(), objectValue);
        } catch (JsonProcessingException e) {
            LOG.error("Could not store target in original-target process variable");
        }
    }

    void clearOriginalTargetProcessVariable(DelegateExecution execution) {
        execution.removeVariable(AdditionalProcessVariables.ORIGINAL_PROCESS_TARGET.name());
    }

    private Target restoreOriginalTargetFromProcessVariable(DelegateExecution execution) {
        Target target = new Target();
        try {
            String originalTargetAsString = objectMapper.writeValueAsString(execution.getVariable(AdditionalProcessVariables.ORIGINAL_PROCESS_TARGET.name()));
            target = objectMapper.readValue(originalTargetAsString, Target.class);
        } catch (IOException e) {
            LOG.error("Could not restore original target from process variable");
        }
        return target;
    }

    private void storeTargetInProcessVariable(Target target, DelegateExecution execution) {
        try {
            LOG.debug("Store target in process variable");

            List<Target> newTargets = new ArrayList<>();
            newTargets.add(target);

            ObjectValue objectValue = Variables.objectValue(objectMapper.writeValueAsString(newTargets))
                    .serializationDataFormat(Variables.SerializationDataFormats.JSON)
                    .create();
            execution.setVariable(DefaultFields.PROCESS_TARGETS.name(), objectValue);
        } catch (IOException e) {
            LOG.error("Could not store target in process variable");
        }
    }

}

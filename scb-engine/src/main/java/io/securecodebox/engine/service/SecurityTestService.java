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
package io.securecodebox.engine.service;

import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.securitytest.SecurityTestConfiguration;
import io.securecodebox.scanprocess.ProcessVariableHelper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SecurityTestService {
    public static class NonExistentSecurityTestDefinitionException extends Exception{}
    public static class DuplicateSecurityTestDefinitionForKeyException extends Exception{}

    @Autowired
    ProcessEngine engine;

    public void checkSecurityTestDefinitionExistence(String key) throws NonExistentSecurityTestDefinitionException, DuplicateSecurityTestDefinitionForKeyException {
        long processCount = engine.getRepositoryService()
                .createProcessDefinitionQuery()
                .active()
                .processDefinitionKey(key)
                .latestVersion()
                .count();

        if (processCount == 0) {
            throw new NonExistentSecurityTestDefinitionException();
        } else if (processCount > 1) {
            throw new DuplicateSecurityTestDefinitionForKeyException();
        }
    }

    public UUID startSecurityTest(SecurityTestConfiguration securityTest){
        Map<String, Object> values = new HashMap<>();

        List<Target> targets = new LinkedList<>();
        targets.add(securityTest.getTarget());

        values.put(DefaultFields.PROCESS_AUTOMATED.name(), true);
        values.put(DefaultFields.PROCESS_CONTEXT.name(), securityTest.getContext());
        values.put(DefaultFields.PROCESS_TARGETS.name(), ProcessVariableHelper.generateObjectValue(targets));

        ProcessInstance instance = engine.getRuntimeService().startProcessInstanceByKey(securityTest.getProcessDefinitionKey(), values);
        return UUID.fromString(instance.getProcessInstanceId());
    }

    public List<String> getAvailableSecurityTestDefinitionNames(){
        List<ProcessDefinition> allProcesses = engine.getRepositoryService()
                .createProcessDefinitionQuery()
                .active()
                .latestVersion()
                .list();

        return allProcesses
                .stream()
                .map(ProcessDefinition::getKey)
                .map(SecurityTestConfiguration::getNameByProcessDefinitionKey)
                .collect(Collectors.toList());
    }
}

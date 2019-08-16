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
import io.securecodebox.engine.auth.InsufficientAuthorizationException;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.rest.Report;
import io.securecodebox.model.securitytest.SecurityTest;
import io.securecodebox.model.securitytest.SecurityTestConfiguration;
import io.securecodebox.scanprocess.ProcessVariableHelper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
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

    public UUID startSecurityTest(SecurityTestConfiguration securityTest) throws InsufficientAuthorizationException {
        Map<String, Object> values = new HashMap<>();

        List<Target> targets = new LinkedList<>();
        targets.add(securityTest.getTarget());

        values.put(DefaultFields.PROCESS_AUTOMATED.name(), true);
        values.put(DefaultFields.PROCESS_CONTEXT.name(), securityTest.getContext());
        values.put(DefaultFields.PROCESS_NAME.name(), securityTest.getName());
        values.put(DefaultFields.PROCESS_TARGETS.name(), ProcessVariableHelper.generateObjectValue(targets));
        values.put(DefaultFields.PROCESS_META_DATA.name(), securityTest.getMetaData());

        if(securityTest.getTenant() != null){
            values.put(DefaultFields.PROCESS_TENANT.name(), securityTest.getTenant());
        }

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

    public SecurityTest getCompletedSecurityTest(UUID id) throws SecurityTestNotFoundException, SecurityTestErroredException {
        List<HistoricProcessInstance> processInstances = engine.getHistoryService()
                .createHistoricProcessInstanceQuery()
                .processInstanceId(id.toString())
                .list();

        if(processInstances.size() != 1){
            throw new SecurityTestNotFoundException();
        }
        HistoricProcessInstance process = processInstances.get(0);

        List<HistoricIncident> incidents = engine.getHistoryService().createHistoricIncidentQuery().processInstanceId(id.toString()).list();

        if(!incidents.isEmpty()){
            throw new SecurityTestErroredException();
        }

        Map<String, HistoricVariableInstance> variables = engine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceId(id.toString())
                .list()
                .stream()
                .collect(Collectors.toMap(HistoricVariableInstance::getName, Function.identity()));

        Report report = null;

        if(process.getEndTime() != null){
            List<Finding> findings = getListValue(variables, DefaultFields.PROCESS_FINDINGS, Finding.class);
            String rawFindings = (String) variables.get(DefaultFields.PROCESS_RAW_FINDINGS.name()).getValue();

            report = new Report(findings, rawFindings);
        }

        String context = (String) variables.get(DefaultFields.PROCESS_CONTEXT.name()).getValue();
        String name = (String) variables.get(DefaultFields.PROCESS_NAME.name()).getValue();
        String tenant = null;
        if(variables.containsKey(DefaultFields.PROCESS_TENANT.name())){
            tenant = (String) variables.get(DefaultFields.PROCESS_TENANT.name()).getValue();
        }

        List<Target> targets = getListValue(variables, DefaultFields.PROCESS_TARGETS, Target.class);
        Map<String, String> metaData = (Map<String, String>) variables.get(DefaultFields.PROCESS_META_DATA.name()).getValue();

        return new SecurityTest(id, context, name, targets.get(0), report, metaData, tenant, process.getStartTime(), Optional.ofNullable(process.getEndTime()));
    }

    private <T> List<T> getListValue(Map<String, HistoricVariableInstance> variables, DefaultFields name, Class<T> type) {
        String serializedValue = (String) variables.get(name.name()).getValue();
        return ProcessVariableHelper.readListFromValue(serializedValue, type);
    }

    public static class SecurityTestNotFoundException extends Exception { }
    public static class SecurityTestErroredException extends Exception { }
}

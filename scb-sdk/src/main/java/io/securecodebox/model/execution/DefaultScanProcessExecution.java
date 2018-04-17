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

package io.securecodebox.model.execution;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.findings.Finding;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 08.03.18
 */
public class DefaultScanProcessExecution extends ExecutionAware implements ScanProcessExecution {

    @JsonIgnore
    private static final Logger LOG = LoggerFactory.getLogger(DefaultScanProcessExecution.class);

    @JsonIgnore
    protected ObjectMapper objectMapper;

    public DefaultScanProcessExecution(DelegateExecution execution) {
        super(execution);
        objectMapper = new ObjectMapper();
    }

    @Override
    public UUID getId() {
        return UUID.fromString(execution.getId());
    }

    @Override
    public void setContext(String context) {
        execution.setVariable(DefaultFields.PROCESS_CONTEXT.name(), context);
    }

    @Override
    public String getContext() {
        StringValue data = execution.<StringValue>getVariableTyped(DefaultFields.PROCESS_CONTEXT.name());
        return data != null ? data.getValue() : "";
    }

    @Override
    public boolean isRunning() {
        return !execution.isCanceled();
    }

    @Override
    public boolean hasScanner() {
        return !getScanners().isEmpty();
    }

    @Override
    public synchronized void addScanner(Scanner scanner) {
        List<Scanner> scanners = getJsonFromProcessVariableModifiable(DefaultFields.PROCESS_SCANNERS, Scanner.class);
        scanners.add(scanner);
        writeToProcess(DefaultFields.PROCESS_SCANNERS, scanners);
    }

    @Override
    public List<Scanner> getScanners() {
        return Collections.unmodifiableList(
                getJsonFromProcessVariableModifiable(DefaultFields.PROCESS_SCANNERS, Scanner.class));
    }

    private <T> List<T> getJsonFromProcessVariableModifiable(Enum<?> field, Class<T> innerClass) {
        synchronized (field) {
            Object variable = execution.getVariable(field.name());
            if (!StringUtils.isEmpty(variable)) {
                try {
                    return objectMapper.readValue((String) variable,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, innerClass));
                } catch (IOException e) {
                    LOG.error("Can't extract json field {} from process! Raw Data {}", field, variable, e);
                }
            }
            return new LinkedList<>();
        }
    }

    private void writeToProcess(Enum<?> field, List<?> data) {
        synchronized (field) {
            try {
                execution.setVariable(field.name(), objectMapper.writeValueAsString(data));
            } catch (JsonProcessingException e) {
                LOG.error("Can't write field {} to process!", field, e);
                throw new IllegalStateException("Can't write field to process!", e);
            }
        }
    }

    @Override
    public List<Finding> getFindings() {
        return Collections.unmodifiableList(
                getJsonFromProcessVariableModifiable(DefaultFields.PROCESS_FINDINGS, Finding.class));
    }

    @Override
    public void clearFindings() {
        writeToProcess(DefaultFields.PROCESS_FINDINGS, new LinkedList<>());
    }

    @Override
    public void appendFinding(Finding finding) {
        List<Finding> findings = getJsonFromProcessVariableModifiable(DefaultFields.PROCESS_FINDINGS, Finding.class);
        findings.add(finding);
        writeToProcess(DefaultFields.PROCESS_FINDINGS, findings);
    }

    @Override
    public boolean isAutomated() {
        BooleanValue isAutomated = execution.getVariableTyped(DefaultFields.PROCESS_AUTOMATED.name());
        return isAutomated != null ? isAutomated.getValue() : false;
    }

    @Override
    public String getTenantId() {
        StringValue tenantId = execution.<StringValue>getVariableTyped(DefaultFields.PROCESS_TENANT_ID.name());
        return tenantId != null ? tenantId.getValue() : null;
    }
}

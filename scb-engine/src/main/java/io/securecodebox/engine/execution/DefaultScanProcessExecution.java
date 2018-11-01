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

package io.securecodebox.engine.execution;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.rest.Report;
import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.Scanner;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.scanprocess.ProcessVariableHelper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 08.03.18
 */
@Configurable
public class DefaultScanProcessExecution implements ScanProcessExecution {

    @JsonIgnore
    protected DelegateExecution execution;

    public DefaultScanProcessExecution(DelegateExecution execution) {
        this.execution = execution;
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
                return ProcessVariableHelper.readListFromValue((String) variable, innerClass);
            }
            return new LinkedList<>();
        }
    }

    private void writeToProcess(Enum<?> field, List<?> data) {
        synchronized (field) {
            execution.setVariable(field.name(), ProcessVariableHelper.generateObjectValue(data));
        }
    }

    @Override
    public List<Finding> getFindings() {
        return Collections.unmodifiableList(
                getJsonFromProcessVariableModifiable(DefaultFields.PROCESS_FINDINGS, Finding.class));
    }

    @Override
    public String getRawFindings() {
        return (String) execution.getVariable(DefaultFields.PROCESS_RAW_FINDINGS.name());
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
    public void clearTargets() {
        writeToProcess(DefaultFields.PROCESS_TARGETS, new LinkedList<>());
    }

    @Override
    public void appendTarget(Target target) {
        List<Target> targets = getJsonFromProcessVariableModifiable(DefaultFields.PROCESS_TARGETS, Target.class);
        targets.add(target);
        writeToProcess(DefaultFields.PROCESS_TARGETS, targets);
    }

    @Override
    public List<Target> getTargets() {
        return Collections.unmodifiableList(
                getJsonFromProcessVariableModifiable(DefaultFields.PROCESS_TARGETS, Target.class));
    }

    @Override
    public boolean isAutomated() {
        BooleanValue isAutomated = execution.getVariableTyped(DefaultFields.PROCESS_AUTOMATED.name());
        return isAutomated != null ? isAutomated.getValue() : false;
    }

    @Override
    public String getScannerType(){
        return (String) execution.getVariable(DefaultFields.PROCESS_SCANNER_TYPE.name());
    }

    @Override
    public void saveReportToVariable(Report report) throws JsonProcessingException{
        ObjectMapper objectMapper = new ObjectMapper();

        String reportJson = objectMapper.writeValueAsString(report);

        this.execution.setVariable(DefaultFields.PROCESS_REPORT.toString(), ProcessVariableHelper.generateObjectValue(reportJson));
    }
}

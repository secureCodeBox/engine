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
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.findings.Finding;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.StringValue;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 08.03.18
 */
public class DefaultScanProcessExecution extends ExecutionAware implements ScanProcessExecution {

    @JsonIgnore
    private final Spider spider;
    @JsonIgnore
    private final Scanner scanner;

    public DefaultScanProcessExecution(DelegateExecution execution) {
        super(execution);
        scanner = new Scanner(execution);
        spider = new Spider(execution);
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
    public boolean hasSpider() {
        return spider.getSpiderId() != null;
    }

    @Override
    public Spider getSpider() {
        return spider;
    }

    @Override
    public boolean hasScanner() {
        return scanner.getScannerId() != null;
    }

    @Override
    public Scanner getScanner() {
        return scanner;
    }

    @Override
    public List<Finding> getFindings() {
        return scanner.getFindings();
    }

    @Override
    public void appendFinding(Finding finding) {
        getScanner().appendFinding(finding);
    }

    @Override
    public boolean isAutomated() {
        BooleanValue isAutomated = execution.getVariableTyped(DefaultFields.PROCESS_AUTOMATED.name());
        return isAutomated != null ? isAutomated.getValue() : false;
    }

    @Override
    public String getTenantId(){
        StringValue tenantId = execution.<StringValue>getVariableTyped(DefaultFields.PROCESS_TENANT_ID.name());
        return tenantId != null ? tenantId.toString() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DefaultScanProcessExecution that = (DefaultScanProcessExecution) o;
        return Objects.equals(spider, that.spider) && Objects.equals(scanner, that.scanner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spider, scanner);
    }

    @Override
    public String toString() {
        return "DefaultScanProcessExecution{" + "spider=" + spider + ", scanner=" + scanner + '}';
    }
}

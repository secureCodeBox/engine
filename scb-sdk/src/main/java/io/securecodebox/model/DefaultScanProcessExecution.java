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

package io.securecodebox.model;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.StringValue;

import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 08.03.18
 */
public class DefaultScanProcessExecution implements ScanProcessExecution {

    protected DelegateExecution execution;

    public DefaultScanProcessExecution(DelegateExecution execution) {
        super();
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
        return execution.getVariableTyped(DefaultFields.PROCESS_CONTEXT.name());
    }

    @Override
    public boolean isRunning() {
        return !execution.isCanceled();
    }

    @Override
    public boolean hasSpider() {
        return getScannerId() != null;
    }

    @Override
    public void setSpiderId(UUID id) {
        execution.setVariable(DefaultFields.PROCESS_SPIDER_ID.name(), id.toString());
    }

    @Override
    public UUID getSpiderId() {
        StringValue input = execution.getVariableTyped(DefaultFields.PROCESS_SPIDER_ID.name());
        return input != null ? UUID.fromString(input.getValue()) : null;
    }

    @Override
    public void setSpiderType(String type) {
        execution.setVariable(DefaultFields.PROCESS_SPIDER_TYPE.name(), type);
    }

    @Override
    public String getSpiderType() {
        return execution.getVariableTyped(DefaultFields.PROCESS_SPIDER_TYPE.name());
    }

    @Override
    public boolean hasScanner() {
        return getScannerId() != null;
    }

    @Override
    public void setScannerId(UUID id) {
        execution.setVariable(DefaultFields.PROCESS_SCANNER_ID.name(), id.toString());
    }

    @Override
    public UUID getScannerId() {
        StringValue input = execution.getVariableTyped(DefaultFields.PROCESS_SCANNER_ID.name());
        return input != null ? UUID.fromString(input.getValue()) : null;
    }

    @Override
    public void setScannerType(String type) {
        execution.setVariable(DefaultFields.PROCESS_SCANNER_TYPE.name(), type);
    }

    @Override
    public String getScannerType() {
        return execution.getVariableTyped(DefaultFields.PROCESS_SCANNER_TYPE.name());
    }

    @Override
    public boolean isAutomated() {
        BooleanValue isAutomated = execution.getVariableTyped(DefaultFields.PROCESS_AUTOMATED.name());
        return isAutomated != null ? isAutomated.getValue() : false;
    }
}

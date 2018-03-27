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

package io.securecodebox.scanprocess;

import io.securecodebox.model.execution.DefaultScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

import static io.securecodebox.scanprocess.DefaultScanProcessExcecutionFactory.FactoryFields.PROCESS_EXECUTION_TYPE;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 01.03.18
 */
@Component
public class DefaultScanProcessExcecutionFactory implements ScanProcessExecutionFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultScanProcessExcecutionFactory.class);

    @Override
    public ScanProcessExecution get(DelegateExecution execution) {
        return get(execution, DefaultScanProcessExecution.class);
    }

    @Override
    public <P extends ScanProcessExecution> P get(DelegateExecution execution, Class<P> customProcess) {
        StringValue value = execution.getVariableTyped(PROCESS_EXECUTION_TYPE.name());
        LOG.debug("Reading {} is {} target is {}", PROCESS_EXECUTION_TYPE.name(),
                value != null ? value.getValue() : "null", customProcess.getTypeName());
        if (value == null || DefaultScanProcessExecution.class.getTypeName().equals(customProcess.getTypeName())
                || ScanProcessExecution.class.getTypeName().equals(customProcess.getTypeName()) || customProcess.getTypeName()
                .equals(value.getValue())) {
            return getInstance(execution, customProcess);
        } else {
            throw new IllegalArgumentException("The given process Class does not match the existing!");
        }
    }

    private <P extends ScanProcessExecution> P getInstance(DelegateExecution execution, Class<P> customProcess) {
        try {
            execution.setVariable(PROCESS_EXECUTION_TYPE.name(), customProcess.getTypeName());
            LOG.debug("Writing {} to {}", PROCESS_EXECUTION_TYPE.name(), customProcess.getTypeName());
            return customProcess.getConstructor(DelegateExecution.class).newInstance(execution);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            LOG.error("Error creating custom process execution", e);
            throw new IllegalStateException("Error creating custom process execution", e);
        }
    }

    protected enum FactoryFields {
        PROCESS_EXECUTION_TYPE
    }
}

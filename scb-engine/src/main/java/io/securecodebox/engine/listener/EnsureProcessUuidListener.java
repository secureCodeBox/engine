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
package io.securecodebox.engine.listener;

import io.securecodebox.scanprocess.ScanProcessExecution;
import io.securecodebox.scanprocess.ScanProcessExecutionFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author Robert Seedorff - iteratec GmbH
 */
@Component
class EnsureProcessUuidListener implements ExecutionListener {

    private static final Logger LOG = LoggerFactory.getLogger(EnsureProcessUuidListener.class);

    @Autowired
    ScanProcessExecutionFactory factory;

    @Override
    public void notify(DelegateExecution execution) {
        ScanProcessExecution scanProcessExecution = factory.get(execution);
        LOG.debug("Current process UUID {}", scanProcessExecution.getProcessUuid());

        if (scanProcessExecution.getProcessUuid() == null) {
            scanProcessExecution.setProcessUuid(UUID.fromString(execution.getId()));
            LOG.debug("Setting new process UUID {}", scanProcessExecution.getProcessUuid());
        }
    }
}

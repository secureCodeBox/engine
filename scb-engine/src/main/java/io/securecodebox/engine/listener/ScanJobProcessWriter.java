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

/**
 *
 */
package io.securecodebox.engine.listener;

import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.scanprocess.PersistenceAwareTaskListener;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Robert Seedorff - iteratec GmbH
 */
@Component
class ScanJobProcessWriter extends PersistenceAwareTaskListener {
    private static final Logger LOG = LoggerFactory.getLogger(ScanJobProcessWriter.class);

    @Autowired
    ScanProcessExecutionFactory factory;

    /* (non-Javadoc)
     * @see org.camunda.bpm.engine.delegate.ExecutionListener#notify(org.camunda.bpm.engine.delegate.DelegateExecution)
     */
    @Override
    public void notify(DelegateExecution execution) throws Exception {
        ScanProcessExecution scanProcessExecution = factory.get(execution);
        LOG.info("Storing result for process {}", scanProcessExecution.getId());

        // persist the extended results to the persistence store
        //storeResult(scanProcessExecution.getProcessUuid(), scanProcessExecution.toMap(), .job.indexType, scanProcessExecution.getTenantId(), "")
    }
}

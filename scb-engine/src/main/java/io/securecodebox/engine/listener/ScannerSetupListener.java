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

import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 13.04.18
 */
@Component
public class ScannerSetupListener implements ExecutionListener {

    @Autowired
    ScanProcessExecutionFactory factory;

    private static final Logger LOG = LoggerFactory.getLogger(ScannerSetupListener.class);

    @Override
    public void notify(DelegateExecution instance) throws Exception {
        LOG.info("TestListener called :)");
        ScanProcessExecution scanProcessExecution = factory.get(instance);
        scanProcessExecution.addScanner(ScannerBuilder.init().buildByExecution(instance));

    }
}

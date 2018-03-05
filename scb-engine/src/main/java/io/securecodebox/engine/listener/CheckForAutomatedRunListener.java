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

import io.securecodebox.constants.CommonConstants;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Robert Seedorff - iteratec GmbH
 */
@Component
class CheckForAutomatedRunListener implements ExecutionListener {
    private static final Logger LOG = LoggerFactory.getLogger(CheckForAutomatedRunListener.class);

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        LOG.trace("Checking for automated run");

        // Define default values if the current process is not
        // started by another automated process (Jenkins, Bamboo) with
        // already defined process variables.
        if (!execution.hasVariable(CommonConstants.AUTOMATED_RUN)) {
            execution.setVariable(CommonConstants.AUTOMATED_RUN, false);
            LOG.trace("No automated run detected");
        }

    }
}

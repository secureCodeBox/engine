#set( $component = $package.replace(".", "_") )

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

package $package;

import io.securecodebox.model.rest.Report;
import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import io.securecodebox.persistence.PersistenceProvider;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Example process saving results to the persistence.
 *
 * @author Rüdiger Heins - iteratec GmbH
 * @since 04.04.18
 */
@Component("${component}_SummaryGeneratorDelegate")
public class SummaryGeneratorDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(SummaryGeneratorDelegate.class);

    @Autowired
    PersistenceProvider persistenceProvider;

    @Autowired
    ScanProcessExecutionFactory executionFactory;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        ScanProcessExecution scanProcessExecution = executionFactory.get(delegateExecution);

        Report report = new Report(scanProcessExecution);
        persist(report);
    }

    /**
     * Eventually consistent: try to persist if the persistence provider is currently available.
     *
     * @param report The generic report of findings to persist.
     */
    private void persist(Report report) {
        LOG.trace("starting scan report persistence. {}", report);

        try {
            persistenceProvider.persist(report);
        } catch (Exception e) {
            LOG.error("Unexpected Error while trying to init a persistence provider!", e);
        }
    }

}

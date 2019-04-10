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

package io.securecodebox.scanprocess.delegate;

import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.securitytest.SecurityTest;
import io.securecodebox.persistence.PersistenceException;
import io.securecodebox.persistence.PersistenceProvider;
import io.securecodebox.scanprocess.ProcessVariableHelper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 04.04.18
 */
@Component
public class SummaryGeneratorDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(SummaryGeneratorDelegate.class);

    @Autowired(required = false)
    List<PersistenceProvider> persistenceProviders;

    @Autowired
    ScanProcessExecutionFactory executionFactory;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        List<Finding> findings = new LinkedList<>(ProcessVariableHelper.readListFromValue(
                (String) delegateExecution.getVariable(DefaultFields.PROCESS_FINDINGS.name()), Finding.class));
        removeDuplicates(findings);
        delegateExecution.removeVariable(DefaultFields.PROCESS_FINDINGS.name());
        delegateExecution.setVariable(DefaultFields.PROCESS_FINDINGS.name(), ProcessVariableHelper.generateObjectValue(findings));

        ScanProcessExecution scanProcessExecution = executionFactory.get(delegateExecution);
        SecurityTest securityTest = new SecurityTest(scanProcessExecution);

        persist(securityTest);
    }

    /**
     * Eventually consistent: try to persist if the persistence provider is currently available.
     *
     * @param securityTest The securityTest to persist.
     */
    private void persist(SecurityTest securityTest) {
        LOG.trace("starting securityTest persistence. {}", securityTest);

        try {
            if (persistenceProviders == null || persistenceProviders.isEmpty()) {
                LOG.warn("No persistence providers were enabled. If you want your findings to get persisted you'll need to enable one via the app properties / environment variables. E.g. 'securecodebox.persistence.elasticsearch.enabled: \"true\"'");
                return;
            }

            for (PersistenceProvider persistenceProvider: persistenceProviders) {
                persistenceProvider.persist(securityTest);
            }
        } catch (PersistenceException e) {
            LOG.error("Persistence provider errored while trying to save report. Going to create incident.", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Unexpected Error while trying to init a persistence provider!", e);
        }
    }

    private static void removeDuplicates(List<Finding> findings){

        List<Finding> withoutDuplicates = new LinkedList<>();
        for(Finding f : findings){
            boolean contains = false;
            for(Finding fi : withoutDuplicates){
                if(fi.equalsIgnoringId(f)){
                    contains = true;
                }
            }
            if(!contains){
                withoutDuplicates.add(f);
            }
        }

        findings.clear();
        findings.addAll(withoutDuplicates);
    }

}

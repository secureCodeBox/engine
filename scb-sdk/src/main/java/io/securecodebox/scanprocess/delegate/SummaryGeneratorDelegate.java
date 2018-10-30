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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.Report;
import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.rest.Result;
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

    @Autowired
    PersistenceProvider persistenceProvider;

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
        Report report = new Report(scanProcessExecution);

        try {
            scanProcessExecution.saveResultToVariable(Result.fromExecution(scanProcessExecution));
        } catch (JsonProcessingException e) {
            LOG.error("Could not save result to process variables.");
        }

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

            if (persistenceProvider != null) {
                persistenceProvider.persist(report);
            }
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

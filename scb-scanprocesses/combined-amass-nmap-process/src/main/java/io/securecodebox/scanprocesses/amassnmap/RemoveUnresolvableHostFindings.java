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
package io.securecodebox.scanprocesses.amassnmap;

import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import io.securecodebox.model.findings.Finding;
import java.util.List;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RemoveUnresolvableHostFindings implements ExecutionListener {

    protected static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RemoveUnresolvableHostFindings.class);

    @Autowired
    ScanProcessExecutionFactory processExecutionFactory;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        LOG.debug("Filter unresolvable host results");

        ScanProcessExecution scanProcess = processExecutionFactory.get(execution);
        List<Finding> findings = scanProcess.getFindings();
        LOG.debug("Number of Findings: " + findings.size());

        List<Finding> filteredFindings = findings.stream()
                .filter(finding -> !"Host Unresolvable".equals(finding.getCategory()))
                .collect(Collectors.toList());

        scanProcess.clearFindings();
        filteredFindings.stream().forEach(finding -> scanProcess.appendFinding(finding));

        LOG.debug("Removed finding for unresolvable host. Number of remaining findings: " + scanProcess.getFindings().size());
    }
}

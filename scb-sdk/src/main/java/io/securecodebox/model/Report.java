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

import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.findings.Finding;

import java.util.List;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 09.03.18
 */
public class Report {

    private ScanProcessExecution execution;

    public Report(ScanProcessExecution execution) {
        this.execution = execution;
    }

    public ScanProcessExecution getExecution() {
        return execution;
    }

    public List<Finding> getFindings() {
        return execution.getFindings();
    }

}

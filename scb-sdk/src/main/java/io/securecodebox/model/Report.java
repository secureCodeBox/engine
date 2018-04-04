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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.findings.Severity;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 09.03.18
 */
@JsonPropertyOrder({"execution", "findings", "severity_highest", "severity_overview"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Report {

    private ScanProcessExecution execution;

    public Report(ScanProcessExecution execution) {
        this.execution = execution;
    }

    public ScanProcessExecution getExecution() {
        return execution;
    }

    @JsonIgnore
    public List<Finding> getFindings() {
        return execution.getFindings();
    }

    @JsonProperty("severity_highest")
    public Severity getHighestSeverity() {
        return getFindings().stream()
                .map(Finding::getSeverity)
                .max(Comparator.comparing(Enum::ordinal))
                .orElse(Severity.INFORMATIONAL);
    }

    @JsonProperty("severity_overview")
    public Map<Severity, Long> getSeverityOverview() {
        return getFindings().stream().collect(groupingBy(Finding::getSeverity, counting()));
    }

}

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

package io.securecodebox.model.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.findings.Severity;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 09.03.18
 */
public class Report {
    @JsonProperty("report_id")
    private UUID id = UUID.randomUUID();

    @JsonProperty("security_test_id")
    private UUID securityTestId;

    @JsonProperty("context")
    private String context;

    @JsonProperty("findings")
    private List<Finding> findings;

    @JsonProperty("raw_findings")
    private String rawFindings;

    @JsonProperty("targets")
    private List<Target> targets;

    @JsonProperty("scanner_type")
    private String scannerType;

    @JsonProperty("severity_highest")
    private Severity highestSeverity;

    @JsonProperty("severity_overview")
    private Map<Severity, Long> severityOverview;

    public Report(){}

    public Report(ScanProcessExecution execution) {
        this.securityTestId = execution.getId();
        this.context = execution.getContext();
        this.findings = execution.getFindings();
        this.rawFindings = execution.getRawFindings();
        this.targets = execution.getTargets();
        this.scannerType = execution.getScannerType();

        this.highestSeverity = getFindings().stream()
                .map(Finding::getSeverity)
                .max(Comparator.comparing(Enum::ordinal))
                .orElse(Severity.INFORMATIONAL);

        this.severityOverview = getFindings().stream()
                .collect(groupingBy(Finding::getSeverity, counting()));
    }

    @JsonIgnore
    public UUID getId(){
        return id;
    }

    @JsonIgnore
    public void setId(UUID id){
        this.id = id;
    }

    @JsonIgnore
    public UUID getSecurityTestId() {
        return securityTestId;
    }

    @JsonIgnore
    public void setSecurityTestId(UUID securityTestId) {
        this.securityTestId = securityTestId;
    }

    @JsonIgnore
    public List<Finding> getFindings() {
        return findings;
    }

    @JsonIgnore
    public void setFindings(List<Finding> findings) {
        this.findings = findings;
    }

    @JsonIgnore
    public String getRawFindings() {
        return rawFindings;
    }

    @JsonIgnore
    public void setRawFindings(String rawFindings) {
        this.rawFindings = rawFindings;
    }

    @JsonIgnore
    public String getContext() {
        return context;
    }

    @JsonIgnore
    public void setContext(String context) {
        this.context = context;
    }

    @JsonIgnore
    public List<Target> getTargets() {
        return targets;
    }

    @JsonIgnore
    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }

    @JsonIgnore
    public String getScannerType() {
        return scannerType;
    }

    @JsonIgnore
    public void setScannerType(String scannerType) {
        this.scannerType = scannerType;
    }

    @JsonIgnore
    public Severity getHighestSeverity() {
        return highestSeverity;
    }

    @JsonIgnore
    public void setHighestSeverity(Severity highestSeverity) {
        this.highestSeverity = highestSeverity;
    }

    @JsonIgnore
    public Map<Severity, Long> getSeverityOverview() {
        return severityOverview;
    }

    @JsonIgnore
    public void setSeverityOverview(Map<Severity, Long> severityOverview) {
        this.severityOverview = severityOverview;
    }
}

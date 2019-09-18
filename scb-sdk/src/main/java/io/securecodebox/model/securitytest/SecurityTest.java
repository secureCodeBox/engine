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
package io.securecodebox.model.securitytest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.rest.Report;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SecurityTest extends AbstractSecurityTest {
    @JsonProperty
    UUID id;

    @JsonProperty
    Report report;

    public SecurityTest() {}

    public SecurityTest(UUID id, String context, String name, Target target, Report report, Map<String, String> metaData) {
        this(id, context, name, target, report, metaData, null);
    }

    public SecurityTest(UUID id, String context, String name, Target target, Report report, Map<String, String> metaData, String tenant) {
        this(id, context, name, target, report, metaData, tenant, null, Optional.empty());
    }

    public SecurityTest(UUID id, String context, String name, Target target, Report report, Map<String, String> metaData, String tenant, Date startedAt, Optional<Date> endedAt) {
        this.id = id;
        this.context = context;
        this.name = name;
        this.target = target;
        this.report = report;
        this.tenant = tenant;
        this.setMetaData(metaData);
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public SecurityTest(ScanProcessExecution execution){
        this.id = execution.getId();
        this.context = execution.getContext();
        this.name = execution.getName();
        this.setMetaData(execution.getMetaData());
        if(execution.getTargets().isEmpty()){
            this.target = null;
        } else {
            this.target = execution.getTargets().get(0);
        }
        this.report = new Report(execution);
        this.startedAt = execution.getStartDate();
        this.endedAt = execution.getEndDate();
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @JsonProperty("finished")
    @ApiModelProperty(
            value = "Indicates weather the process was completed.",
            example = "true"
    )
    public boolean isFinished(){
        return this.report != null;
    }

    @JsonProperty("durationInMilliSeconds")
    @ApiModelProperty(
            value = "Shows the current runtime duration or the time to completion in milli seconds.",
            example = "42"
    )
    public Long getDurationInMilliSeconds() {
        if(startedAt == null){
            return null;
        }
        return endedAt.orElseGet(Date::new).getTime() - startedAt.getTime();
    }

    @JsonProperty("startedAt")
    @ApiModelProperty(
            value = "Timestamp of when the security test was started.",
            example = "42"
    )
    protected Date startedAt;
    public Date startedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    @JsonProperty("endedAt")
    @ApiModelProperty(
            value = "Timestamp of when the security test was ended. Null if still running, see finished attributes",
            example = "42"
    )
    protected Optional<Date> endedAt;
    public Optional<Date> getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Optional<Date> endedAt) {
        this.endedAt = endedAt;
    }
}

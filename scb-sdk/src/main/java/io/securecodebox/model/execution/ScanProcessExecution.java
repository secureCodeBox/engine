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

package io.securecodebox.model.execution;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.securecodebox.model.findings.Finding;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 08.03.18
 */
@JsonPropertyOrder({ "id", "context", "automated", "scanners", "scanner_type", "tenant_id", "startDate", "endDate", "durationInMilliSeconds" })
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface ScanProcessExecution {

    @JsonProperty("id")
    UUID getId();

    @JsonProperty("context")
    void setContext(String context);

    @JsonProperty("context")
    String getContext();

    @JsonIgnore
    boolean isRunning();

    @JsonIgnore
    boolean hasScanner();

    void addScanner(Scanner scanner);

    @JsonProperty("scanners")
    List<Scanner> getScanners();

    /**
     * Returns the Findings directly attached to the process. Mostly it's the result of the last step.
     * If the process has multiple scanners you might want to have a look into getScanners().
     */
    @JsonProperty("findings")
    List<Finding> getFindings();

    /**
     * Returns the RawFindings directly attached to the process. Mostly it's the result of the last step.
     */
    @JsonIgnore
    String getRawFindings();

    /**
     * Clears the Findings currently attached to this process findings.
     * If the process has multiple scanners you might want to have a look into getScanners().
     */
    @JsonIgnore
    void clearFindings();

    @JsonProperty("metaData")
    Map<String,String> getMetaData();

    /**
     * Attaches Findings directly to the process instance.
     * If the process has multiple scanners you might want to have a look into getScanners().
     *
     * @param finding
     */
    @JsonIgnore
    void appendFinding(Finding finding);

    @JsonIgnore
    void appendFindings(List<Finding> newFindings);

    void appendTarget(Target target);

    List<Target> getTargets();

    void clearTargets();

    @JsonProperty("automated")
    boolean isAutomated();

    @JsonProperty("scanner_type")
    String getScannerType();

    @JsonProperty("name")
    String getName();

    @JsonProperty("name")
    void setName(String name);

    @JsonProperty("durationInMilliSeconds")
    Long getDurationInMilliSeconds();

    @JsonProperty("startDate")
    Date getStartDate();

    @JsonProperty("endDate")
    Optional<Date> getEndDate();
}

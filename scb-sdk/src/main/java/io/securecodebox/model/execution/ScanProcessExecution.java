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

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 08.03.18
 */
@JsonPropertyOrder({ "id", "context", "automated", "scanners", "scanner_type", "tenant_id" })
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface ScanProcessExecution {

    @JsonProperty("id")
    public abstract UUID getId();

    @JsonProperty("context")
    public abstract void setContext(String id);

    @JsonProperty("context")
    public abstract String getContext();

    @JsonIgnore
    public abstract boolean isRunning();

    @JsonIgnore
    public abstract boolean hasScanner();

    public void addScanner(Scanner scanner);

    @JsonProperty("scanners")
    public List<Scanner> getScanners();

    /**
     * Returns the Findings directly attached to the process. Mostly it's the result of the last step.
     * If the process has multiple scanners you might want to have a look into getScanners().
     */
    @JsonProperty("findings")
    public abstract List<Finding> getFindings();

    /**
     * Returns the RawFindings directly attached to the process. Mostly it's the result of the last step.
     */
    @JsonIgnore
    public abstract String getRawFindings();

    /**
     * Clears the Findings currently attached to this process findings.
     * If the process has multiple scanners you might want to have a look into getScanners().
     */
    @JsonIgnore
    public abstract void clearFindings();

    @JsonProperty("metaData")
    Map<String,String> getMetaData();

    /**
     * Attaches Findings directly to the process instance.
     * If the process has multiple scanners you might want to have a look into getScanners().
     *
     * @param finding
     */
    @JsonIgnore
    public abstract void appendFinding(Finding finding);

    public abstract void appendTarget(Target target);

    public abstract List<Target> getTargets();

    public abstract void clearTargets();

    @JsonProperty("automated")
    public abstract boolean isAutomated();

    @JsonProperty("scanner_type")
    public abstract String getScannerType();

    @JsonProperty("name")
    public String getName();

    @JsonProperty("name")
    public void setName(String name);
}

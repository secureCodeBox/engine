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
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.securecodebox.model.findings.Finding;

import java.util.List;
import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 08.03.18
 */
@JsonPropertyOrder({"id","context","automated"})
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
    public abstract boolean hasSpider();

    @JsonUnwrapped(prefix = "spider_")
    public Spider getSpider();

    @JsonIgnore
    public abstract boolean hasScanner();

    @JsonUnwrapped(prefix = "scanner_")
    public Scanner getScanner();

    /**
     * Convenience Method for getScanner().getFindings();
     */
    @JsonIgnore
    public abstract List<Finding> getFindings();

    /**
     * Convenience Method for getScanner().appendFinding(Finding);
     *
     * @param finding
     */
    @JsonIgnore
    public abstract void appendFinding(Finding finding);

    @JsonProperty("automated")
    public abstract boolean isAutomated();

}

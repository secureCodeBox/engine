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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.securecodebox.model.execution.Target;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 23.04.18
 */
@JsonPropertyOrder(alphabetic = true)
public class ScanConfiguration {

    @JsonProperty(required = true)
    UUID jobId;
    @JsonProperty
    List<Target> targets;

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ScanConfiguration that = (ScanConfiguration) o;
        return Objects.equals(jobId, that.jobId) && Objects.equals(targets, that.targets);
    }

    @Override
    public int hashCode() {

        return Objects.hash(jobId, targets);
    }

    @Override
    public String toString() {
        return "ScanConfiguration{" + "jobId=" + jobId + ", targets=" + targets + '}';
    }
}

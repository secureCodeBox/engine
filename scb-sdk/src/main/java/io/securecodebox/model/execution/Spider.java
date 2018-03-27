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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.securecodebox.constants.DefaultFields;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.value.StringValue;

import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 09.03.18
 */
@JsonPropertyOrder({ "id", "type" })
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Spider extends ExecutionAware {
    public Spider(DelegateExecution execution) {
        super(execution);
    }

    @JsonProperty("id")
    public void setSpiderId(UUID id) {
        execution.setVariable(DefaultFields.PROCESS_SPIDER_ID.name(), id.toString());
    }

    @JsonProperty("id")
    public UUID getSpiderId() {
        StringValue input = execution.getVariableTyped(DefaultFields.PROCESS_SPIDER_ID.name());
        return input != null ? UUID.fromString(input.getValue()) : null;
    }

    @JsonProperty("type")
    public void setSpiderType(String type) {
        execution.setVariable(DefaultFields.PROCESS_SPIDER_TYPE.name(), type);
    }

    @JsonProperty("type")
    public String getSpiderType() {
        StringValue value = execution.<StringValue>getVariableTyped(DefaultFields.PROCESS_SPIDER_TYPE.name());
        return value != null ? value.getValue() : "";
    }

}

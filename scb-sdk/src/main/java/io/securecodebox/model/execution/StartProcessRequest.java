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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Format required by the start process endpoint.
 * Combines the target list with other parameters required to start a scan.
 *
 * @author Jannik Hollenbach - iteratec GmbH
 * @since 27.8.18
 */
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "This type contains all parameters required to start a scan.")
public class StartProcessRequest {
    @ApiModelProperty(value = "The context of this scan.", example = "BodgeIt", required = true)
    @Size(min = 1, max = 4000)
    @Pattern(regexp = "^[\\w-]*$")
    @JsonProperty
    private String context;
    @ApiModelProperty(value = "The targets of this scan.", required = true)
    @Size(min = 1, max = 128)
    @JsonProperty
    private List<Target> targets;

    public String getContext() {
        return context;
    }

    public List<Target> getTargets() {
        return targets;
    }
}

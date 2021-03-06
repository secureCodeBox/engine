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
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents targets to scan by a scanner.
 *
 * @author Rüdiger Heins - iteratec GmbH
 * @since 17.04.18
 */
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "This type represents targets to scan by a scanner.")
public class Target {

    @ApiModelProperty(value = "The name of this target.",
                        example = "SecureCodeBox Demo Website ",
                        required = true)
    @Size(min = 1, max = 4000)
    @Pattern(regexp = "^[\\w-]*$")
    @JsonProperty
    private String name;

    @ApiModelProperty(value = "The location of this target, this could be a URL, Hostname or IP-Address.",
                        example = "127.0.0.1",
                        required = true)
    @Size(min = 1, max = 4000)
    @JsonProperty
    @Pattern(regexp = "^[^<>\\\\\\[\\]()%$]*$")
    private String location;

    @JsonProperty
    @ApiModelProperty(value = "Key (in upper case) / value pairs of target / scanner specific configuration options.",
            example = "{\"NMAP_PARAMETER\":\"-Pn\"}",
            required = false)
    private Map<String, Object> attributes = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void appendOrUpdateAttribute(String key, Object value) {
        if (attributes.get(key) != null) {
            attributes.remove(key);
        }
        attributes.put(key, value);
    }

    @Override
    public String toString() {
        return "Target{" + "name='" + name + '\'' + ", location='" + location + '\'' + ", attributes=" + attributes
                + '}';
    }
}

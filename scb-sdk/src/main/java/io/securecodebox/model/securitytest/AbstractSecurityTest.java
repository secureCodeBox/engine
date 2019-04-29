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
import io.securecodebox.model.execution.Target;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;

public abstract class AbstractSecurityTest {
    @JsonProperty
    @ApiModelProperty(
            value = "Context references the larger scope the security test. In most cases this is equal to the name of the project, team name or a domain.",
            example = "Feature Team 1"
    )
    String context;

    @JsonProperty("name")
    @ApiModelProperty(
            value = "The Name of the security test to perform on the target.",
            example = "nmap"
    )
    String name;

    @JsonProperty
    @ApiModelProperty("The target configuration of the security test.")
    Target target;

    @JsonProperty
    @ApiModelProperty(
            value = "A tenant is a camunda concept. A tenant can have both users and groups. It can be used to restrict the access to your security tests to members of the tenant.",
            allowEmptyValue = true,
            example = "team-1"
    )
    String tenant;

    @JsonProperty
    private Map<String, String> metaData;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
}

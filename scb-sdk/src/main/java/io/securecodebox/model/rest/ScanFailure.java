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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 23.04.18
 */
@ApiModel(value = "ScanFailure", description = "The failure result of an external scan.")
@JsonPropertyOrder(alphabetic = true)
public class ScanFailure {

    @ApiModelProperty(value = "The id of the external scanner, which provides this failure.",
            example = "5dd0840c-81ae-4fed-90b5-b3eea3d4c701", required = true)
    @JsonProperty(required = true)
    UUID scannerId;

    @ApiModelProperty(value = "Short error message why this failure happened.",
            example = "The host down.securecodebox.io is nor reachable!")
    @JsonProperty
    String errorMessage;

    @ApiModelProperty(value = "Provide more details, if there are any, why this failure happened.",
            example = "It was not possible to resolve a DNS entry!")
    @JsonProperty
    String errorDetails;

    public UUID getScannerId() {
        return scannerId;
    }

    public void setScannerId(UUID scannerId) {
        this.scannerId = scannerId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
}

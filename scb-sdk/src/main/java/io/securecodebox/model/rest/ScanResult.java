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
import io.securecodebox.model.findings.Finding;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 20.04.18
 */
@ApiModel(value = "ScanResult", description = "The result of an external scan.")
@JsonPropertyOrder(alphabetic = true)
public class ScanResult {

    @ApiModelProperty(value = "The id of the external scanner, which provides this result.",
            example = "5dd0840c-81ae-4fed-90b5-b3eea3d4c701", required = true)
    @JsonProperty(required = true)
    UUID scannerId;

    @ApiModelProperty(value = "The type of the external scanner, which provides this result.", example = "nmap",
            required = true)
    @JsonProperty(required = true)
    String scannerType;

    @ApiModelProperty(value = "The prepared findings of an external scan result.")
    @JsonProperty
    List<Finding> findings;

    @ApiModelProperty(value = "The raw findings providet by the scanner. This can be nearly everything.")
    @JsonProperty
    String rawFindings;

    public UUID getScannerId() {
        return scannerId;
    }

    public void setScannerId(UUID scannerId) {
        this.scannerId = scannerId;
    }

    public String getScannerType() {
        return scannerType;
    }

    public void setScannerType(String scannerType) {
        this.scannerType = scannerType;
    }

    public List<Finding> getFindings() {
        return findings;
    }

    public void setFindings(List<Finding> findings) {
        this.findings = findings;
    }

    public String getRawFindings() {
        return rawFindings;
    }

    public void setRawFindings(String rawFindings) {
        this.rawFindings = rawFindings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ScanResult that = (ScanResult) o;
        return Objects.equals(scannerId, that.scannerId) && Objects.equals(scannerType, that.scannerType)
                && Objects.equals(findings, that.findings) && Objects.equals(rawFindings, that.rawFindings);
    }

    @Override
    public int hashCode() {

        return Objects.hash(scannerId, scannerType, findings, rawFindings);
    }

    @Override
    public String toString() {
        return "ScanResult{" + "scannerId=" + scannerId + ", scannerType='" + scannerType + '\'' + ", findings="
                + findings + ", rawFindings='" + rawFindings + '\'' + '}';
    }
}

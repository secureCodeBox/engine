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

package io.securecodebox.engine.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.securecodebox.model.findings.Finding;

import java.util.List;
import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 20.04.18
 */
@JsonPropertyOrder(alphabetic = true)
public class ScanResult {

    @JsonProperty
    UUID scannerId;
    @JsonProperty
    String scannerType;
    @JsonProperty
    List<Finding> findings;
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
    public String toString() {
        return "ScanResult{" + "scannerId=" + scannerId + ", scannerType='" + scannerType + '\'' + ", findings="
                + findings + ", rawFindings='" + rawFindings + '\'' + '}';
    }
}

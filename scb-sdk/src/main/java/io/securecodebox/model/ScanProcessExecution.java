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

package io.securecodebox.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 08.03.18
 */
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

    @JsonProperty("spiderId")
    public abstract void setSpiderId(UUID id);

    @JsonProperty("spiderId")
    public abstract UUID getSpiderId();

    @JsonProperty("spiderType")
    public abstract void setSpiderType(String type);

    @JsonProperty("spiderType")
    public abstract String getSpiderType();

    @JsonIgnore
    public abstract boolean hasScanner();

    @JsonProperty("scannerId")
    public abstract void setScannerId(UUID id);

    @JsonProperty("scannerId")
    public abstract UUID getScannerId();

    @JsonProperty("scannerType")
    public abstract void setScannerType(String type);

    @JsonProperty("scannerType")
    public abstract String getScannerType();

    @JsonProperty("automated")
    public abstract boolean isAutomated();

    public enum DefaultFields {
        PROCESS_CONTEXT, PROCESS_SPIDER_ID, PROCESS_SPIDER_TYPE, PROCESS_SCANNER_ID, PROCESS_SCANNER_TYPE,
        PROCESS_AUTOMATED, PROCESS_FINDINGS, PROCESS_RAW_FINDINGS;
    }

}

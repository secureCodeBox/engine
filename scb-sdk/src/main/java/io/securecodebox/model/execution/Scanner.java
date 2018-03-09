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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.findings.Finding;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 09.03.18
 */
@JsonPropertyOrder({"scannerId", "scannerType"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Scanner extends ExecutionAware {
    private static final Logger LOG = LoggerFactory.getLogger(Scanner.class);

    @JsonIgnore
    ObjectMapper objectMapper;

    public Scanner(DelegateExecution execution) {
        super(execution);
        objectMapper=new ObjectMapper();
    }

    @JsonProperty("scannerId")
    public void setScannerId(UUID id) {
        execution.setVariable(ScanProcessExecution.DefaultFields.PROCESS_SCANNER_ID.name(), id.toString());
    }

    @JsonProperty("scannerId")
    public UUID getScannerId() {
        StringValue input = execution.getVariableTyped(ScanProcessExecution.DefaultFields.PROCESS_SCANNER_ID.name());
        return input != null ? UUID.fromString(input.getValue()) : null;
    }

    @JsonProperty("scannerType")
    public void setScannerType(String type) {
        execution.setVariable(ScanProcessExecution.DefaultFields.PROCESS_SCANNER_TYPE.name(), type);
    }

    @JsonProperty("scannerType")
    public String getScannerType() {
        return execution.<StringValue>getVariableTyped(ScanProcessExecution.DefaultFields.PROCESS_SCANNER_TYPE.name()).getValue();
    }

    @JsonIgnore
    public List<Finding> getFindings() {
        StringValue rawFindings = execution.getVariableTyped(
                ScanProcessExecution.DefaultFields.PROCESS_FINDINGS.name());
        if (rawFindings != null && !rawFindings.getValue().isEmpty()) {
            try {
                return objectMapper.readValue(rawFindings.getValue(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Finding.class));
            } catch (IOException e) {
                LOG.error("Cann't extract findings from process! Raw Data {}", rawFindings.getValue(), e);
            }
        }
        return new LinkedList<>();
    }

    /**
     * Appends a finding to the finding list.
     *
     * @param finding
     *
     * @throws IllegalStateException if something goes wrong writing the finding to the process
     */
    @JsonIgnore
    public synchronized void appendFinding(Finding finding) {
        List<Finding> findings = getFindings();
        findings.add(finding);
        try {
            String rawFindingString = objectMapper.writeValueAsString(findings);
            execution.setVariable(ScanProcessExecution.DefaultFields.PROCESS_FINDINGS.name(), rawFindingString);
        } catch (JsonProcessingException e) {
            LOG.error("Can't write findings to process!", e);
            throw new IllegalStateException("Can't write findings to process!", e);
        }
    }

}

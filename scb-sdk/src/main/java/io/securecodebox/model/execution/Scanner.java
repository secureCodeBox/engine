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
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.findings.Finding;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 09.03.18
 */
@JsonPropertyOrder({ "id", "type" })
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Scanner extends ExecutionAware {
    private static final Logger LOG = LoggerFactory.getLogger(Scanner.class);

    @JsonIgnore
    ObjectMapper objectMapper;

    public Scanner(DelegateExecution execution) {
        super(execution);
        objectMapper = new ObjectMapper();
    }

    @JsonProperty("id")
    public void setScannerId(UUID id) {
        execution.setVariable(DefaultFields.PROCESS_SCANNER_ID.name(), id.toString());
    }

    @JsonProperty("id")
    public UUID getScannerId() {
        StringValue input = execution.getVariableTyped(DefaultFields.PROCESS_SCANNER_ID.name());
        return input != null ? UUID.fromString(input.getValue()) : null;
    }

    @JsonProperty("type")
    public void setScannerType(String type) {
        execution.setVariable(DefaultFields.PROCESS_SCANNER_TYPE.name(), type);
    }

    @JsonProperty("type")
    public String getScannerType() {
        StringValue valueHolder = execution.getVariableTyped(DefaultFields.PROCESS_SCANNER_TYPE.name());
        return valueHolder != null ? valueHolder.getValue() : "";
    }

    @JsonIgnore
    public List<Finding> getFindings() {

        if (!execution.hasVariable(DefaultFields.PROCESS_FINDINGS.name())) {
            return new LinkedList<>();
        }

        Object findings = execution.getVariable(DefaultFields.PROCESS_FINDINGS.name());
        if (findings != null && !StringUtils.isEmpty(findings)) {
            try {
                return objectMapper.readValue((String) findings,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Finding.class));
            } catch (IOException e) {
                LOG.error("Can't extract findings from process! Raw Data {}", findings, e);
            }
        }
        return new LinkedList<>();
    }

    /**
     * Clears all {@link Finding}s in this Scanner.
     * <p>
     * After invoking this method, {@link Scanner#getFindings()} will return zero elements.
     */
    @JsonIgnore
    public void clearFindings() {
        saveFindingsToProcess(new LinkedList<>());
    }

    /**
     * Clears the raw findings in this Scanner.
     * <p>
     * After invoking this method, {@link Scanner#getRawFindings()} ()} will return an empty string.
     */
    @JsonIgnore
    public void clearRawFindings() {
        execution.setVariable(DefaultFields.PROCESS_RAW_FINDINGS.name(), null);
    }

    /**
     * This are the raw findings from a scanner. They can be in different formats.
     * <p>
     * The raw findings get not persisted in the ScanResult. But can be used to convert it to Findings.
     * <p>
     * For example:
     * - JSON
     * - XML
     * - RAW String Output
     * - LOG Output
     * - Base64
     * - ...
     *
     * @return some String representing the findings in a raw format.
     */
    @JsonIgnore
    public String getRawFindings() {
        Object rawFindings = execution.getVariable(DefaultFields.PROCESS_RAW_FINDINGS.name());
        return rawFindings != null ? (String) rawFindings : "";
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
        saveFindingsToProcess(findings);
    }

    private void saveFindingsToProcess(List<Finding> findings) {
        try {
            String rawFindingString = objectMapper.writeValueAsString(findings);
            execution.setVariable(DefaultFields.PROCESS_FINDINGS.name(), rawFindingString);
        } catch (JsonProcessingException e) {
            LOG.error("Can't write findings to process!", e);
            throw new IllegalStateException("Can't write findings to process!", e);
        }
    }

}

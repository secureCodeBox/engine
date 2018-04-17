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

package io.securecodebox.engine.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Scanner;
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
public class ScannerBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ScannerBuilder.class);

    private ObjectMapper objectMapper;

    private UUID id;
    private String type;
    private String rawFindings;
    private List<Finding> findings;

    private ScannerBuilder() {
        objectMapper = new ObjectMapper();
    }

    public static ScannerBuilder init() {
        return new ScannerBuilder();
    }

    public Scanner buildByExecution(DelegateExecution execution) {
        initScannerId(execution);
        initScannerType(execution);
        initFindings(execution);
        initRawFindings(execution);
        validate();
        return new Scanner(id, type, rawFindings, findings);
    }

    public Scanner build() {
        validate();
        return new Scanner(id, type, rawFindings, findings);
    }

    public ScannerBuilder id(UUID id) {
        this.id = id;
        return this;
    }

    public ScannerBuilder type(String type) {
        this.type = type;
        return this;
    }

    public ScannerBuilder findings(List<Finding> findings) {
        this.findings = findings;
        return this;
    }

    public ScannerBuilder rawFindings(String rawFindings) {
        this.rawFindings = rawFindings;
        return this;
    }

    private void initRawFindings(DelegateExecution execution) {
        Object findings = execution.getVariable(DefaultFields.PROCESS_RAW_FINDINGS.name());
        rawFindings = findings instanceof String ? (String) findings : "";
    }

    private void initFindings(DelegateExecution execution) {
        if (!execution.hasVariable(DefaultFields.PROCESS_FINDINGS.name())) {
            findings = new LinkedList<>();
        }

        Object results = execution.getVariable(DefaultFields.PROCESS_FINDINGS.name());
        if (results != null && !StringUtils.isEmpty(results)) {
            try {
                findings = objectMapper.readValue((String) results,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Finding.class));
                return;
            } catch (IOException e) {
                LOG.error("Can't extract findings from process! Raw Data {}", results, e);
            }
        }
        findings = new LinkedList<>();
    }

    private void initScannerId(DelegateExecution execution) {
        StringValue input = execution.getVariableTyped(DefaultFields.PROCESS_SCANNER_ID.name());
        id = input != null ? UUID.fromString(input.getValue()) : null;
    }

    private void initScannerType(DelegateExecution execution) {
        StringValue scannerType = execution.getVariableTyped(DefaultFields.PROCESS_SCANNER_TYPE.name());
        type = scannerType != null ? scannerType.getValue() : "";
    }

    private void validate() {
        if (id == null) {
            throw new IllegalStateException("Scanner UUID should never be null!");
        }
    }

}

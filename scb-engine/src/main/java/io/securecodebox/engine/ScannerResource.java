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

package io.securecodebox.engine;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 16.04.18
 */
@RestController
@RequestMapping(value = "/box/jobs")
public class ScannerResource {
    private static final Logger LOG = LoggerFactory.getLogger(ScannerResource.class);
    public static final int LOCK_DURATION_MS = 86400000;

    @Autowired
    ProcessEngine engine;

    @Autowired
    ObjectMapper objectMapper;

    @ApiOperation(value = "Lock a scan job for the given topic",
            notes = "Returns a scan job for the given topic / capability, if there is one.")

    @ApiResponses(value = { @ApiResponse(code = 201, message = "Successful retrieval of the scan Job",
            response = ScanConfiguration.class),
            @ApiResponse(code = 204, message = "No scanjob available", response = void.class),
            @ApiResponse(code = 400, message = "Incomplete or inconsistent Request"),

            @ApiResponse(code = 500, message = "Unknown technical error occurred.") })

    @RequestMapping(method = RequestMethod.POST, value = "/lock/{topic}/{scannerId}")
    public ResponseEntity<ScanConfiguration> lockJob(
            @ApiParam(defaultValue = "nmap_portscan") @PathVariable String topic,
            @ApiParam(value = "UUID of the job.", required = true, type = "UUID",
                    defaultValue = "29bf7fd3-8512-4d73-a28f-608e493cd726") @PathVariable UUID scannerId) {
        ExternalTaskQueryBuilder externalTaskQueryBuilder = engine.getExternalTaskService()
                .fetchAndLock(1, scannerId.toString());
        externalTaskQueryBuilder.topic(topic, LOCK_DURATION_MS);

        LockedExternalTask result = Iterables.getFirst(externalTaskQueryBuilder.execute(), null);
        if (result != null) {

            ScanConfiguration config = new ScanConfiguration();
            config.processId = UUID.fromString(result.getExecutionId());
            config.targets = getVariableListFromJsonField(result, DefaultFields.PROCESS_TARGETS, Target.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(config);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    private <T> List<T> getVariableListFromJsonField(LockedExternalTask result, Enum<?> field, Class<T> innerClass) {
        synchronized (field) {
            Object processFindings = engine.getRuntimeService().getVariable(result.getExecutionId(), field.name());
            ;
            if (!(processFindings instanceof String)) {
                LOG.error("String field {} is not instance of string. Value {}", field, processFindings);
                throw new IllegalStateException("String field is not instance of string!");
            }

            try {
                return objectMapper.readValue((String) processFindings,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, innerClass));
            } catch (IOException e) {
                LOG.error("Can't extract json field {} from process! Raw Data {}", field.name(), processFindings, e);
                return new LinkedList<>();
            }
        }
    }

    @ApiOperation(value = "Send a scan result for the previously locked job.")

    @ApiResponses(value = { @ApiResponse(code = 201, message = "Successful retrival of the result."),
            @ApiResponse(code = 400, message = "Incomplete or inconsistent Request"),
            @ApiResponse(code = 500, message = "Unknown technical error occurred.") })

    @RequestMapping(method = RequestMethod.POST, value = "{id}/result")
    public ResponseEntity completeJob(@ApiParam(value = "UUID of the job.", required = true, type = "UUID",
            defaultValue = "29bf7fd3-8512-4d73-a28f-608e493cd726") @PathVariable UUID id,
            @RequestBody ScanResult result) {

        LOG.info("Recived scan result {}", result);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @JsonPropertyOrder(alphabetic = true)
    private class ScanConfiguration {

        @JsonProperty
        UUID processId;
        @JsonProperty
        List<Target> targets;

        @Override
        public String toString() {
            return "ScanConfiguration{" + "processId=" + processId + ", targets=" + targets + '}';
        }
    }

    @JsonPropertyOrder(alphabetic = true)
    private class ScanResult {

        @JsonProperty
        UUID scannerId;
        @JsonProperty
        String scannerType;
        @JsonProperty
        List<Finding> findings;
        @JsonProperty
        String rawFindings;

        @Override
        public String toString() {
            return "ScanResult{" + "scannerId=" + scannerId + ", scannerType='" + scannerType + '\'' + ", findings="
                    + findings + ", rawFindings='" + rawFindings + '\'' + '}';
        }
    }
}

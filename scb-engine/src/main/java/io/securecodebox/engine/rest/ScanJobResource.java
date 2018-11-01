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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.rest.ScanConfiguration;
import io.securecodebox.model.rest.ScanFailure;
import io.securecodebox.model.rest.ScanResult;
import io.securecodebox.scanprocess.ProcessVariableHelper;
import io.swagger.annotations.*;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * API / Endpoint for scan jobs.
 *
 * @author Rüdiger Heins - iteratec GmbH
 * @since 16.04.18
 */
@Api(description = "Scan Jobs Resource", consumes = "application/json", produces = "application/json")
@RestController
@RequestMapping(value = "/box/jobs")
public class ScanJobResource {
    private static final Logger LOG = LoggerFactory.getLogger(ScanJobResource.class);
    public static final int LOCK_DURATION_MS = 86400000;

    @Autowired
    ProcessEngine engine;

    @Autowired
    ObjectMapper objectMapper;

    @ApiOperation(value = "Lock a scan job for the given topic",
            notes = "Returns a scan job for the given topic / capability, if there is one.",
            authorizations = {
                @Authorization(value="basicAuth")
            })

    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful retrieval of the scan job",
            response = ScanConfiguration.class),
            @ApiResponse(code = 204, message = "No scan job available", response = void.class),
            @ApiResponse(code = 400, message = "Incomplete or inconsistent Request"),
            @ApiResponse(code = 401, message = "Unauthenticated", response = void.class),
            @ApiResponse(code = 500, message = "Unknown technical error occurred.") })

    @RequestMapping(method = RequestMethod.POST, value = "/lock/{topic:[a-zA-Z0-9_\\-]*}/{scannerId}")
    public ResponseEntity<ScanConfiguration> lockJob(
            @ApiParam(defaultValue = "nmap_portscan", example = "nmap_portscan",
                    value = "Topic name for the Process, be shure only to use: [A-Za-z0-9-_]",
                    required = true) @PathVariable String topic,
            @ApiParam(value = "UUID of the job.", required = true, type = "UUID",
                    defaultValue = "29bf7fd3-8512-4d73-a28f-608e493cd726",
                    example = "29bf7fd3-8512-4d73-a28f-608e493cd726") @PathVariable UUID scannerId) {
        ExternalTaskQueryBuilder externalTaskQueryBuilder = engine.getExternalTaskService()
                .fetchAndLock(1, scannerId.toString());
        externalTaskQueryBuilder.topic(topic, LOCK_DURATION_MS);

        LockedExternalTask result = Iterables.getFirst(externalTaskQueryBuilder.execute(), null);
        if (result != null) {

            ScanConfiguration config = new ScanConfiguration();
            config.setJobId(UUID.fromString(result.getId()));
            config.setTargets(getVariableListFromJsonField(result, DefaultFields.PROCESS_TARGETS, Target.class));
            return ResponseEntity.ok(config);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @ApiOperation(value = "Send a scan result for the previously locked job.",
            authorizations = {
                    @Authorization(value="basicAuth")
            })
    @ApiResponses(
            value = { @ApiResponse(code = 200, message = "Successful delivery of the result.", response = void.class),
                    @ApiResponse(code = 400, message = "Incomplete or inconsistent Request"),
                    @ApiResponse(code = 404, message = "Unable to find jobId"),
                    @ApiResponse(code = 500, message = "Unknown technical error occurred.") })

    @RequestMapping(method = RequestMethod.POST, value = "{id}/result")
    public ResponseEntity completeJob(@ApiParam(value = "UUID of the job.", required = true, type = "UUID",
            defaultValue = "29bf7fd3-8512-4d73-a28f-608e493cd726",
            example = "29bf7fd3-8512-4d73-a28f-608e493cd726") @PathVariable UUID id,
            @Valid @RequestBody ScanResult result) {

        LOG.debug("Received scan result {}", result);

        Map<String, Object> variables = new HashMap<>();
        variables.put(DefaultFields.PROCESS_SCANNER_ID.name(), result.getScannerId().toString());
        variables.put(DefaultFields.PROCESS_SCANNER_TYPE.name(), result.getScannerType());
        synchronized (DefaultFields.PROCESS_FINDINGS) {
            variables.put(DefaultFields.PROCESS_FINDINGS.name(),
                    ProcessVariableHelper.generateObjectValue(result.getFindings()));
        }
        synchronized (DefaultFields.PROCESS_RAW_FINDINGS) {
            variables.put(DefaultFields.PROCESS_RAW_FINDINGS.name(),
                    ProcessVariableHelper.generateObjectValue(result.getRawFindings()));
        }

        try {
            engine.getExternalTaskService().complete(id.toString(), result.getScannerId().toString(), variables);
        } catch (NotFoundException e) {
            LOG.info("Can not find taskId {}", id, e);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Send a scan failure for the previously locked job.",
            authorizations = {
                    @Authorization(value="basicAuth")
            })
    @ApiResponses(
            value = { @ApiResponse(code = 200, message = "Successful delivery of the failure.", response = void.class),
                    @ApiResponse(code = 400, message = "Incomplete or inconsistent Request"),
                    @ApiResponse(code = 404, message = "Unable to find jobId"),
                    @ApiResponse(code = 500, message = "Unknown technical error occurred.") })

    @RequestMapping(method = RequestMethod.POST, value = "{id}/failure")
    public ResponseEntity failJob(@ApiParam(value = "UUID of the job.", required = true, type = "UUID",
            defaultValue = "29bf7fd3-8512-4d73-a28f-608e493cd726",
            example = "29bf7fd3-8512-4d73-a28f-608e493cd726") @PathVariable UUID id,
            @Valid @RequestBody ScanFailure result) {

        int retriesLeft = 0;

        LOG.debug("Recived scan failure {}", result);

        ExternalTask externalTask = engine.getExternalTaskService()
                .createExternalTaskQuery()
                .externalTaskId(id.toString())
                .withRetriesLeft()
                .singleResult();

        if (externalTask == null) {
            LOG.info("Can not find taskId {}", id);
            return ResponseEntity.notFound().build();
        } else if (externalTask.getRetries() != null && externalTask.getRetries() > 0) {
            retriesLeft = externalTask.getRetries() - 1;
        }

        engine.getExternalTaskService()
                .handleFailure(id.toString(), result.getScannerId().toString(), result.getErrorMessage(),
                        result.getErrorDetails(), retriesLeft, 1000);
        return ResponseEntity.ok().build();
    }

    private <T> List<T> getVariableListFromJsonField(LockedExternalTask result, Enum<?> field, Class<T> innerClass) {
        synchronized (field) {
            Object processFindings = engine.getRuntimeService().getVariable(result.getExecutionId(), field.name());

            if (!(processFindings instanceof String)) {
                LOG.error("String field {} is not instance of string. Value {}", field, processFindings);
                throw new IllegalStateException("String field is not instance of string!");
            }
            return ProcessVariableHelper.readListFromValue((String) processFindings, innerClass);
        }
    }

}

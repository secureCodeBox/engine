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
import com.google.common.collect.Iterables;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 16.04.18
 */
@RestController
@RequestMapping(value = "/box/scanners")
public class ScannerResource {

    @Autowired
    ProcessEngine engine;

    @ApiOperation(value = "Lock a scan job for the given topic",
            notes = "Returns a scan job for the given topic / capability, if there is one.")

    @ApiResponses(value = { @ApiResponse(code = 201, message = "Successful retrieval of the scan Job",
            response = ScanConfiguration.class), @ApiResponse(code = 204, message = "No scanjob available"),
            @ApiResponse(code = 400, message = "Incomplete or inconsistent Request"),

            @ApiResponse(code = 500, message = "Unknown technical error occurred.") })

    @RequestMapping(method = RequestMethod.POST, value = "/{scannerId}/jobs/lock/{topic}")
    public ResponseEntity<ScanConfiguration> lockJob(@PathVariable String topic, @PathVariable UUID workerId) {
        ExternalTaskQueryBuilder externalTaskQueryBuilder = engine.getExternalTaskService()
                .fetchAndLock(1, workerId.toString());
        externalTaskQueryBuilder.topic(topic, Long.MAX_VALUE);

        LockedExternalTask result = Iterables.getFirst(externalTaskQueryBuilder.execute(), null);
        if (result != null) {
            ScanConfiguration config = new ScanConfiguration();
            config.processId = UUID.fromString(result.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(config);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @ApiOperation(value = "Send a scan result for the previously locked job.")

    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful return of the scan results.",
            response = ScanConfiguration.class),
            @ApiResponse(code = 400, message = "Incomplete or inconsistent Request"),
            @ApiResponse(code = 500, message = "Unknown technical error occurred.") })

    @RequestMapping(method = RequestMethod.POST, value = "/jobs/{id}")
    public ResponseEntity completeJob( @ApiParam(value = "UUID of the job.", required = true) @PathVariable UUID id, @RequestBody ScanResult result) {

        return ResponseEntity.ok().build();
    }

    @JsonPropertyOrder(alphabetic = true)
    private class ScanConfiguration {

        @JsonProperty
        UUID processId;
        @JsonProperty
        Map<String, Object> configs;
        @JsonProperty
        List<Target> targets;

    }

    @JsonPropertyOrder(alphabetic = true)
    private class ScanResult {

        @JsonProperty
        List<Finding> findings;
        @JsonProperty
        String rawFindings;

    }
}

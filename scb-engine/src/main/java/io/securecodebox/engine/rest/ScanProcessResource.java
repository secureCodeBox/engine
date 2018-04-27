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
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * API / Endpoint for scan processes (camunda processes).
 *
 * @author Rüdiger Heins - iteratec GmbH
 * @since 23.04.18
 */

@Api(description = "Scan Process Resource", produces = "application/json", consumes = "application/json")
@RestController
@RequestMapping(value = "/box/processes")
public class ScanProcessResource {
    private static final Logger LOG = LoggerFactory.getLogger(ScanProcessResource.class);

    @Autowired
    ProcessEngine engine;

    @ApiOperation(value = "Creates a new scan process. (API STUB NOT IMPLEMENTED!)")
    @ApiResponses(
            value = { @ApiResponse(code = 201, message = "Successful delivery of the failure.", response = void.class),
                    @ApiResponse(code = 300,
                            message = "For some reason multiple processes could be adressed by the given processKey.",
                            response = void.class),
                    @ApiResponse(code = 400, message = "Incomplete or inconsistent Request"),
                    @ApiResponse(code = 500, message = "Unknown technical error occurred.") })

    @RequestMapping(method = RequestMethod.PUT, value = "/{processKey}")
    public ResponseEntity getProcesses(@PathVariable String processKey, @RequestBody List<Target> targets) {

        long processCount = engine.getRepositoryService()
                .createProcessDefinitionQuery()
                .active()
                .processDefinitionKey(processKey)
                .latestVersion()
                .count();

        Map<String, Object> values = new HashMap<>();
        values.put(DefaultFields.PROCESS_AUTOMATED.name(), true);

        if (processCount == 1) {
            LOG.debug("Starting process for id {}", processKey);
            engine.getRuntimeService().startProcessInstanceByKey(processKey, values);
            return ResponseEntity.ok().build();
        } else {
            LOG.error("Found more than one result for processKey: {}", processKey);
            return ResponseEntity.status(HttpStatus.MULTIPLE_CHOICES).build();
        }

    }

    @ApiOperation(value = "Returns all possible processes. (API STUB NOT IMPLEMENTED!)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful delivery of the failure.", response = Result.class,
                    responseContainer = "List"),
            @ApiResponse(code = 400, message = "Incomplete or inconsistent Request"),
            @ApiResponse(code = 500, message = "Unknown technical error occurred.") })

    @RequestMapping(method = RequestMethod.GET, value = "/")
    public ResponseEntity<List<Result>> getProcesses() {

        List<ProcessDefinition> list = engine.getRepositoryService()
                .createProcessDefinitionQuery()
                .active()
                .latestVersion()
                .list();

        List<Result> results = new LinkedList<>();
        list.forEach(element -> results.add(new Result(element.getId(), element.getName(), element.getKey())));

        return ResponseEntity.ok(results);
    }

    @JsonPropertyOrder(alphabetic = true)
    private class Result {
        @JsonProperty
        String processId;
        @JsonProperty
        String processName;
        @JsonProperty
        String processKey;

        public Result(String processId, String processName, String processKey) {
            this.processId = processId;
            this.processKey = processKey;
            this.processName = processName;
        }
    }

}

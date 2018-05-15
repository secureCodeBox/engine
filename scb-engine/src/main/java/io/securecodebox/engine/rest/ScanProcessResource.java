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
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.rest.Process;
import io.securecodebox.scanprocess.ProcessVariableHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
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

import javax.validation.Valid;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Autowired
    ObjectMapper objectMapper;

    @ApiOperation(value = "Creates a new scan process.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created a new process returns the process id.",
                    response = UUID.class), @ApiResponse(code = 300,
            message = "For some reason multiple processes could be adressed by the given processKey.",
            response = void.class), @ApiResponse(code = 400, message = "Incomplete or inconsistent Request"),
            @ApiResponse(code = 500, message = "Unknown technical error occurred.") })

    @RequestMapping(method = RequestMethod.PUT, value = "/{processKey}")
    public ResponseEntity<UUID> getProcesses(
            @ApiParam(value = "The key of the process to be started. See GET /box/processes.", example = "nmap-process",
                    required = true) @PathVariable String processKey, @Valid @RequestBody List<Target> targets) {

        long processCount = engine.getRepositoryService()
                .createProcessDefinitionQuery()
                .active()
                .processDefinitionKey(processKey)
                .latestVersion()
                .count();

        Map<String, Object> values = new HashMap<>();
        values.put(DefaultFields.PROCESS_AUTOMATED.name(), true);
        values.put(DefaultFields.PROCESS_TARGETS.name(), ProcessVariableHelper.generateObjectValue(targets));

        if (processCount == 1) {
            LOG.debug("Starting process for id {}", processKey);
            ProcessInstance instance = engine.getRuntimeService().startProcessInstanceByKey(processKey, values);
            return ResponseEntity.ok(UUID.fromString(instance.getProcessInstanceId()));
        } else {
            LOG.error("Found more than one result for processKey: {}", processKey);
            return ResponseEntity.status(HttpStatus.MULTIPLE_CHOICES).build();
        }

    }

    @ApiOperation(value = "Returns all possible processes.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns a list of all available processes.", response = Process.class,
                    responseContainer = "List"),
            @ApiResponse(code = 400, message = "Incomplete or inconsistent Request"),
            @ApiResponse(code = 500, message = "Unknown technical error occurred.") })

    @RequestMapping(method = RequestMethod.GET, value = "/")
    public ResponseEntity<List<Process>> getProcesses() {

        List<ProcessDefinition> allProcesses = engine.getRepositoryService()
                .createProcessDefinitionQuery()
                .active()
                .latestVersion()
                .list();

        List<Process> results = new LinkedList<>();
        allProcesses.forEach(element -> results.add(new Process(element.getId(), element.getName(), element.getKey())));

        return ResponseEntity.ok(results);
    }

}

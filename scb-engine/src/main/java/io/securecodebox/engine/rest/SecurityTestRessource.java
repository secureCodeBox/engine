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
import io.securecodebox.model.rest.SecurityTest;
import io.securecodebox.scanprocess.ProcessVariableHelper;
import io.swagger.annotations.*;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@Api(value = "security-tests",
        description = "Starting new security test instances.",
        produces = "application/json",
        consumes = "application/json")
@RestController
@RequestMapping(value = "/box/security-tests")
public class SecurityTestRessource {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityTestRessource.class);

    @Autowired
    ProcessEngine engine;

    @Autowired
    ObjectMapper objectMapper;

    @ApiOperation(value = "Starts new security tests.",
                    notes = "Starts new security tests, based on a given list of security test configurations.",
                    authorizations = {
                            @Authorization(value="basicAuth")
                    }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Successful created a new process returns the process id.",
                    response = UUID.class,
                    responseContainer = "List"
            ),
            @ApiResponse(
                    code = 300,
                    message = "For some reason multiple processes could be addressed by the given processKey.",
                    response = void.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Incomplete or inconsistent Request."
            ),
            @ApiResponse(
                    code = 404,
                    message = "Could not find definition for specified security test.",
                    response = void.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "Unknown technical error occurred."
            )
    })
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<List<UUID>> startSecurityTests(@Valid @RequestBody List<SecurityTest> securityTests) {

        for (SecurityTest securityTest : securityTests) {
            long processCount = engine.getRepositoryService()
                    .createProcessDefinitionQuery()
                    .active()
                    .processDefinitionKey(securityTest.getProcessDefinitionKey())
                    .latestVersion()
                    .count();

            if (processCount == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (processCount > 1) {
                return ResponseEntity.status(HttpStatus.MULTIPLE_CHOICES).build();
            }
        }

        List<UUID> processInstances = new LinkedList<>();

        for (SecurityTest securityTest : securityTests) {
            Map<String, Object> values = new HashMap<>();

            List<Target> targets = new LinkedList<>();
            targets.add(securityTest.getTarget());

            values.put(DefaultFields.PROCESS_AUTOMATED.name(), true);
            values.put(DefaultFields.PROCESS_CONTEXT.name(), securityTest.getContext());
            values.put(DefaultFields.PROCESS_TARGETS.name(), ProcessVariableHelper.generateObjectValue(targets));

            ProcessInstance instance = engine.getRuntimeService().startProcessInstanceByKey(securityTest.getProcessDefinitionKey(), values);
            processInstances.add(UUID.fromString(instance.getProcessInstanceId()));
        }

        return ResponseEntity.ok(processInstances);
    }
}

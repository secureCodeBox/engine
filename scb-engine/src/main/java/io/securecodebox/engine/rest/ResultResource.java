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
import io.securecodebox.model.rest.Result;
import io.swagger.annotations.*;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Api(value = "results",
        description = "Gets the results of security-tests.",
        produces = "application/json",
        consumes = "application/json")
@RestController
@RequestMapping(value = "/box/results")
public class ResultResource {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityTestResource.class);

    @Autowired
    ProcessEngine engine;

    @Autowired
    ObjectMapper objectMapper;

    @ApiOperation(value = "Get the results from security-tests.",
            notes = "Gets one result of a single security-test.",
            authorizations = {
                    @Authorization(value="basicAuth")
            }
    )
    @ApiResponses(value = {
        @ApiResponse(
            code = 200,
            message = "Successful fetched a result for a security-test.",
            response = UUID.class
        ),
        @ApiResponse(
            code = 400,
            message = "Incomplete or inconsistent Request",
            response = void.class
        ),
        @ApiResponse(
            code = 404,
            message = "No result found for the security-test id.",
            response = void.class
        ),
        @ApiResponse(
            code = 500,
            message = "Unknown technical error occurred.",
            response = void.class
        )
    })
    @RequestMapping(method = RequestMethod.PUT, value = "/{securityTestId}")
    public ResponseEntity<Result> getResult(
            @Valid @PathVariable @ApiParam(value = "UUID of the security-test for which the result should be fetched.", required = true) UUID securityTestId
    ) {

        List<HistoricVariableInstance> variables = engine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .variableName(DefaultFields.PROCESS_RESULT.name())
                .processInstanceId(securityTestId.toString())
                .list();

        if(variables.size() != 1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String resultsDoubilySerialized = (String) variables.get(0).getValue();

        try {
            String resultString = objectMapper.readValue(resultsDoubilySerialized, String.class);
            Result result = objectMapper.readValue(resultString, Result.class);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            LOG.error("Could not deserialize security-test result. {}", e);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

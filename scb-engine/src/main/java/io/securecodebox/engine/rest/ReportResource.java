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
import io.securecodebox.model.rest.Report;
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

@Api(value = "reports",
        description = "Gets the reports of security-tests.",
        produces = "application/json",
        consumes = "application/json")
@RestController
@RequestMapping(value = "/box/reports")
public class ReportResource {

    private static final Logger LOG = LoggerFactory.getLogger(ReportResource.class);

    @Autowired
    ProcessEngine engine;

    @Autowired
    ObjectMapper objectMapper;

    @ApiOperation(value = "Get the report of security-tests.",
            notes = "Gets one report of a single security-test.",
            authorizations = {
                    @Authorization(value="basicAuth")
            }
    )
    @ApiResponses(value = {
        @ApiResponse(
            code = 200,
            message = "Successful fetched a report for a security-test.",
            response = Report.class
        ),
        @ApiResponse(
            code = 400,
            message = "Incomplete or inconsistent Request",
            response = void.class
        ),
        @ApiResponse(
            code = 401,
            message = "Unauthenticated",
            response = void.class
        ),
        @ApiResponse(
            code = 404,
            message = "No report found for the security-test id.",
            response = void.class
        ),
        @ApiResponse(
            code = 500,
            message = "Unknown technical error occurred.",
            response = void.class
        )
    })
    @RequestMapping(method = RequestMethod.GET, value = "/{securityTestId}")
    public ResponseEntity<Report> getReport(
            @Valid @PathVariable @ApiParam(value = "UUID of the security-test for which the report should be fetched.", required = true) UUID securityTestId
    ) {

        List<HistoricVariableInstance> variables = engine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .variableName(DefaultFields.PROCESS_REPORT.name())
                .processInstanceId(securityTestId.toString())
                .list();

        if(variables.size() != 1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String reportDoubilySerialized = (String) variables.get(0).getValue();

        try {
            String reportString = objectMapper.readValue(reportDoubilySerialized, String.class);
            Report report = objectMapper.readValue(reportString, Report.class);
            return ResponseEntity.ok(report);
        } catch (IOException e) {
            LOG.error("Could not deserialize security-test report. {}", e);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

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

import io.securecodebox.engine.service.ReportService;
import io.securecodebox.model.rest.Report;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
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
    private ReportService reportService;

    @ApiOperation(value = "Get the report of security-tests.",
            notes = "Gets one report of a single security-test.",
            authorizations = {
                    @Authorization(value = "basicAuth")
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
        try {
            Report report = reportService.getReport(securityTestId);
            return ResponseEntity.ok(report);
        } catch (ReportService.ReportNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            LOG.error("Could not deserialize report. {}", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

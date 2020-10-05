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

import io.securecodebox.engine.auth.InsufficientAuthorizationException;
import io.securecodebox.engine.model.PermissionType;
import io.securecodebox.engine.model.ResourceType;
import io.securecodebox.engine.service.AuthService;
import io.securecodebox.engine.service.SecurityTestService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Api(value = "securityTestDefinitions",
        description = "Retrieve information about the available securityTest definitions.",
        produces = "application/json",
        consumes = "application/json")
@RestController
@RequestMapping(value = "/box/securityTests/definitions")
public class SecurityTestDefinitionResource {

    @Autowired
    SecurityTestService securityTestService;

    @Autowired
    AuthService authService;

    @ApiOperation(value = "Lists all available securityTest definitions.",
            authorizations = {
                    @Authorization(value="basicAuth")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "Successfully listed all available securityTest definitions.",
                    response = UUID.class,
                    responseContainer = "List"
            ),
            @ApiResponse(
                    code = 401,
                    message = "Unauthenticated",
                    response = void.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "Unauthorized, the user is missing the required rights to perform this action.",
                    response = void.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "Unknown technical error occurred."
            )
    })
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<String>> getSecurityTestDefinitions(){
        try {
            authService.checkAuthorizedFor(ResourceType.SECURITY_TEST_DEFINITION, PermissionType.READ);
        }catch (InsufficientAuthorizationException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<String> securityTests = securityTestService.getAvailableSecurityTestDefinitionNames();

        return ResponseEntity.ok(securityTests);
    }

    @ApiOperation(
            value = "Count the active securityTests belonging to a fixed definition",
            notes = "Only those security test instances are included in the counting which the requesting user is " +
                    "allowed to see. " +
                    "In the case that the there is no security test definition (process definition) with the name " +
                    "that is provided as path parameter, the count is defined as zero.",
            response = long.class,
            authorizations = {
                    @Authorization(value = "basicAuth")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "Successful fetched the number of active security tests with the specified definition " +
                            "name",
                    response = long.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Unauthenticated",
                    response = void.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "Unauthorized, the user is missing the required rights to perform this action.",
                    response = void.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "Unknown technical error occurred."
            )
    })
    @RequestMapping(method = RequestMethod.GET, value = "/{testDefinitionName}/countActiveTests")
    public ResponseEntity<Long> getCountOfActiveSecurityTestsFilteredByDefinitionName(
            @PathVariable
            @ApiParam(
                    value = "The number of active security test instances for which the definition has this name " +
                            "shall be counted.",
                    required = true
            )
                    String testDefinitionName
    ) {
        try {
            authService.checkAuthorizedFor(ResourceType.SECURITY_TEST, PermissionType.READ);
        } catch (InsufficientAuthorizationException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        long countOfActiveTestsFilteredByDefinitionName = securityTestService.countActiveTestsFilteredByDefinitionName(
                testDefinitionName
        );

        return ResponseEntity.ok(countOfActiveTestsFilteredByDefinitionName);
    }
}

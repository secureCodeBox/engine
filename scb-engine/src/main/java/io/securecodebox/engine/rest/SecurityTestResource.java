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
import io.securecodebox.engine.model.PermissionType;
import io.securecodebox.engine.model.ResourceType;
import io.securecodebox.engine.service.AuthService;
import io.securecodebox.engine.service.SecurityTestService;
import io.securecodebox.model.securitytest.SecurityTest;
import io.securecodebox.model.securitytest.SecurityTestConfiguration;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.*;

@Api(value = "securityTests",
        description = "Manage securityTests.",
        produces = "application/json",
        consumes = "application/json")
@RestController
@RequestMapping(value = "/box/securityTests")
public class SecurityTestResource {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityTestResource.class);

    @Autowired
    AuthService authService;

    @Autowired
    SecurityTestService securityTestService;

    @Autowired
    ObjectMapper objectMapper;

    @ApiOperation(value = "Starts new securityTests.",
            notes = "Starts new securityTests, based on a given list of securityTest configurations.",
            authorizations = {
                    @Authorization(value = "basicAuth")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Successful created a new securityTest returns the process id.",
                    response = UUID.class,
                    responseContainer = "List"
            ),
            @ApiResponse(
                    code = 300,
                    message = "For some reason multiple securityTest definitions could be addressed by the given securityTest name.",
                    response = void.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Incomplete or inconsistent Request."
            ),
            @ApiResponse(
                    code = 401,
                    message = "Unauthenticated",
                    response = void.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "Could not find definition for specified securityTest.",
                    response = void.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "Unknown technical error occurred."
            )
    })
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<List<UUID>> startSecurityTests(
            @Valid
            @RequestBody
            @ApiParam(
                    value = "A list with all securityTest which should be performed.",
                    required = true
            )
            List<SecurityTestConfiguration> securityTests
    ) {
        try {
            for (SecurityTestConfiguration securityTest : securityTests) {
                authService.isAuthorizedFor(
                        securityTest.getProcessDefinitionKey(),
                        ResourceType.SECURITY_TEST_DEFINITION,
                        PermissionType.CREATE_INSTANCE
                );
            }
        } catch (InsufficientAuthenticationException e){
            return ResponseEntity.status(401).build();
        }

        for (SecurityTestConfiguration securityTest : securityTests) {
            try {
                this.securityTestService.checkSecurityTestDefinitionExistence(securityTest.getProcessDefinitionKey());
            } catch (SecurityTestService.NonExistentSecurityTestDefinitionException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } catch (SecurityTestService.DuplicateSecurityTestDefinitionForKeyException e) {
                return ResponseEntity.status(HttpStatus.MULTIPLE_CHOICES).build();
            }
        }

        List<UUID> processInstances = new LinkedList<>();

        for (SecurityTestConfiguration securityTest : securityTests) {
            processInstances.add(securityTestService.startSecurityTest(securityTest));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(processInstances);
    }

    @ApiOperation(value = "Returns the state of a securityTests.",
            notes = "Currently only supports finished securityTests.",
            authorizations = {
                    @Authorization(value = "basicAuth")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "Successful fetched the complete securityTest.",
                    response = SecurityTest.class
            ),
            @ApiResponse(
                    code = 206,
                    message = "Partial result as the SecurityTest hasn't finished yet.",
                    response = SecurityTest.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Incomplete or inconsistent Request."
            ),
            @ApiResponse(
                    code = 401,
                    message = "Unauthenticated",
                    response = void.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "Could not find definition for specified securityTest.",
                    response = void.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "Unknown technical error occurred."
            )
    })
    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ResponseEntity<SecurityTest> getSecurityTest(
            @Valid @PathVariable @ApiParam(value = "UUID of the security-test for which the report should be fetched.", required = true) UUID id
    ) {
        try {
            authService.isAuthorizedFor(
                    id.toString(),
                    ResourceType.SECURITY_TEST,
                    PermissionType.READ
            );
        } catch (InsufficientAuthenticationException e){
            return ResponseEntity.status(401).build();
        }

        try {
            SecurityTest securityTest = securityTestService.getCompletedSecurityTest(id);

            if (securityTest.isFinished()) {
                return ResponseEntity.status(HttpStatus.OK).body(securityTest);
            }
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(securityTest);

        } catch (SecurityTestService.SecurityTestNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (SecurityTestService.SecurityTestErroredException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

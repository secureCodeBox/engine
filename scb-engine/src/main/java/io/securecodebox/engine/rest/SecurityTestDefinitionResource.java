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

import io.securecodebox.engine.model.PermissionType;
import io.securecodebox.engine.model.ResourceType;
import io.securecodebox.engine.service.AuthService;
import io.securecodebox.engine.service.SecurityTestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Api(value = "securityTestDefinitions",
        description = "Lists available definitions of securityTests.",
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
        }catch (InsufficientAuthenticationException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<String> securityTests = securityTestService.getAvailableSecurityTestDefinitionNames();

        return ResponseEntity.ok(securityTests);
    }
}

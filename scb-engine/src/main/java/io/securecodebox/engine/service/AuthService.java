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
package io.securecodebox.engine.service;

import io.securecodebox.engine.model.PermissionType;
import io.securecodebox.engine.model.ResourceType;
import org.camunda.bpm.engine.identity.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.camunda.bpm.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthService {
    private static String AUTH_DISABLED_TYPE = "none";
    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    ProcessEngine engine;

    @Value("${securecodebox.rest.auth}")
    private String authType;

    public void isAuthorizedFor(String resourceId, ResourceType resource, PermissionType permission) throws InsufficientAuthenticationException{
        if(AUTH_DISABLED_TYPE.equals(authType)){
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null) {
            throw new InsufficientAuthenticationException("No authentication provided.");
        }

        List<String> groups = engine
                .getIdentityService()
                .createGroupQuery()
                .groupMember(authentication.getName())
                .list()
                .stream()
                .map(Group::getId)
                .collect(Collectors.toList());

        boolean isAuthorized = false;
        if(resourceId == null){
            isAuthorized = engine.getAuthorizationService().isUserAuthorized(
                    authentication.getName(),
                    groups,
                    permission.getCamundaPermission(),
                    resource.getCamundaResource()
            );
        } else {
            isAuthorized = engine.getAuthorizationService().isUserAuthorized(
                    authentication.getName(),
                    groups,
                    permission.getCamundaPermission(),
                    resource.getCamundaResource(),
                    resourceId
            );
        }

        LOG.debug("Current User '{}' with groups: '{}'", authentication.getName(), groups);
        LOG.debug("Access check for [{}, {}, {}]: {}", resourceId, resource, permission, isAuthorized);

        if(!isAuthorized){
            throw new InsufficientAuthenticationException("User is not authorised to perform this action.");
        }
    }

    public void isAuthorizedFor(ResourceType resource, PermissionType permission) throws InsufficientAuthenticationException{
        this.isAuthorizedFor(null, resource, permission);
    }
}

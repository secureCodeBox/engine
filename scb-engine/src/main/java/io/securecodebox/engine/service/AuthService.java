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

import io.securecodebox.engine.auth.InsufficientAuthorizationException;
import io.securecodebox.engine.model.PermissionType;
import io.securecodebox.engine.model.ResourceType;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.camunda.bpm.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthService {
    private static String AUTH_DISABLED_TYPE = "none";
    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    ProcessEngine engine;

    @Value("${securecodebox.rest.auth}")
    private String authType;

    @Autowired
    public AuthService(ProcessEngine engine){
        this.engine = engine;
    }

    public void checkAuthorizedFor(String resourceId, ResourceType resource, PermissionType permission) throws InsufficientAuthorizationException {
        if (AUTH_DISABLED_TYPE.equals(authType)) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new InsufficientAuthorizationException("No authentication provided.");
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
        if (resourceId == null) {
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

        LOG.trace("Current User '{}' with groups: '{}'", authentication.getName(), groups);
        LOG.trace("Access check for [{}, {}, {}]: {}", resourceId, resource, permission, isAuthorized);

        if (!isAuthorized) {
            throw new InsufficientAuthorizationException("User is not authorised to perform this action.");
        }
    }

    public void checkAuthorizedFor(ResourceType resource, PermissionType permission) throws InsufficientAuthorizationException {
        this.checkAuthorizedFor(null, resource, permission);
    }

    public org.camunda.bpm.engine.impl.identity.Authentication getAuthentication() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        List<String> groups = engine
                .getIdentityService()
                .createGroupQuery()
                .groupMember(userId)
                .list()
                .stream()
                .map(Group::getId)
                .collect(Collectors.toList());

        List<String> tenants = engine
                .getIdentityService()
                .createTenantQuery()
                .userMember(userId)
                .list()
                .stream()
                .map(Tenant::getId)
                .collect(Collectors.toList());

        return new org.camunda.bpm.engine.impl.identity.Authentication(
                userId,
                groups,
                tenants
        );

    }
}

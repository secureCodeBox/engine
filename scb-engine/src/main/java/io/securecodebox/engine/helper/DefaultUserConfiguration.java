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

package io.securecodebox.engine.helper;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.AuthorizationQuery;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This configuration file adds a default user for scanner services
 */
@Configuration
public class DefaultUserConfiguration extends AbstractCamundaConfiguration {

    @Autowired
    private PropertyValueProvider properties;

    @Autowired
    private AuthConfiguration userConfiguration;

    private static final Logger LOG = LoggerFactory.getLogger(DefaultUserConfiguration.class);

    @Override
    public void postProcessEngineBuild(final ProcessEngine processEngine) {
        final IdentityService identityService = processEngine.getIdentityService();

        if (identityService.isReadOnly()) {
            LOG.warn("Identity service provider is Read Only, not creating any users.");
            return;
        }

        createGroups(processEngine);
        createUsers(identityService);
    }

    private void createGroups(final ProcessEngine processEngine) {

        for(AuthConfiguration.GroupConfiguration group : userConfiguration.getGroups()){
            createGroup(processEngine.getIdentityService(), group.getId(), group.getName());

            for (AuthConfiguration.GroupConfiguration.GroupAuthorizations authorization : group.getAuthorizations()){
                createAuthorizationForGroup(
                        processEngine.getAuthorizationService(),
                        group.getId(),
                        Resources.valueOf(authorization.getResource()),
                        authorization.getPermissions().stream().map(Permissions::forName).collect(Collectors.toList())
                );
            }
        }
    }

    private void createUsers(final IdentityService identityService) {
        // Deprecated single User Config
        final String scannerUserId = properties.getDefaultUserScannerId();
        final String scannerUserPw = properties.getDefaultUserScannerPassword();

        if (scannerUserId == null || scannerUserId.isEmpty() || scannerUserPw == null || scannerUserPw.isEmpty()) {
            LOG.info("No environment variables provided to create technical user for scanners");
        } else {
            AuthConfiguration.UserConfiguration user = new AuthConfiguration.UserConfiguration();
            user.setId(scannerUserId);
            user.setPassword(scannerUserPw);
            user.setFirstname("Technical-User");
            user.setLastname("Scanner-User");
            user.getGroups().add("scanner");
            createUser(identityService, user);
        }

        // Newer Multi User Config
        for (AuthConfiguration.UserConfiguration user : userConfiguration.getUsers()) {
            createUser(identityService, user);
        }
    }

    private void createUser(final IdentityService identityService, AuthConfiguration.UserConfiguration user) {
        boolean userForScannersAlreadyExists = identityService.createUserQuery().userId(user.getId()).count() > 0;
        if (userForScannersAlreadyExists) {
            LOG.info("User '{}' already exists", user.getId());
            return;
        }

        User newUser = identityService.newUser(user.getId());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        newUser.setFirstName(user.getFirstname());
        newUser.setLastName(user.getLastname());

        identityService.saveUser(newUser);

        for (String groupId : user.getGroups()){
            if(identityService.createGroupQuery().groupId(groupId).count() == 0){
                throw new RuntimeException("Tried to add user '" + user.getId() + "' to group '" + groupId + "' but the group doesn't exist. You'll need to change group of the user to a existing group or configure the group in your config.");
            }

            identityService.createMembership(user.getId(), groupId);
            LOG.info("Added user '{}' to group '{}'", user.getId(), groupId);
        }
    }

    private void createGroup(IdentityService identityService, String groupId, String groupName) {
        if (identityService.createGroupQuery().groupId(groupId).count() == 0) {
            Group group = identityService.newGroup(groupId);
            group.setName(groupName);
            group.setType("secureCodeBox");
            identityService.saveGroup(group);
            LOG.info("Created group: {}", group.getName());
        }
    }

    private void createAuthorizationForGroup(AuthorizationService authorizationService, String groupId, Resource resource, List<Permission> permissions) {
        if (permissions.isEmpty()) {
            throw new IllegalArgumentException("createAuthorizationForGroup needs at least one permission");
        }

        AuthorizationQuery authorizationQuery = authorizationService
                .createAuthorizationQuery()
                .groupIdIn(groupId)
                .resourceType(resource)
                .resourceId("*");
        for (Permission permission : permissions) {
            authorizationQuery.hasPermission(permission);
        }
        long authCounts = authorizationQuery.count();

        if (authCounts == 0) {
            Authorization auth = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
            auth.setGroupId(groupId);
            auth.setResource(resource);
            auth.setResourceId("*");
            for (Permission permission : permissions) {
                auth.addPermission(permission);
            }
            authorizationService.saveAuthorization(auth);

            LOG.info("Created Authorization for Group {}", groupId);
        }
    }
}

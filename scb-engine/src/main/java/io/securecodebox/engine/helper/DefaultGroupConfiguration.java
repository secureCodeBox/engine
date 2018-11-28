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
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration file generates the default engine groups
 *
 * @author Rüdiger Heins - iteratec GmbH
 * @since 07.02.18
 */
@Configuration
public class DefaultGroupConfiguration extends AbstractCamundaConfiguration {

    public static final String GROUP_SCANNER = "scanner";
    public static final String GROUP_APPROVER = "approver";
    public static final String GROUP_CI = "continuous-integration";

    private static final Logger LOG = LoggerFactory.getLogger(DefaultGroupConfiguration.class);

    @Override
    public void postProcessEngineBuild(final ProcessEngine processEngine) {

        final IdentityService identityService = processEngine.getIdentityService();
        createGroup(identityService, GROUP_APPROVER);

        createGroup(identityService, GROUP_SCANNER);
        createAuthorizationForGroup(
                processEngine.getAuthorizationService(),
                GROUP_SCANNER,
                Resources.PROCESS_INSTANCE,
                Permissions.READ, Permissions.UPDATE
        );


        createGroup(identityService, GROUP_CI);
        createAuthorizationForGroup(
                processEngine.getAuthorizationService(),
                GROUP_CI,
                Resources.PROCESS_DEFINITION,
                Permissions.CREATE_INSTANCE
        );
        createAuthorizationForGroup(
                processEngine.getAuthorizationService(),
                GROUP_CI,
                Resources.PROCESS_INSTANCE,
                Permissions.READ
        );
    }

    private void createGroup(IdentityService identityService, String groupId) {
        // create group
        if (identityService.createGroupQuery().groupId(groupId).count() == 0) {
            Group group = identityService.newGroup(groupId);
            group.setName("SecureCodeBox " + groupId);
            group.setType(Groups.GROUP_TYPE_SYSTEM);
            identityService.saveGroup(group);
            LOG.info("Created default secureCodeBox group: {}", group.getName());
        }
    }

    private void createAuthorizationForGroup(AuthorizationService authorizationService, String groupId, Resource resource, Permission... permissions){
        if(permissions.length == 0){
            throw new IllegalArgumentException("createAuthorizationForGroup needs at least one permission");
        }

        AuthorizationQuery authorizationQuery = authorizationService
                .createAuthorizationQuery()
                .groupIdIn(groupId)
                .resourceType(resource)
                .resourceId("*");
        for (Permission permission: permissions) {
            authorizationQuery.hasPermission(permission);
        }
        long authCounts = authorizationQuery.count();

        if(authCounts == 0){
            Authorization auth = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
            auth.setGroupId(groupId);
            auth.setResource(resource);
            auth.setResourceId("*");
            for (Permission permission: permissions) {
                auth.addPermission(permission);
            }
            authorizationService.saveAuthorization(auth);

            LOG.info("Created Authorization for Group {}", groupId);
        }
    }
}

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
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration file adds a default user for scanner services
 */
@Configuration
public class DefaultUserConfiguration extends AbstractCamundaConfiguration {

    @Autowired
    private PropertyValueProvider properties;

    @Autowired
    private ScannerUserConfiguration scannerUserConfiguration;

    private static final Logger LOG = LoggerFactory.getLogger(DefaultUserConfiguration.class);

    public static final String GROUP_SCANNER = "scanner";
    public static final String GROUP_APPROVER = "approver";
    public static final String GROUP_CI = "continuousIntegration";


    @Override
    public void postProcessEngineBuild(final ProcessEngine processEngine) {
        final IdentityService identityService = processEngine.getIdentityService();

        if(identityService.isReadOnly()) {
            LOG.warn("Identity service provider is Read Only, not creating any users.");
            return;
        }

        createGroups(processEngine);
        setupTechnicalUserForScanner(identityService);

        for(ScannerUserConfiguration.ScannerUser scannerUser : scannerUserConfiguration.getScannerUsers()){
            LOG.info("I'd create a User for with creds '{}:{}' now", scannerUser.getUsername(), scannerUser.getPassword());
        }
    }

    private void createGroups(final ProcessEngine processEngine){
        createGroup(processEngine.getIdentityService(), GROUP_APPROVER);

        createGroup(processEngine.getIdentityService(), GROUP_SCANNER);
        createAuthorizationForGroup(
                processEngine.getAuthorizationService(),
                GROUP_SCANNER,
                Resources.PROCESS_INSTANCE,
                Permissions.READ, Permissions.UPDATE
        );
        createAuthorizationForGroup(
                processEngine.getAuthorizationService(),
                GROUP_SCANNER,
                Resources.PROCESS_DEFINITION,
                Permissions.READ, Permissions.READ_INSTANCE, Permissions.UPDATE_INSTANCE
        );

        createGroup(processEngine.getIdentityService(), GROUP_CI);
        createAuthorizationForGroup(
                processEngine.getAuthorizationService(),
                GROUP_CI,
                Resources.PROCESS_DEFINITION,
                Permissions.CREATE_INSTANCE, Permissions.READ, Permissions.READ_HISTORY
        );
        createAuthorizationForGroup(
                processEngine.getAuthorizationService(),
                GROUP_CI,
                Resources.PROCESS_INSTANCE,
                Permissions.READ, Permissions.CREATE
        );
    }

    private void setupTechnicalUserForScanner(final IdentityService identityService) {
        final String scannerUserId = properties.getDefaultUserScannerId();
        final String scannerUserPw = properties.getDefaultUserScannerPassword();

        if(scannerUserId == null || scannerUserId.isEmpty() || scannerUserPw == null || scannerUserPw.isEmpty()) {
            LOG.info("No environment variables provided to create technical user for scanners");
            return;
        }

        boolean userForScannersAlreadyExists = identityService.createUserQuery().userId(scannerUserId).count() > 0;
        if(userForScannersAlreadyExists){
            LOG.info("Technical user for scanners already exists");
        } else {
            LOG.info("Creating technical user for scanners");
            LOG.info("User: {}, Password: {}", scannerUserId, scannerUserPw);
            createTechnicalUserForScanner(identityService, scannerUserId, scannerUserPw);
            identityService.createMembership(scannerUserId, GROUP_SCANNER);
        }
    }

    private void createTechnicalUserForScanner(final IdentityService identityService, final String scannerUserId, final String scannerUserPw) {
        User technicalUserForScanner = identityService.newUser(scannerUserId);
        technicalUserForScanner.setPassword(scannerUserPw);
        technicalUserForScanner.setFirstName("Technical-User");
        technicalUserForScanner.setLastName("Default-Scanner");

        identityService.saveUser(technicalUserForScanner);
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

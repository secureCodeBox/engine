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

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;


/**
 * This configuration file generates the default group approver and
 *
 * @author Rüdiger Heins - iteratec GmbH
 * @since 07.02.18
 */
@Configuration
public class DefaultGroupConfiguration extends AbstractCamundaConfiguration {

    public static final String GROUP_SCANNER = "scanner";
    public static final String GROUP_APPROVER = "approver";

    private static final Logger LOG = LoggerFactory.getLogger(DefaultGroupConfiguration.class);

    @Override
    public void postProcessEngineBuild(final ProcessEngine processEngine) {

        final IdentityService identityService = processEngine.getIdentityService();
        createGroup(identityService, GROUP_APPROVER);
        createGroup(identityService, GROUP_SCANNER);
    }

    private void createGroup(IdentityService identityService, String group) {
        // create group
        if (identityService.createGroupQuery().groupId(group).count() == 0) {
            Group approverGroup = identityService.newGroup(group);
            approverGroup.setName("SecureCodeBox " + group);
            approverGroup.setType(Groups.GROUP_TYPE_SYSTEM);
            identityService.saveGroup(approverGroup);
            LOG.info("Created default secureCodeBox group: {}", approverGroup.getName());
        }
    }

}

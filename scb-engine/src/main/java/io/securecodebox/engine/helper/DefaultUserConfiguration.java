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

    private static final Logger LOG = LoggerFactory.getLogger(DefaultUserConfiguration.class);

    @Override
    public void postProcessEngineBuild(final ProcessEngine processEngine) {
        final IdentityService identityService = processEngine.getIdentityService();

        if(identityService.isReadOnly()) {
            LOG.warn("Identity service provider is Read Only, not creating any users.");
            return;
        }

        setupTechnicalUserForScanner(identityService);
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
            createTechnicalUserForScanner(identityService, scannerUserId, scannerUserPw);
            identityService.createMembership(scannerUserId, DefaultGroupConfiguration.GROUP_SCANNER);
        }
    }

    private void createTechnicalUserForScanner(final IdentityService identityService, final String scannerUserId, final String scannerUserPw) {
        User technicalUserForScanner = identityService.newUser(scannerUserId);
        technicalUserForScanner.setPassword(scannerUserPw);
        technicalUserForScanner.setFirstName("Technical-User");
        technicalUserForScanner.setLastName("Default-Scanner");

        identityService.saveUser(technicalUserForScanner);
    }


}

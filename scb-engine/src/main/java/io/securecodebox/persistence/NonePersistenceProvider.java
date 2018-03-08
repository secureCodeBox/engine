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

package io.securecodebox.persistence;

import io.securecodebox.model.ScanProcessExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * "Fake" Persistence Provider which will be used if no real Persistence Provider was specified.
 * Following the Null Object Pattern
 *
 * @author Jannik Hollenbach - iteratec GmbH
 * @date 14.03.2017
 */
@Component
class NonePersistenceProvider implements PersistenceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(NonePersistenceProvider.class);

    public NonePersistenceProvider() {
        LOG.warn("No persistence available as no concrete persistence provider was configured!");
    }

    @Override
    public void initializePersistenceProvider(Object configuration, String persistanceName, String tenantId) {
        LOG.info("Skipping persistence since no concrete persistence provider was specified!");
    }

    @Override
    public void beforeSecureBoxProcess(ScanProcessExecution process) {

    }

    @Override
    public void afterSecureBoxProcess(ScanProcessExecution process) {

    }

    @Override
    public Object getJsonContent(String contentIdentifier, String microserviceName) {
        return null;
    }

    @Override
    public Object getJsonContentForType(String processUuid) {
        return null;
    }

    @Override
    public void saveJsonContent(String contentIdentifier, Map jsonContent, String microserviceName) {

    }

    @Override
    public void deleteJsonContent(String contentIdentifier) {

    }

    @Override
    public void deleteAllContent() {

    }

    @Override
    public void shutdownPersistence() {

    }
}

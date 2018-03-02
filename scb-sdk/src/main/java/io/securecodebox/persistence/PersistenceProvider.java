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

import io.securecodebox.scanprocess.ScanProcessExecution;

import java.util.Map;

/**
 * A general interface representing a persitence service to store and retrieve content objects.
 *
 * @author Robert Seedorff
 */
public interface PersistenceProvider {

    /**
     * Prepare the persistence provider on startup with the given configuration.
     *
     * @param configuration
     */
    void initializePersistenceProvider(Object configuration, String persistanceName, String tenantId);

    void beforeSecureBoxProcess(ScanProcessExecution process);

    void afterSecureBoxProcess(ScanProcessExecution process);

    /**
     * General method to retrieve a json formated content from the persitence provider.
     *
     * @param contentIdentifier A unique identifier (UUID) to identify the content to return.
     * @param microserviceName  The microservice name consuming this persistence service.
     *
     * @return A json content object from the persitence provider.
     */
    Object getJsonContent(String contentIdentifier, String microserviceName);

    Object getJsonContentForType(String processUuid);

    /**
     * General method to store the given json formated content.
     *
     * @param contentIdentifier A unique identifier (UUID) to identify the stored content.
     * @param jsonContent       The json formated content to store.
     * @param microserviceName  The microservice name consuming this persistence service.
     */
    void saveJsonContent(String contentIdentifier, Map jsonContent, String microserviceName);

    /**
     * Delete a persistent content based on the given identifier.
     *
     * @param contentIdentifier A unique identifier (UUID) to identify the content to delete.
     */
    void deleteJsonContent(String contentIdentifier);

    /**
     * Deletes all content currently stored in the persistence provider.
     */
    void deleteAllContent();

    /**
     * Close the persistence provider on application stop.
     */
    void shutdownPersistence();
}

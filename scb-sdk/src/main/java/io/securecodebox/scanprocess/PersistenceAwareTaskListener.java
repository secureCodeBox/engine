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

package io.securecodebox.scanprocess;

import io.securecodebox.constants.CommonConstants;
import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.persistence.PersistenceProvider;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * @author Robert Seedorff - iteratec GmbH
 */
@Component
public abstract class PersistenceAwareTaskListener implements ExecutionListener {

    @Autowired
    PersistenceProvider persistenceProvider;

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceAwareTaskListener.class);

    //protected Object config = ConfigurationLoader.getConfig()

    /**
     * @param map
     * @param process
     */
    protected void addProcessInformationsToResult(Map map, ScanProcessExecution process, String microserviceId) {

        // Add additional process informations
        map.put(CommonConstants.DOCUMENT_UUID, process.getId());
        map.put(CommonConstants.PROCESS_UUID, process.getId());
        map.put(CommonConstants.CONTEXT, process.getContext());
        map.put(CommonConstants.TENAND_ID, UUID.randomUUID());
        map.put(CommonConstants.MICROSERVICE, process.getScanners().iterator().next().getScannerType());
        map.put(CommonConstants.MICROSERVICE_ID, microserviceId);
    }

    /**
     * Eventually consistent: try to persist if the persistence provider is currently available
     *
     * @param report
     */
    protected void storeResult(String contentIdentifier, Map report, Object indexType, Object tenantId,
            String microserviceId) {
        try {
            //persistenceProvider.saveJsonContent(contentIdentifier, report, microserviceId);
        } catch (Exception e) {
            LOG.error("Unexpected Error while trying to save a Document!", e);
        }

        LOG.debug("PersistenceAwareTaskListener persistance finished...");
    }

}

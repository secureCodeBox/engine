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

package io.securecodebox.persistence.elasticsearch;

import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.persistence.PersistenceProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "securecodebox.persistence.provider", havingValue = "elasticsearch")
public class ElasticsearchPersistenceProvider implements PersistenceProvider {

    @Value("${securecodebox.persistence.elasticsearch.host}")
    private String host;

    @Value("${securecodebox.persistence.elasticsearch.port}")
    private int port;

    @Value("${securecodebox.persistence.elasticsearch.index.prefix}")
    private String indexPrefix;


    @Override
    public void initializePersistenceProvider(Object configuration, String persistanceName, String tenantId) {

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

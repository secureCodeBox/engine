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

import io.securecodebox.model.securitytest.SecurityTest;

import io.securecodebox.persistence.models.EngagementPayload;
import io.securecodebox.persistence.models.EngagementResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@ConditionalOnProperty(name = "securecodebox.persistence.provider", havingValue = "defectdojo")
public class DefectDojoPersistenceProvider implements PersistenceProvider{
    private static final Logger LOG = LoggerFactory.getLogger(DefectDojoPersistenceProvider.class);

    @Override
    public void persist(SecurityTest securityTest) throws PersistenceException {
        LOG.debug("Starting defectdojo persistence provider");

        // TODO: Add more details to engagement
        ResponseEntity<EngagementResponse> res = createEngagement(securityTest);

        LOG.debug("Created engagement: '{}'", res.getBody().getUrl());

        // TODO: Attach raw findings to engagement
        // parse raw findings json string
    }

    private ResponseEntity<EngagementResponse> createEngagement(SecurityTest securityTest) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        // TODO: Read Token from properties
        headers.set("Authorization", "Token 27aceaf759994177c319a767c55b70f15ecb6b75");

        EngagementPayload engagementPayload = new EngagementPayload();
        engagementPayload.setName(securityTest.getContext());

        // TODO: Make User configurable / always use technical user?
        engagementPayload.setLead("http://localhost:8000/api/v2/users/1/");
        // TODO: Read Product Id from securityTest Meta Data Field
        engagementPayload.setProduct("http://localhost:8000/api/v2/products/1/");

        // TODO: Use current date for both
        engagementPayload.setTargetStart("2018-12-04");
        engagementPayload.setTargetEnd("2018-12-04");

        engagementPayload.setStatus(EngagementPayload.Status.COMPLETED);

        HttpEntity<EngagementPayload> payload = new HttpEntity<>(engagementPayload, headers);

        return restTemplate.exchange("http://localhost:8000/api/v2/engagements/", HttpMethod.POST,payload, EngagementResponse.class);
    }
}

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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.securitytest.SecurityTest;

import io.securecodebox.persistence.models.EngagementPayload;
import io.securecodebox.persistence.models.EngagementResponse;
import io.securecodebox.persistence.models.ImportScanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

@Component
@ConditionalOnProperty(name = "securecodebox.persistence.provider", havingValue = "defectdojo")
public class DefectDojoPersistenceProvider implements PersistenceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DefectDojoPersistenceProvider.class);

    @Override
    public void persist(SecurityTest securityTest) throws PersistenceException {
        LOG.debug("Starting defectdojo persistence provider");

        LOG.debug("RawFindings: {}", securityTest.getReport().getRawFindings());

        ObjectMapper objectMapper = new ObjectMapper();

        // TODO: Add more details to engagement
        ResponseEntity<EngagementResponse> res = createEngagement(securityTest);

        String engagementUrl = res.getBody().getUrl();

        LOG.debug("Created engagement: '{}'", engagementUrl);

        try {
            String rawRawFindings = objectMapper.readValue(securityTest.getReport().getRawFindings(), String.class);

            List<String> rawResults = objectMapper.readValue(rawRawFindings,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            for (String rawResult : rawResults) {
                createFindings(securityTest, rawResult, engagementUrl);
            }
        } catch (IOException e) {
            LOG.error("Could not deserialize rawResults");
            e.printStackTrace();
        }
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

        String productId = securityTest.getMetaData().get("DEFECT_DOJO_PRODUCT");

        if(productId == null){
            throw new RuntimeException("DefectDojo persistence provider was configured but no product id was provided in the security test meta fields.");
        }

        engagementPayload.setProduct("http://localhost:8000/api/v2/products/" + productId + "/");

        // TODO: Use current date for both
        engagementPayload.setTargetStart("2018-12-04");
        engagementPayload.setTargetEnd("2018-12-04");

        engagementPayload.setStatus(EngagementPayload.Status.COMPLETED);

        HttpEntity<EngagementPayload> payload = new HttpEntity<>(engagementPayload, headers);

        return restTemplate.exchange("http://localhost:8000/api/v2/engagements/", HttpMethod.POST, payload, EngagementResponse.class);
    }

    private ResponseEntity<ImportScanResponse> createFindings(SecurityTest securityTest, String rawResult, String engagementUrl) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        // TODO: Read Token from properties
        headers.set("Authorization", "Token 27aceaf759994177c319a767c55b70f15ecb6b75");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        restTemplate.setMessageConverters(Arrays.asList(new FormHttpMessageConverter(), new ResourceHttpMessageConverter(), new MappingJackson2HttpMessageConverter()));

        MultiValueMap<String, Object> mvn = new LinkedMultiValueMap<>();
        mvn.add("engagement", engagementUrl);
        // TODO: Fix User hardcoding
        mvn.add("lead", "http://localhost:8000/api/v2/users/1/");
        mvn.add("scan_date", "2018-12-04");
        mvn.add("scan_type", getDefectDojoScanName(securityTest.getName()));

        try {
            ByteArrayResource contentsAsResource = new ByteArrayResource(rawResult.getBytes("UTF-8")) {
                @Override
                public String getFilename() {
                    return "this_needs_to_be_here_but_doesnt_really_matter.txt";
                }
            };

            mvn.add("file", contentsAsResource);

            HttpEntity<MultiValueMap> payload = new HttpEntity<>(mvn, headers);

            return restTemplate.exchange("http://localhost:8000/api/v2/import-scan/", HttpMethod.POST, payload, ImportScanResponse.class);
        } catch (UnsupportedEncodingException e) {
            LOG.error("UnsupportedEncodingException {}", e);
        }
        return null;
    }

    protected String getDefectDojoScanName(String securityTestName){
        switch (securityTestName){
            case "nmap":
                return "Nmap Scan";
            case "nikto":
                return "Nikto Scan";
            case "arachni":
                return "Arachni Scan";
            case "zap":
                return "Zap Scan";
            default:
                throw new RuntimeException("No defectdojo parser for securityTest: '" + securityTestName + "'");
        }
    }
}

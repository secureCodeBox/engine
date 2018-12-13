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
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@ConditionalOnProperty(name = "securecodebox.persistence.provider", havingValue = "defectdojo")
public class DefectDojoPersistenceProvider implements PersistenceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DefectDojoPersistenceProvider.class);

    @Value("${securecodebox.persistence.defectdojo.host}")
    private String defectdojoHost;
    @Value("${securecodebox.persistence.defectdojo.port}")
    private int defectdojoPort;
    @Value("${securecodebox.persistence.defectdojo.scheme:http}")
    private String defectdojoScheme;

    @Value("${securecodebox.persistence.defectdojo.baseurl}")
    protected String defectDojoUrl;

    @Value("${securecodebox.persistence.defectdojo.apikey}")
    protected String defectDojoApiKey;

    protected static final String DATE_FORMAT = "yyyy-MM-dd";
    protected static final String TIME_FORMAT = "HH-mm-ss";

    @Override
    public void persist(SecurityTest securityTest) throws PersistenceException {
        LOG.debug("Starting defectdojo persistence provider");
        LOG.debug("RawFindings: {}", securityTest.getReport().getRawFindings());

        checkConnection();

        ResponseEntity<EngagementResponse> res = createEngagement(securityTest);
        String engagementUrl = res.getBody().getUrl();
        LOG.debug("Created engagement: '{}'", engagementUrl);

        for (String rawResult : getRawResults(securityTest)) {
            createFindings(securityTest, rawResult, engagementUrl);
        }
    }

    private void checkConnection() throws DefectDojoUnreachableException {
        try (RestHighLevelClient highLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(defectdojoHost, defectdojoPort, defectdojoScheme)))) {
            highLevelClient.ping();
        } catch (IOException e) {
            throw new DefectDojoUnreachableException("Could not reach defectdojo at '" + defectdojoHost + "'!");
        }
    }

    private List<String> getRawResults(SecurityTest securityTest) throws DefectDojoPersistenceException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String rawRawFindings = objectMapper.readValue(securityTest.getReport().getRawFindings(), String.class);

            return objectMapper.readValue(rawRawFindings,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (IOException e) {
            throw new DefectDojoPersistenceException("RawResults were in an unexpected format. Might be something wrong with the scanner implementation?");
        }
    }

    private ResponseEntity<EngagementResponse> createEngagement(SecurityTest securityTest) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + defectDojoApiKey);

        EngagementPayload engagementPayload = new EngagementPayload();
        engagementPayload.setName(securityTest.getContext());

        // TODO: Make User configurable / always use technical user?
        engagementPayload.setLead(defectDojoUrl + "/api/v2/users/1/");
        engagementPayload.setBranch(securityTest.getMetaData().get("SCB_BRANCH"));
        engagementPayload.setBuildID(securityTest.getMetaData().get("SCB_BUILD_ID"));
        engagementPayload.setCommitHash(securityTest.getMetaData().get("SCB_COMMIT_HASH"));
        engagementPayload.setRepo(securityTest.getMetaData().get("SCB_REPO"));
        engagementPayload.setTracker(securityTest.getMetaData().get("SCB_TRACKER"));

        //TODO Fill Description with necessary Information about the security Test
        //engagementPayload.setDescription(currentDate() + "  " + currentTime());

        //TODO: Configure Tool Configurations for the Fields below
        /*
        engagementPayload.setBuildServer(securityTest.getMetaData().get("SCB_BUILD_SERVER"));
        engagementPayload.setScmServer(securityTest.getMetaData().get("SCB_SCM_SERVER"));
        engagementPayload.setOrchestrationEngine(securityTest.getMetaData().get("SCB_ORCHESTRATION_ENGINE"));
        */

        String productId = securityTest.getMetaData().get("SCB_PRODUCT");

        if (productId == null) {
            throw new DefectDojoPersistenceException("DefectDojo persistence provider was configured but no product id was provided in the security test meta fields.");
        }

        engagementPayload.setProduct(defectDojoUrl + "/api/v2/products/" + productId + "/");

        engagementPayload.setTargetStart(currentDate());
        engagementPayload.setTargetEnd(currentDate());

        engagementPayload.setStatus(EngagementPayload.Status.COMPLETED);

        HttpEntity<EngagementPayload> payload = new HttpEntity<>(engagementPayload, headers);

        try {
            return restTemplate.exchange(defectDojoUrl + "/api/v2/engagements/", HttpMethod.POST, payload, EngagementResponse.class);
        } catch (HttpClientErrorException e) {
            LOG.warn("Failed to create Engagement for SecurityTest. {}", e);
            LOG.warn("Failure response body. {}", e.getResponseBodyAsString());
            throw new DefectDojoPersistenceException("Failed to create Engagement for SecurityTest", e);
        }
    }

    private String currentDate() {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }

    private String currentTime() {
        return new SimpleDateFormat(TIME_FORMAT).format(new Timestamp(System.currentTimeMillis()));
    }

    private ResponseEntity<ImportScanResponse> createFindings(SecurityTest securityTest, String rawResult, String engagementUrl) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", "Token " + defectDojoApiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        restTemplate.setMessageConverters(Arrays.asList(new FormHttpMessageConverter(), new ResourceHttpMessageConverter(), new MappingJackson2HttpMessageConverter()));

        MultiValueMap<String, Object> mvn = new LinkedMultiValueMap<>();
        mvn.add("engagement", engagementUrl);
        // TODO: Fix User hardcoding
        mvn.add("lead", defectDojoUrl + "/api/v2/users/1/");
        mvn.add("scan_date", currentDate());
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

            return restTemplate.exchange(defectDojoUrl + "/api/v2/import-scan/", HttpMethod.POST, payload, ImportScanResponse.class);
        } catch (UnsupportedEncodingException e) {
            LOG.error("UnsupportedEncodingException {}", e);
        } catch (HttpClientErrorException e) {
            LOG.warn("Failed to import findings to DefectDojo. Request failed with status code: '{}'.", e.getStatusCode());
            LOG.warn("Failure body: {}", e.getResponseBodyAsString());
            throw new DefectDojoPersistenceException("Failed to attach findings to engagement.");
        }
        return null;
    }

    protected String getDefectDojoScanName(String securityTestName) {
        Map<String, String> scannerDefectDojoMapping = new HashMap<>();

        // Officially supported by secureCodeBox
        scannerDefectDojoMapping.put("arachni", "Arachni Scan");
        scannerDefectDojoMapping.put("nmap", "Nmap Scan");
        scannerDefectDojoMapping.put("zap", "ZAP Scan");

        // TODO: Why is nikto not in the list?

        // Can be used by 3rd party integrations to
        // import these scan results directly into defectdojo
        scannerDefectDojoMapping.put("appspider", "AppSpider Scan");
        scannerDefectDojoMapping.put("bandit", "Bandit Scan");
        scannerDefectDojoMapping.put("burp", "Burp Scan");
        scannerDefectDojoMapping.put("checkmarx", "Checkmarx Scan");
        scannerDefectDojoMapping.put("dependencycheck", "Dependency Check Scan");
        scannerDefectDojoMapping.put("gosec", "Gosec Scanner");
        scannerDefectDojoMapping.put("nessus", "Nessus Scan");
        scannerDefectDojoMapping.put("nexpose", "Nexpose Scan");
        scannerDefectDojoMapping.put("nodesecurityplattform", "Node Security Platform Scan");
        scannerDefectDojoMapping.put("openvas", "OpenVAS CSV");
        scannerDefectDojoMapping.put("qualys", "Qualys Scan");
        scannerDefectDojoMapping.put("qualyswebapp", "Qualys Webapp Scan");
        scannerDefectDojoMapping.put("retirejs", "Retire.js Scan");
        scannerDefectDojoMapping.put("skf", "SKF Scan");
        scannerDefectDojoMapping.put("ssllabs", "SSL Labs Scan");
        scannerDefectDojoMapping.put("snyk", "Snyk Scan");
        scannerDefectDojoMapping.put("trustwave", "Trustwave Scan (CSV)");
        scannerDefectDojoMapping.put("vgg", "VCG Scan");
        scannerDefectDojoMapping.put("veracode", "Veracode Scan");

        if (scannerDefectDojoMapping.containsKey(securityTestName)) {
            return scannerDefectDojoMapping.get(securityTestName);
        }

        throw new DefectDojoPersistenceException("No defectdojo parser for securityTest: '" + securityTestName + "'");
    }

}

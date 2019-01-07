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

import io.securecodebox.persistence.models.*;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "securecodebox.persistence.defectdojo.enabled", havingValue = "true")
public class DefectDojoPersistenceProvider implements PersistenceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DefectDojoPersistenceProvider.class);

    @Value("${securecodebox.persistence.defectdojo.host}")
    private String defectdojoHost;
    @Value("${securecodebox.persistence.defectdojo.port}")
    private int defectdojoPort;
    @Value("${securecodebox.persistence.defectdojo.scheme:http}")
    private String defectdojoScheme;

    @Autowired
    DefectDojoService defectDojoService;

    @Autowired
    DescriptionGenerator descriptionGenerator;

    protected static final String DATE_FORMAT = "yyyy-MM-dd";

    @Value("${securecodebox.persistence.defectdojo.baseurl}")
    String defectDojoUrl;

    @Value("${securecodebox.persistence.defectdojo.apikey}")
    protected String defectDojoApiKey;

    private HttpHeaders getHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + defectDojoApiKey);
        return headers;
    }

    @Override
    public void persist(SecurityTest securityTest) throws PersistenceException {
        LOG.debug("Starting defectdojo persistence provider");
        LOG.debug("RawFindings: {}", securityTest.getReport().getRawFindings());

//        checkConnection();
        checkToolTypes();

        EngagementResponse res = createEngagement(securityTest);
        String engagementUrl = res.getUrl();
        LOG.debug("Created engagement: '{}'", engagementUrl);

//        for (String rawResult : getRawResults(securityTest)) {
//            createFindings(securityTest, rawResult, engagementUrl);
//        }
    }

    static final String GIT_SERVER_NAME = "GitServer";
    static final String BUILD_SERVER_NAME = "BuildServer";
    static final String SECURITY_TEST_SERVER_NAME = "SecurityTestOrchestrationEngine";

    private void checkToolTypes() {
        DefectDojoResponse<ToolType> toolTypeGitResponse = defectDojoService.getToolTypeByName(GIT_SERVER_NAME);
        if(toolTypeGitResponse.getCount() == 0){
            defectDojoService.createToolType(GIT_SERVER_NAME, "Source Code Management Server");
        }

        DefectDojoResponse<ToolType> toolTypeScmResponse = defectDojoService.getToolTypeByName(BUILD_SERVER_NAME);
        if(toolTypeScmResponse.getCount() == 0){
            defectDojoService.createToolType(BUILD_SERVER_NAME, "Build Server responsible for starting Security Scan");
        }

        DefectDojoResponse<ToolType> toolTypeStoeResponse = defectDojoService.getToolTypeByName(SECURITY_TEST_SERVER_NAME);
        if(toolTypeStoeResponse.getCount() == 0){
            defectDojoService.createToolType(SECURITY_TEST_SERVER_NAME, "Security Test Orchestration Engine");
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

    private EngagementResponse createEngagement(SecurityTest securityTest) {
        EngagementPayload engagementPayload = new EngagementPayload();
        engagementPayload.setName(securityTest.getContext());

        String productId = securityTest.getMetaData().get("DEFECT_DOJO_PRODUCT");
        if (productId == null) {
            throw new DefectDojoPersistenceException("DefectDojo persistence provider was configured but no product id was provided in the security test meta fields.");
        }
        String username = securityTest.getMetaData().get("DEFECT_DOJO_USER");

        engagementPayload.setProduct(defectDojoUrl + "/api/v2/products/" + productId + "/");
        engagementPayload.setLead(defectDojoService.getUserUrl(username));
        engagementPayload.setDescription(descriptionGenerator.generate(securityTest));
        engagementPayload.setBranch(securityTest.getMetaData().get("SCB_BRANCH"));
        engagementPayload.setBuildID(securityTest.getMetaData().get("SCB_BUILD_ID"));
        engagementPayload.setCommitHash(securityTest.getMetaData().get("SCB_COMMIT_HASH"));
        engagementPayload.setRepo(securityTest.getMetaData().get("SCB_REPO"));
        engagementPayload.setTracker(securityTest.getMetaData().get("SCB_TRACKER"));

        engagementPayload.setBuildServer(defectDojoService.getToolConfiguration(securityTest.getMetaData().get("SCB_BUILD_SERVER"), "BuildServer"));
        engagementPayload.setScmServer(defectDojoService.getToolConfiguration(securityTest.getMetaData().get("SCB_SCM_SERVER"), "GitServer"));
        engagementPayload.setOrchestrationEngine(defectDojoService.getToolConfiguration("https://github.com/secureCodeBox","SecurityTestOrchestrationEngine"));

        engagementPayload.setTargetStart(currentDate());
        engagementPayload.setTargetEnd(currentDate());

        engagementPayload.setStatus(EngagementPayload.Status.COMPLETED);

        return defectDojoService.createEngagement(engagementPayload);
    }

    private String currentDate() {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }

    private ResponseEntity<ImportScanResponse> createFindings(SecurityTest securityTest, String rawResult, String engagementUrl) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = getHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        restTemplate.setMessageConverters(Arrays.asList(new FormHttpMessageConverter(), new ResourceHttpMessageConverter(), new MappingJackson2HttpMessageConverter()));

        MultiValueMap<String, Object> mvn = new LinkedMultiValueMap<>();
        mvn.add("engagement", engagementUrl);
        mvn.add("lead", defectDojoService.getUserUrl(securityTest.getMetaData().get("DEFECT_DOJO_USER")));
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

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

import io.securecodebox.persistence.exceptions.DefectDojoLoopException;
import io.securecodebox.persistence.exceptions.DefectDojoPersistenceException;
import io.securecodebox.persistence.exceptions.DefectDojoProductNotFound;
import io.securecodebox.persistence.exceptions.DefectDojoUserNotFound;
import io.securecodebox.persistence.models.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Iterator;

@Component
@ConditionalOnProperty(name = "securecodebox.persistence.defectdojo.enabled", havingValue = "true")
public class DefectDojoService {
    @Value("${securecodebox.persistence.defectdojo.url}")
    protected String defectDojoUrl;

    @Value("${securecodebox.persistence.defectdojo.auth.key}")
    protected String defectDojoApiKey;

    @Value("${securecodebox.persistence.defectdojo.auth.name}")
    protected String defectDojoDefaultUserName;

    protected static final String DATE_FORMAT = "yyyy-MM-dd";
    protected static final String DATE_TIME_FORMAT = "yyyy-MM-dd hh:m:ss";

    Clock clock = Clock.systemDefaultZone();

    private String currentDate() {
        return LocalDate.now(clock).format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }
    private String currentDateTime() {
        return LocalDateTime.now(clock).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }    

    private static final Logger LOG = LoggerFactory.getLogger(DefectDojoService.class);

    private HttpHeaders getHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + defectDojoApiKey);
        return headers;
    }

    public DefectDojoResponse<ToolType> getToolTypeByName(String name){
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity toolTypeRequest = new HttpEntity(getHeaders());

        String uri = defectDojoUrl + "/api/v2/tool_types/?name=" + name;
        ResponseEntity<DefectDojoResponse<ToolType>> toolTypeResponse = restTemplate.exchange(uri, HttpMethod.GET, toolTypeRequest, new ParameterizedTypeReference<DefectDojoResponse<ToolType>>(){});

        return toolTypeResponse.getBody();
    }

    public void createToolType(String name, String description){
        RestTemplate restTemplate = new RestTemplate();

        ToolType toolType = new ToolType();
        toolType.setName(name);
        toolType.setDescription(description);
        HttpEntity<ToolType> toolPayload = new HttpEntity<>(toolType, getHeaders());

        restTemplate.exchange(defectDojoUrl + "/api/v2/tool_types/", HttpMethod.POST, toolPayload, ToolType.class);
    }

    public Long retrieveUserId(String username){
        RestTemplate restTemplate = new RestTemplate();

        if(username == null){
            username = defectDojoDefaultUserName;
        }

        String uri = defectDojoUrl + "/api/v2/users/?username=" + username;
        HttpEntity userRequest = new HttpEntity(getHeaders());
        ResponseEntity<DefectDojoResponse<DefectDojoUser>> userResponse = restTemplate.exchange(uri, HttpMethod.GET, userRequest, new ParameterizedTypeReference<DefectDojoResponse<DefectDojoUser>>(){});
        if(userResponse.getBody().getCount() == 1){
            return userResponse.getBody().getResults().get(0).getId();
        }
        else {
            throw new DefectDojoUserNotFound(MessageFormat.format("Could not find user: \"{0}\" in DefectDojo", username));
        }
    }

    public long retrieveProductId(String product){
        RestTemplate restTemplate = new RestTemplate();

        String uri = defectDojoUrl + "/api/v2/products/?name=" + product;
        HttpEntity productRequest = new HttpEntity(getHeaders());
        ResponseEntity<DefectDojoResponse<DefectDojoProduct>> productResponse = restTemplate.exchange(uri, HttpMethod.GET, productRequest, new ParameterizedTypeReference<DefectDojoResponse<DefectDojoProduct>>(){});
        if(productResponse.getBody().getCount() == 1){
            return productResponse.getBody().getResults().get(0).getId();
        }
        else {
            throw new DefectDojoProductNotFound(MessageFormat.format("Could not find product: \"{0}\" in DefectDojo", product));
        }
    }
    private long retrieveOrCreateProduct(String productName, String productDescription, List<String> productTags) {
        long productId = 0;
        try {
            productId = retrieveProductId(productName);
        } catch(DefectDojoProductNotFound e) {
            LOG.debug("Given product does not exists");
        }
        if(productId == 0) {
            ProductResponse productResponse = createProduct(productName, productDescription, productTags);
            productId = productResponse.getId();
        }
        return productId;
    }    

    public Long retrieveOrCreateToolConfiguration(String toolUrl, String toolType){
        if (toolUrl == null){
            return null;
        }

        ResponseEntity<DefectDojoResponse<ToolConfig>> toolResponse = retrieveToolConfiguration(toolUrl);
        if(toolResponse.getBody().getCount() > 0){
            LOG.info("Tool configuration already exists. Returning existing configuration.");
            return toolResponse.getBody().getResults().get(0).getId();
        }
        else {
            LOG.info("Tool configuration does not exist yet. Creating new configuration.");
            createToolConfiguration(toolUrl, toolType);
            return retrieveToolConfiguration(toolUrl).getBody().getResults().get(0).getId();
        }
    }

    private ResponseEntity<DefectDojoResponse<ToolConfig>> retrieveToolConfiguration(String toolUrl) {
        RestTemplate restTemplate = new RestTemplate();
        String uri = defectDojoUrl + "/api/v2/tool_configurations/?name=" + toolUrl;
        HttpEntity toolRequest = new HttpEntity(getHeaders());
        return restTemplate.exchange(uri, HttpMethod.GET, toolRequest, new ParameterizedTypeReference<DefectDojoResponse<ToolConfig>>(){});
    }

    private void createToolConfiguration(String toolUrl, String toolType) {
        HttpEntity toolTypeRequest = new HttpEntity(getHeaders());
        String toolTypeRequestUri = defectDojoUrl + "/api/v2/tool_types/?name=" + toolType;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<DefectDojoResponse<ToolType>> toolTypeResponse = restTemplate.exchange(toolTypeRequestUri, HttpMethod.GET, toolTypeRequest, new ParameterizedTypeReference<DefectDojoResponse<ToolType>>(){});
        String toolTypeId = toolTypeResponse.getBody().getResults().get(0).getId();

        ToolConfig toolConfig = new ToolConfig();
        toolConfig.setName(toolUrl);
        toolConfig.setToolType(toolTypeId);
        toolConfig.setConfigUrl(toolUrl);
        toolConfig.setDescription(toolType);

        HttpEntity<ToolConfig> toolPayload = new HttpEntity<>(toolConfig, getHeaders());
        restTemplate.exchange(defectDojoUrl + "/api/v2/tool_configurations/", HttpMethod.POST, toolPayload, ToolConfig.class);
    }

    public EngagementResponse createEngagement(EngagementPayload engagementPayload) {
        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<EngagementPayload> payload = new HttpEntity<>(engagementPayload, getHeaders());

        try {
            ResponseEntity<EngagementResponse> response = restTemplate.exchange(defectDojoUrl + "/api/v2/engagements/", HttpMethod.POST, payload, EngagementResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            LOG.warn("Failed to create Engagement for SecurityTest. {}", e);
            LOG.warn("Failure response body. {}", e.getResponseBodyAsString());
            throw new DefectDojoPersistenceException("Failed to create Engagement for SecurityTest", e);
        }
    }
    
    public ImportScanResponse createFindings(String rawResult, long engagementId, long lead, String currentDate, String defectDojoScanName) {
        return createFindings(rawResult, engagementId, lead, currentDate,defectDojoScanName, "", new LinkedMultiValueMap<>());
    }
    /**
     * Before version 1.5.4. testName (in DefectDojo _test_type_) must be defectDojoScanName, afterwards, you can have somethings else
     */
    public ImportScanResponse createFindings(String rawResult, long engagementId, long lead, String currentDate,String defectDojoScanName, String testName, MultiValueMap<String, Object> options) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        restTemplate.setMessageConverters(Arrays.asList(new FormHttpMessageConverter(), new ResourceHttpMessageConverter(), new MappingJackson2HttpMessageConverter()));

        MultiValueMap<String, Object> mvn = new LinkedMultiValueMap<>();
        mvn.add("engagement", Long.toString(engagementId));
        mvn.add("lead", Long.toString(lead));
        mvn.add("scan_date", currentDate);
        mvn.add("scan_type", defectDojoScanName);
        mvn.add("close_old_findings", "true");
        mvn.add("skip_duplicates", "false");

        if(!testName.isEmpty())
            mvn.add("test_type", testName);

        Iterator<String> it = options.keySet().iterator();
        while(it.hasNext()){
            String theKey = (String)it.next();
            if(mvn.containsKey(theKey)) {
                mvn.remove(theKey);
            }
        }
        mvn.addAll(options);            
        
        try {
            ByteArrayResource contentsAsResource = new ByteArrayResource(rawResult.getBytes(StandardCharsets.UTF_8)) {
                @Override
                public String getFilename() {
                    return "this_needs_to_be_here_but_doesnt_really_matter.txt";
                }
            };

            mvn.add("file", contentsAsResource);

            HttpEntity<MultiValueMap> payload = new HttpEntity<>(mvn, headers);

            return restTemplate.exchange(defectDojoUrl + "/api/v2/import-scan/", HttpMethod.POST, payload, ImportScanResponse.class).getBody();
        } catch (HttpClientErrorException e) {
            LOG.warn("Failed to import findings to DefectDojo. Request failed with status code: '{}'.", e.getStatusCode());
            LOG.warn("Failure body: {}", e.getResponseBodyAsString());
            throw new DefectDojoPersistenceException("Failed to attach findings to engagement.");
        }
    }
    /**
     * When DefectDojo >= 1.5.4 is used, testType can be given. Add testName in case DefectDojo >= 1.5.4 is used
     * Using testName for each branch leads to multiple issues in DefectDojo, so it is not recommended
     */
    private Optional<Long> getTestIdByEngagementName(long engagementId, String testName, long offset) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(defectDojoUrl + "/api/v2/tests")
                .queryParam("engagement", Long.toString(engagementId))
                .queryParam("limit", Long.toString(50L))
                .queryParam("offset", Long.toString(offset));
        if(testName != null && !testName.isEmpty()) {
            builder.queryParam("testType", testName);
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity engagementRequest = new HttpEntity(getHeaders());

        ResponseEntity<DefectDojoResponse<TestResponse>> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, engagementRequest, new ParameterizedTypeReference<DefectDojoResponse<TestResponse>>(){});

        Optional<Long> testResponseId = null;
        Optional<Long> latestTestResponseId = Optional.empty();
        for(TestResponse test : response.getBody().getResults()) {
            if(testName == null || (test.getTitle() != null && test.getTitle().equals(testName))) {
                testResponseId = Optional.of(test.getId());
            }
            if(!latestTestResponseId.isPresent() || latestTestResponseId.get() < test.getId()) {
                latestTestResponseId = Optional.of(test.getId());
            }

        }
        if(testResponseId != null) {
            return testResponseId;
        }
        
        if(response.getBody().getNext() != null) {
            return getTestIdByEngagementName(engagementId, testName, offset + 1);
        }
        LOG.info("Test with name '{}' not found, using latest.", testName);
        return latestTestResponseId;
    }
    /*
    * Be aware that using latest might results in "conflicting" "latest" in case a new test is added while requesting latest
    */
    public Optional<Long> getLatestTestIdByEngagementName(String engagementName, String productName, String testName, long offset) {
        Optional<Long> optionalEngagementId = getEngagementIdByEngagementName(engagementName, productName);
        if(!optionalEngagementId.isPresent()) {
            LOG.warn("engagementName with name '{}' not found.", engagementName);
            return Optional.empty();
        }
        Long engagementId = optionalEngagementId.get();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(defectDojoUrl + "/api/v2/tests")
                .queryParam("engagement", Long.toString(engagementId))
                .queryParam("limit", Long.toString(50L))
                .queryParam("offset", Long.toString(offset));
        if(testName != null) builder.queryParam("testType", testName);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity engagementRequest = new HttpEntity(getHeaders());

        ResponseEntity<DefectDojoResponse<TestResponse>> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, engagementRequest, new ParameterizedTypeReference<DefectDojoResponse<TestResponse>>(){});

        Optional<Long> testResponseId = null;
        for(TestResponse test : response.getBody().getResults()){
            if(testResponseId == null || test.getId() > testResponseId.get()) {
                testResponseId = Optional.of(test.getId());
            }
        }
                
        if(response.getBody().getNext() != null){
            Optional<Long> subOptionalTestResponseId = getTestIdByEngagementName(engagementId, testName, offset + 1);
            if(testResponseId == null ||
                (subOptionalTestResponseId.isPresent()) && 
                subOptionalTestResponseId.get() > testResponseId.get() 
            ) {
                testResponseId = subOptionalTestResponseId;
            }
        }
        if(testResponseId != null) {
            return testResponseId;
        }

        LOG.warn("Test with name '{}' not found.", testName);
        return Optional.empty();
    }

    private EngagementResponse createTest(TestPayload testPayload) {
        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<TestPayload> payload = new HttpEntity<>(testPayload, getHeaders());

        try {
            ResponseEntity<EngagementResponse> response = restTemplate.exchange(defectDojoUrl + "/api/v2/tests/", HttpMethod.POST, payload, EngagementResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            LOG.warn("Failed to create Test for SecurityTest. {}", e);
            LOG.warn("Failure response body. {}", e.getResponseBodyAsString());
            throw new DefectDojoPersistenceException("Failed to create Test for SecurityTest", e);
        }
    }    
    private long getTestIdOrCreate(long engagementId, TestPayload testPayload, String testType) {
        Long testId = getTestIdByEngagementName(engagementId, testPayload.getTitle(), 0).orElseGet(() -> {
            testPayload.setEngagement(Long.toString(engagementId));
            testPayload.setTargetStart(currentDateTime());
            testPayload.setTargetEnd(currentDateTime());
            testPayload.setTestType(Integer.toString(TestPayload.getTestTypeIdForName(testType)));
            return createTest(testPayload).getId();
        });
        return testId.longValue();
    }
  
    /**
     * @deprecated
     */
    public ImportScanResponse createFindingsReImport(String rawResult, String productName, String engagementName, long lead, String currentDate, String defectDojoScanName, EngagementPayload engagementPayload, TestPayload testPayload, MultiValueMap<String, Object> options) {
        return createFindingsReImport(rawResult, productName, engagementName, lead, currentDate, defectDojoScanName, engagementPayload, testPayload, options, "no Description", java.util.Collections.emptyList());
    }

    public ImportScanResponse createFindingsReImport(String rawResult, String productName, String engagementName, long lead, String currentDate, String defectDojoScanName, EngagementPayload engagementPayload, TestPayload testPayload, MultiValueMap<String, Object> options, String productDescription, List<String> productTags)  {
        long productId = retrieveOrCreateProduct(productName, productDescription, productTags);
        long engagementId = getEngagementIdByEngagementNameOrCreate(productId, engagementName, engagementPayload, lead);
        long testId = getTestIdOrCreate(engagementId, testPayload, defectDojoScanName);
        return createFindingsReImport(rawResult, testId, lead, currentDate, defectDojoScanName, options);
    }
    
    public ImportScanResponse createFindingsReImport(String rawResult, long testId, long lead, String currentDate,String defectDojoScanName, MultiValueMap<String, Object> options) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        restTemplate.setMessageConverters(Arrays.asList(new FormHttpMessageConverter(), new ResourceHttpMessageConverter(), new MappingJackson2HttpMessageConverter()));

        MultiValueMap<String, Object> mvn = new LinkedMultiValueMap<>();
        mvn.add("test", Long.toString(testId));
        mvn.add("lead", Long.toString(lead));
        mvn.add("scan_date", currentDate);
        mvn.add("scan_type", defectDojoScanName);
        mvn.add("close_old_findings", "true");
        mvn.add("skip_duplicates", "false");

        Iterator<String> it = options.keySet().iterator();
        while(it.hasNext()){
            String theKey = (String)it.next();
            if(mvn.containsKey(theKey)) {
                mvn.remove(theKey);
            }
        }
        mvn.addAll(options);
        
        try {
            ByteArrayResource contentsAsResource = new ByteArrayResource(rawResult.getBytes(StandardCharsets.UTF_8)) {
                @Override
                public String getFilename() {
                    return "this_needs_to_be_here_but_doesnt_really_matter.txt";
                }
            };

            mvn.add("file", contentsAsResource);

            HttpEntity<MultiValueMap> payload = new HttpEntity<>(mvn, headers);

            return restTemplate.exchange(defectDojoUrl + "/api/v2/reimport-scan/", HttpMethod.POST, payload, ImportScanResponse.class).getBody();
        } catch (HttpClientErrorException e) {
            LOG.warn("Failed to import findings to DefectDojo. Request failed with status code: '{}'.", e.getStatusCode());
            LOG.warn("Failure body: {}", e.getResponseBodyAsString());
            throw new DefectDojoPersistenceException("Failed to attach findings to engagement.");
        }
    }


    public ImportScanResponse createFindingsForEngagementName(String engagementName, String rawResults, String defectDojoScanName, long productId, long lead){
        return getEngagementIdByEngagementNameOrCreate(engagementName, rawResults, defectDojoScanName, productId, lead, new EngagementPayload(), "", new LinkedMultiValueMap<>());
    }

    public ImportScanResponse getEngagementIdByEngagementNameOrCreate(String engagementName, String rawResults, String defectDojoScanName, long productId, long lead, EngagementPayload engagementPayload, String testName, MultiValueMap<String, Object> options){
        long engagementId = getEngagementIdByEngagementNameOrCreate(productId, engagementName, engagementPayload, lead);

        return createFindings(rawResults, engagementId, lead, currentDate(), defectDojoScanName, testName, options);
    }
    private long getEngagementIdByEngagementNameOrCreate(long productId, String engagementName, EngagementPayload engagementPayload, long lead) {
        Long engagementId = getEngagementIdByEngagementName(engagementName, productId).orElseGet(() -> {
            engagementPayload.setName(engagementName);
            engagementPayload.setProduct(productId);
            engagementPayload.setTargetStart(currentDate());
            engagementPayload.setTargetEnd(currentDate());
            engagementPayload.setLead(lead);
            return createEngagement(engagementPayload).getId();
        });
        return engagementId.longValue();
    }
    public ImportScanResponse createFindingsForEngagementName(String engagementName, String rawResults, String defectDojoScanName, String productName, long lead, EngagementPayload engagementPayload, String testName, MultiValueMap<String, Object> options) {
        return  createFindingsForEngagementName(engagementName, rawResults, defectDojoScanName, productName, lead, engagementPayload, testName,options, "Description missing", java.util.Collections.emptyList());
    }
    public ImportScanResponse createFindingsForEngagementName(String engagementName, String rawResults, String defectDojoScanName, String productName, long lead, EngagementPayload engagementPayload, String testName, MultiValueMap<String, Object> options, String productDescription, List<String> productTags) {
        long productId = retrieveOrCreateProduct(productName, productDescription, productTags);
        
        return getEngagementIdByEngagementNameOrCreate(engagementName, rawResults, defectDojoScanName, productId, lead, engagementPayload, testName, options);
    }

    private Optional<Long> getEngagementIdByEngagementName(String engagementName, String productName){
        long productId = retrieveProductId(productName);
        return getEngagementIdByEngagementName(engagementName, productId, 0L);
    }
    private Optional<Long> getEngagementIdByEngagementName(String engagementName, long productId){
        return getEngagementIdByEngagementName(engagementName, productId, 0L);
    }

    private Optional<Long> getEngagementIdByEngagementName(String engagementName, long productId, long offset){
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(defectDojoUrl + "/api/v2/engagements")
                .queryParam("product", Long.toString(productId))
                .queryParam("limit", Long.toString(50L))
                .queryParam("offset", Long.toString(offset));

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity engagementRequest = new HttpEntity(getHeaders());

        ResponseEntity<DefectDojoResponse<EngagementResponse>> engagementResponse = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, engagementRequest, new ParameterizedTypeReference<DefectDojoResponse<EngagementResponse>>(){});

        for(EngagementResponse engagement : engagementResponse.getBody().getResults()){
            if(engagement.getName().equals(engagementName)){
                return Optional.of(engagement.getId());
            }
        }
        if(engagementResponse.getBody().getNext() != null){
            return getEngagementIdByEngagementName(engagementName, productId, offset + 1);
        }
        LOG.warn("Engagement with name '{}' not found.", engagementName);
        return Optional.empty();
    }
    /**
     * @deprecated
     */
    public ProductResponse createProduct(String productName) {
        return createProduct(productName, "Description missing", java.util.Collections.emptyList());
    }
    public ProductResponse createProduct(String productName, String description, List<String> productTags) {
        RestTemplate restTemplate = new RestTemplate();
        ProductPayload productPayload = new ProductPayload(productName, description, productTags);
        HttpEntity<ProductPayload> payload = new HttpEntity<>(productPayload, getHeaders());

        try {
            ResponseEntity<ProductResponse> response = restTemplate.exchange(defectDojoUrl + "/api/v2/products/", HttpMethod.POST, payload, ProductResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            LOG.warn("Failed to create product {}", e);
            LOG.warn("Failure response body. {}", e.getResponseBodyAsString());
            throw new DefectDojoPersistenceException("Failed to create product", e);
        }
    }

    public void deleteUnusedBranches(List<String> existingBranches, String producName) {
        long productId = retrieveProductId(producName);
        deleteUnusedBranches(existingBranches, productId);
    } 

    /**
     * Deletes engagements based on branch tag
     * Be aware that the branch tag MUST be set, otherwise all engagments will be deleted
     */
    public void deleteUnusedBranches(List<String> existingBranches, long productId) {
        if(existingBranches == null) {
            LOG.error("No existing branches given, this will lead to nullpointer");
        }
        RestTemplate restTemplate = new RestTemplate();
        
        //get existing branches
        List<EngagementResponse> engagementPayloads = getEngagementsForProduct(productId, 0);
        for(EngagementResponse engagementPayload : engagementPayloads) {
            boolean branchExists = false;
            for(String existingBranchName : existingBranches) {
                if(existingBranchName.equals(engagementPayload.getBranch())) {
                    branchExists = true;
                    continue;
                }
            }
            if(!branchExists) {
                deleteEnageament(engagementPayload.getId());
                LOG.info("Deleted engagement with id " + engagementPayload.getId() + ", branch " + engagementPayload.getBranch());
            }
        }
    }

    private List<EngagementResponse> getEngagementsForProduct(long productId, long offset) throws DefectDojoLoopException {
        if(offset > 9999) {
            throw new DefectDojoLoopException("offset engagement products too much!");
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(defectDojoUrl + "/api/v2/engagements")
                .queryParam("product", Long.toString(productId))
                .queryParam("limit", Long.toString(50L))
                .queryParam("offset", Long.toString(offset));

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity engagementRequest = new HttpEntity(getHeaders());

        ResponseEntity<DefectDojoResponse<EngagementResponse>> engagementResponse = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, engagementRequest, new ParameterizedTypeReference<DefectDojoResponse<EngagementResponse>>(){});
        List<EngagementResponse> engagementPayloads = new LinkedList<EngagementResponse>();
        for(EngagementResponse engagement : engagementResponse.getBody().getResults()){
            engagementPayloads.add(engagement);
        }
        if(engagementResponse.getBody().getNext() != null){
            engagementPayloads.addAll(getEngagementsForProduct(productId, offset + 1));;
        }
        return engagementPayloads;
    }    
    public void deleteEnageament(long engagementId){
        RestTemplate restTemplate = new RestTemplate();

        String uri = defectDojoUrl + "/api/v2/engagements/" + engagementId + "/?id=" + engagementId;
        HttpEntity request = new HttpEntity(getHeaders());
        try {
            restTemplate.exchange(uri, HttpMethod.DELETE, request, DefectDojoResponse.class);
        } catch (HttpClientErrorException e) {
            LOG.warn("Failed to delete engagment {}, engagementId: " + engagementId, e);
            LOG.warn("Failure response body. {}", e.getResponseBodyAsString());
            throw new DefectDojoPersistenceException("Failed to delete product", e);
        }
    }

    /* options is created as follows:
        MultiValueMap<String, String> mvn = new LinkedMultiValueMap<>();
        mvn.add("engagement", Long.toString(engagementId));
     */
    private List<Finding> getCurrentFindings(long engagementId, LinkedMultiValueMap<String, String> options){
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(defectDojoUrl + "/api/v2/findings/")
                .queryParam("active", "true")
                .queryParam("false_p", "false")
                .queryParam("duplicate", "false")
                .queryParam("test__engagement", Long.toString(engagementId));

        if(options != null) {
            builder = prepareParameters(options, builder);
        }

        HttpEntity request = new HttpEntity(getHeaders());
        try {
            ResponseEntity<DefectDojoResponse<Finding>> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, request, new ParameterizedTypeReference<DefectDojoResponse<Finding>>(){});
            List<Finding> findings = new LinkedList<Finding>();
            for(Finding finding : response.getBody().getResults()){
                findings.add(finding);
            }
            return findings;
        } catch (HttpClientErrorException e) {
            LOG.warn("Failed to get findings {}, engagementId: " + engagementId, e);
            LOG.warn("Failure response body. {}", e.getResponseBodyAsString());
            throw new DefectDojoPersistenceException("Failed to get findings", e);
        }
    }
    private UriComponentsBuilder prepareParameters(LinkedMultiValueMap<String, String> queryParameters, UriComponentsBuilder builder) {
        Iterator<String> it = queryParameters.keySet().iterator();
     
        while(it.hasNext()){
            String theKey = (String)it.next();
            builder.replaceQueryParam(theKey, queryParameters.getFirst(theKey));
        }
        return builder;
    }    

    public List<Finding> receiveNonHandledFindings(String productName, String engagementName, String minimumSeverity, LinkedMultiValueMap<String, String> options){
        List<Finding> findings = new LinkedList<>();
        Long engagementId = getEngagementIdByEngagementName(engagementName, productName).orElse(0L);
        for(String severity : Finding.getServeritiesAndHigherServerities(minimumSeverity)) {
            options.remove("severity");
            options.add("severity", severity);
            findings.addAll(getCurrentFindings(engagementId, options));
        }
        return findings;
    }
}

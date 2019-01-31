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

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;

@Component
@ConditionalOnProperty(name = "securecodebox.persistence.defectdojo.enabled", havingValue = "true")
public class DefectDojoService {
    @Value("${securecodebox.persistence.defectdojo.url}")
    protected String defectDojoUrl;

    @Value("${securecodebox.persistence.defectdojo.auth.key}")
    protected String defectDojoApiKey;

    @Value("${securecodebox.persistence.defectdojo.auth.name}")
    protected String defectDojoDefaultUserName;

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

    public ImportScanResponse createFindings(String rawResult, long engagementId, long lead, String currentDate,String defectDojoScanName) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        restTemplate.setMessageConverters(Arrays.asList(new FormHttpMessageConverter(), new ResourceHttpMessageConverter(), new MappingJackson2HttpMessageConverter()));

        MultiValueMap<String, Object> mvn = new LinkedMultiValueMap<>();
        mvn.add("engagement", Long.toString(engagementId));
        mvn.add("lead", Long.toString(lead));
        mvn.add("scan_date", currentDate);
        mvn.add("scan_type", defectDojoScanName);

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

}

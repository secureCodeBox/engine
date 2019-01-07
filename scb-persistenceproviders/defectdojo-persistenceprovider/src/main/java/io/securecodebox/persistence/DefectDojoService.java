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
import io.securecodebox.persistence.models.DefectDojoResponse;
import io.securecodebox.persistence.models.DefectDojoUser;
import io.securecodebox.persistence.models.EngagementPayload;
import io.securecodebox.persistence.models.EngagementResponse;
import io.securecodebox.persistence.models.ToolConfig;
import io.securecodebox.persistence.models.ToolType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.MessageFormat;

@Component
public class DefectDojoService {
    @Value("${securecodebox.persistence.defectdojo.baseurl}")
    protected String defectDojoUrl;

    @Value("${securecodebox.persistence.defectdojo.apikey}")
    protected String defectDojoApiKey;

    private static final Logger LOG = LoggerFactory.getLogger(DefectDojoService.class);


    private HttpHeaders getHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + defectDojoApiKey);
        return headers;
    }

    public DefectDojoResponse<ToolType> getToolTypeByName(String name){
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity toolTypeRequest = new HttpEntity(getHeaders());

        String uri = UriComponentsBuilder.fromHttpUrl(defectDojoUrl + "/api/v2/tool_types/").queryParam("name", name).toUriString();
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

    public String getUserUrl(String username){
        RestTemplate restTemplate = new RestTemplate();

        if(username == null){
            username = "admin";
        }

        String uri = UriComponentsBuilder.fromHttpUrl(defectDojoUrl + "/api/v2/users/").queryParam("username", username).toUriString();
        HttpEntity userRequest = new HttpEntity(getHeaders());
        ResponseEntity<DefectDojoResponse<DefectDojoUser>> userResponse = restTemplate.exchange(uri, HttpMethod.GET, userRequest, new ParameterizedTypeReference<DefectDojoResponse<DefectDojoUser>>(){});
        if(userResponse.getBody().getCount() == 1){
            return userResponse.getBody().getResults().get(0).getUrl();
        }
        else {
            throw new DefectDojoUserNotFound(MessageFormat.format("Could not find user: \"{0}\" in DefectDojo", username));
        }
    }

    public String getToolConfiguration(String toolUrl, String toolType){
        RestTemplate restTemplate = new RestTemplate();

        if (toolUrl == null){
            return null;
        }

        String uri = UriComponentsBuilder.fromHttpUrl(defectDojoUrl + "/api/v2/tool_configurations/").queryParam("url", toolUrl).toUriString();
        HttpEntity toolRequest = new HttpEntity(getHeaders());
        ResponseEntity<DefectDojoResponse<ToolConfig>> toolResponse = restTemplate.exchange(uri, HttpMethod.GET, toolRequest, new ParameterizedTypeReference<DefectDojoResponse<ToolConfig>>(){});
        if(toolResponse.getBody().getCount() > 0){
            return toolResponse.getBody().getResults().get(0).getUrl();
        }
        else {
            HttpEntity toolTypeRequest = new HttpEntity(getHeaders());
            String toolTypeRequestUri = UriComponentsBuilder.fromHttpUrl(defectDojoUrl + "/api/v2/tool_types/").queryParam("name", toolType).toUriString();
            ResponseEntity<DefectDojoResponse<ToolType>> toolTypeResponse = restTemplate.exchange(toolTypeRequestUri, HttpMethod.GET, toolTypeRequest, new ParameterizedTypeReference<DefectDojoResponse<ToolType>>(){});
            String toolTypeUri = toolTypeResponse.getBody().getResults().get(0).getUrl();

            ToolConfig toolConfig = new ToolConfig();
            toolConfig.setName(toolUrl);
            toolConfig.setToolType(toolTypeUri);
            toolConfig.setConfigUrl(toolUrl);
            toolConfig.setDescription(toolType);

            HttpEntity<ToolConfig> toolPayload = new HttpEntity<>(toolConfig, getHeaders());
            restTemplate.exchange(defectDojoUrl + "/api/v2/tool_configurations/", HttpMethod.POST, toolPayload, ToolConfig.class);
            return getToolConfiguration(toolUrl, toolType);

        }
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

}

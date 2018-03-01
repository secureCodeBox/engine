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

package io.securecodebox.sdk;

import org.camunda.bpm.engine.variable.value.FileValue;

import java.io.File;
import java.util.Map;
import java.util.UUID;

/**
 * @author Robert Seedorff - iteratec GmbH
 * @since 27.09.15
 */
public interface ScanProcessExecution {

    /**
     * @return the context
     */
     String getContext();

    /**
     * @param the context to set
     */
     void setContext(String context);

    /**
     * @return the authentication
     */
     boolean getAuthentication();

    /**
     * @param the authentication to set
     */
     void setAuthentication(boolean authentication);

    /**
     * @return the loginSite
     */
     String getLoginSite();

    /**
     * @param the loginSite to set
     */
     void setLoginSite(String loginSite);

    /**
     * @return the loginUser name
     */
     String getLoginUserName();

    /**
     * @param the loginUser to set
     */
     void setLoginUserName(String loginUser);

    /**
     * @return the loginPassword
     */
     String getLoginPassword();

    /**
     * @param the loginPassword to set
     */
     void setLoginPassword(String loginPassword);

    /**
     * @return the passwordFieldId
     */
     String getPasswordFieldId();

    /**
     * @param the passwordFieldId to set
     */
     void setPasswordFieldId(String passwordFieldId);

    /**
     * @return the usernameFieldId
     */
     String getUsernameFieldId();

    /**
     * @param the usernameFieldId to set
     */
     void setUsernameFieldId(String usernameFieldId);

    /**
     * @return the logged in indicator
     */
     String getLoggedInIndicator();

    /**
     * @param the logged in indicator to set
     */
     void setLoggedInIndicator(String loggedInIndicator);

    /**
     * @return the logged out indicator
     */
     String getLoggedOutIndicator();

    /**
     * @param the logged out indicator to set
     */
     void setLoggedOutIndicator(String loggedOutIndicator);

    /**
     * @return the spiderType
     */
     String getSpiderType();

    /**
     * @param the spiderType to set
     */
     void setSpiderType(String spiderType);

    /**
     * URL wrapper for "spiderApiSpecFile". It's used in e.g. Zap Scanner as transferring the file itself wasn't possible.
     */
    String getSpiderApiSpecUrl();

    /**
     * Readonly URL wrapper for "spiderApiSpecFile". Its value is actually set in {@link de.iteratec.securebox.engine.listener.appscan.ConfigureSpiderDetailsCompleteListener}.
     */
    void setSpiderApiSpecUrl(String url);

    FileValue getSpiderApiSpecFile();

    void setSpiderApiSpecFile(File fileValue);

    /**
     * @return the spiderIncludeRegexes
     */
     String getSpiderIncludeRegexes();

    /**
     * @param the spiderIncludeRegexes to set
     */
     void setSpiderIncludeRegexes(String spiderIncludeRegexes);

    /**
     * @return the spiderExcludeRegexes
     */
     String getSpiderExcludeRegexes();

    /**
     * @param the spiderExcludeRegexes to set
     */
     void setSpiderExcludeRegexes(String spiderExcludeRegexes);

    /**
     * @return the spiderExcludeDuplicates
     */
     String getSpiderExcludeDuplicates();

    /**
     * @param the spiderExcludeDuplicates to set
     */
     void setSpiderExcludeDuplicates(String spiderExcludeDuplicates);

    /**
     * @return the spiderMaxDepth
     */
     String getSpiderMaxDepth();

    /**
     * @param the spiderMaxDepth to set
     */
     void setSpiderMaxDepth(String spiderMaxDepth);

    /**
     * @return the spiderUuid
     */
     UUID getSpiderUuid();

    /**
     * @param spiderUuid the spiderUuid to set
     */
     void setSpiderUuid(UUID spiderUuid);

    /**
     * @return the spiderMicroserviceRunId
     */
     String getSpiderMicroserviceRunId();

    /**
     * @param spiderMicroserviceRunId the spiderMicroserviceRunId to set
     */
     void setSpiderMicroserviceRunId(String spiderMicroserviceRunId);

    /**
     * @return the spiderMicroserviceId
     */
     String getSpiderMicroserviceId();

    /**
     * @param spiderMicroserviceId the spiderMicroserviceId to set
     */
     void setSpiderMicroserviceId(String spiderMicroserviceId);

    /**
     * @return the scannerType
     */
     String getScannerType();

    /**
     * @param scannerType the scannerType to set
     */
     void setScannerType(String scannerType);

    /**
     * @return the scannerIncludeRegexes
     */
     String getScannerIncludeRegexes();

    /**
     * @param the scannerIncludeRegexes to set
     */
     void setScannerIncludeRegexes(String scannerIncludeRegexes);

    /**
     * @return the scannerExcludeRegexes
     */
     String getScannerExcludeRegexes();

    /**
     * @param the scannerExcludeRegexes to set
     */
     void setScannerExcludeRegexes(String scannerExcludeRegexes);

    /**
     * @return the scannerUuid
     */
     UUID getScannerUuid();

    /**
     * @param scannerUuid the scannerUuid to set
     */
     void setScannerUuid(UUID scannerUuid);

    /**
     * @return the scanMicroserviceRunId
     */
     String getScannerMicroserviceRunId();

    /**
     * @param scannerMicroserviceRunId the scannerMicroserviceRunId to set
     */
     void setScannerMicroserviceRunId(String scannerMicroserviceRunId);

    /**
     * @return the scanMicroserviceId
     */
     String getScannerMicroserviceId();

    /**
     * @param scannerMicroserviceId the scannerMicroserviceId to set
     */
     void setScannerMicroserviceId(String scannerMicroserviceId);

    /**
     * @return the reportingTypes
     */
     String[] getReportingTypes();

    /**
     * @param the reportingType to set
     */
     void setReportingTypes(String[] reportingTypes);

    /**
     * @return the reportingApiKey
     */
     String getReportingApiKey();

    /**
     * @param the reportingApiKey to set
     */
     void setReportingApiKey(String apiKey);

    /**
     * @return the reportingAppId
     */
     String getReportingAppId();

    /**
     * @param the reportingAppId to set
     */
     void setReportingAppId(String appId);

    /**
     * @return the reportingMicroserviceRunId
     */
     String getReportingMicroserviceRunId();

    /**
     * @param reportingMicroserviceRun the reportingMicroserviceRun to set
     */
     void setReportingMicroserviceRunId(String reportingMicroserviceRun);

    /**
     * @return the reportingUuid
     */
     UUID getReportingUuid();

    /**
     * @return the reportingServiceHost
     */
     String getReportingServiceHost();

    /**
     * @param reportingServiceHost the reportingServiceHost
     */
     void setReportingServiceHost(String reportingServiceHost);

    /**
     * @return the reportingDashboardUrl
     */
     String getReportingDashboardUrl();

    /**
     * @param reportingDashboardUrl the reportingDashboardUrl
     */
     void setReportingDashboardUrl(String reportingDashboardUrl);

    /**
     * @param reportingUuid the reportingUuid to set
     */
     void setReportingUuid(UUID reportingUuid);

    /**
     * @return the reportingMicroserviceId
     */
     String getReportingMicroserviceId();

    /**
     * @param reportingMicroservicId the reportingMicroservicId to set
     */
     void setReportingMicroserviceId(String reportingMicroservicId);

    /**
     * @return the reportingFalsePositive
     */
     String getReportingFalsePositives();

    /**
     * @param reportingFalsePositive the reportingFalsePositive to set
     */
     void setReportingFalsePositives(String reportingFalsePositive);

     String getReportingUrl();

     void setReportingUrl(String reportingUrl);

    /**
     * @return the isRunning
     */
     boolean isRunning();

    /**
     * @return the uuid
     */
     UUID getProcessUuid();

    /**
     * @param uuid the uuid to set
     */
     void setProcessUuid(UUID uuid);

    /**
     * @return the targetUrl
     */
     String getTargetUrl();

    /**
     * @return the portScannerParameter
     */
     String getNmapParameter();

    /**
     * @return the portScannerTarget
     */
     String getPortScannerTarget();

    /**
     * @return the spiderTargetUrl
     */
     String getSpiderTargetUrl();

    /**
     * @param portScannerParameter the portScannerParameter
     */
     void setNmapParameter(String portScannerParameter);

    /**
     * @param portScannerTarget the portScannerTarget
     */
     void setPortScannerTarget(String portScannerTarget);

    /**
     * @param spiderTargetUrl the spiderTargetUrl
     */
     void setSpiderTargetUrl(String spiderTargetUrl);

    /**
     * @return the targetUrl
     */
     String getScannerTargetUrl();

    /**
     * @param scannerTargetUrl the scannerTargetUrl
     */
     void setScannerTargetUrl(String scannerTargetUrl);

    /**
     * @return the targetName
     */
     String getTargetName();

    /**
     * Contains all internal fields as JSON Object
     *
     * @return
     */
     String toJsonString();

    /**
     * @return the type of client who invoked the scan (automated scans only)
     */
     String getClientType();

    /**
     * @param clientType who invoked scan
     */
     void setClientType(String clientType);

    /**
     * @return link to jenkins job which the scan results are reported to (in direct mode)
     */
     String getJenkinsJobLink();

    /**
     * @param jenkinsJobLink for returning scan results (in direct mode)
     */
     void setJenkinsJobLink(String jenkinsJobLink);

    /**
     * @return correct parser for the report needed or null
     */
     String getReportingParser();

    /**
     * @param parser for scanning results and formatting them
     */
     void setReportingParser(String reportingParser);

    /**
     * @return max risk level acceptable for vulnerabilities
     */
    String getMaxRiskLevel();

    void setMaxRiskLevel(String risk);

    /**
     * @return how often a threat of the maxRiskLevel is allowed to occur within a scan before it breaks.
     */
     int getMaxVulnerabilityCount();

     void setMaxVulnerabilityCount(int count);

    /**
     * @return basic auth login data for jenkins encoded in base64 (only needed for direct reporting type!)
     */
     String getJenkinsLoginData();

     void setJenkinsLoginData(String jenkinsLoginData);

    /**
     * @return specifies whether or not camunda should directly push the results back to jenkins via an external job or just persist them in consul KV store
     */
     String getJenkinsReportingType();

     void setJenkinsReportingType(String jenkinsReportingType);

    /**
     * @return alternative dashboard url
     */
     String getAlternateDashboardUrl();

     void setAlternateDashboardUrl(String url);

     String getTenantId();

     void setTenantId(String tenantId);

     String getCSRFTokenId();

     void setCSRFTokenId(String tokenId);

    String getCSRFAuthPayload();

    void setCSRFAuthPayload(String payload);

     Map<String, Object> toMap();

}

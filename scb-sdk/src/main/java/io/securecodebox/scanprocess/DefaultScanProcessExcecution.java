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
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.IntegerValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/**
 * @since 01.09.15
 */
public class DefaultScanProcessExcecution implements ScanProcessExecution {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultScanProcessExcecution.class);

    public static final String CLIENT_TYPE = "clientType";
    public static final String CONTEXT = CommonConstants.CONTEXT;
    public static final String JENKINS_JOB_LINK = "jenkinsJobLink";
    public static final String JENKINS_LOGIN_DATA = "jenkinsLoginData";
    public static final String JENKINS_REPORTING_TYPE = "jenkinsReportingType";
    public static final String LOGGED_IN_INDICATOR = "loggedInIndicator";
    public static final String LOGGED_OUT_INDICATOR = "loggedOutIndicator";
    public static final String LOGIN_PASSWORD = "loginPassword";
    public static final String LOGIN_SITE = "loginSite";
    public static final String LOGIN_USER = "loginUser";
    public static final String MAX_RISK = "maxRisk";
    public static final String MAX_VULNERABILITY_COUNT = "maxVulnerabilityCount";
    public static final String PASSWORD_FIELD_ID = "passwordFieldId";
    public static final String ALTERNATE_DASHBOARD_URL = "alternateDashboardUrl";
    public static final String REPORTING_API_KEY = "reportingApiKey";
    public static final String REPORTING_APP_ID = "reportingAppId";
    public static final String REPORTING_DASHBOARD_URL = "reportingDashboardUrl";
    public static final String REPORTING_FALSE_POSITIVES = "reportingFalsePositives";
    public static final String REPORTING_MICROSERVICE_ID = "reportingMicroserviceId";
    public static final String REPORTING_MICROSERVICE_RUN_ID = "reportingMicroserviceRunId";
    public static final String REPORTING_PARSER = "reportingParser";
    public static final String REPORTING_SERVICE_HOST = "reportingServiceHost";
    public static final String REPORTING_URL = "reportingUrl";
    public static final String REPORTING_UUID = "reportingUuid";
    public static final String SCANNER_EXCLUDE_REGEXES = CommonConstants.SCANNER_REGEX_EXCLUDE;
    public static final String SCANNER_INCLUDE_REGEXES = CommonConstants.SCANNER_REGEX_INCLUDE;
    public static final String SCANNER_MICROSERVICE_ID = CommonConstants.SCANNER_MICROSERVICE_ID;
    public static final String SCANNER_MICROSERVICE_RUN_ID = CommonConstants.SCANNER_RUN_ID;
    public static final String SCANNER_TARGET_URL = CommonConstants.SCANNER_TARGET;
    public static final String SCANNER_TYPE = CommonConstants.SCANNER_MICROSERVICE_TYPE;
    public static final String SCANNER_UUID = "scannerUuid";
    public static final String SPIDER_EXCLUDE_DUPLICATES = "spiderExcludeDuplicates";
    public static final String SPIDER_API_SPEC_URL = "spiderApiSpecUrl";
    public static final String SPIDER_API_SPEC_FILE = "spiderApiSpecFile";
    public static final String SPIDER_EXCLUDE_REGEXES = "spiderExcludeRegexes";
    public static final String SPIDER_INCLUDE_REGEXES = "spiderIncludeRegexes";
    public static final String SPIDER_MAX_DEPTH = "spiderMaxDepth";
    public static final String SPIDER_MICROSERVICE_ID = CommonConstants.SPIDER_MICROSERVICE_ID;
    public static final String SPIDER_MICROSERVICE_RUN_ID = CommonConstants.SPIDER_MICROSERVICE_RUN_ID;
    public static final String SPIDER_TARGET_URL = CommonConstants.SPIDER_TARGET;
    public static final String SPIDER_TYPE = CommonConstants.SPIDER_TYPE;
    public static final String SPIDER_UUID = "spiderUuid";
    public static final String TARGET_NAME = "targetName";
    public static final String TARGET_URL = "targetUrl";
    public static final String USERNAME_FIELD_ID = "usernameFieldId";
    public static final String PROCESS_UUID = CommonConstants.PROCESS_UUID;
    public static final String AUTHENTICATION = "authentication";
    public static final String TENANT_ID = CommonConstants.TENAND_ID;
    public static final String CSRF_TOKEN_ID = "csrfTokenId";
    public static final String CSRF_AUTH_PAYLOAD = "csrfAuthPayload";

    protected DelegateExecution execution;

    /**
     * @param execution
     */
    DefaultScanProcessExcecution(DelegateExecution execution) {
        this.execution = execution;
    }

    @Override
    public boolean getAuthentication() {
        return execution.<BooleanValue>getVariableTyped(AUTHENTICATION).getValue();
    }

    @Override
    public String getClientType() {
        return getStringOrEmpty(this.execution.getVariableTyped(CLIENT_TYPE));
    }

    @Override
    public String getContext() {
        return getStringOrEmpty(this.execution.getVariableTyped(CONTEXT));
    }

    @Override
    public String getJenkinsJobLink() {
        return getStringOrEmpty(this.execution.getVariableTyped(JENKINS_JOB_LINK));
    }

    @Override
    public String getJenkinsLoginData() {
        return getStringOrEmpty(this.execution.getVariableTyped(JENKINS_LOGIN_DATA));
    }

    @Override
    public String getJenkinsReportingType() {
        return getStringOrEmpty(this.execution.getVariableTyped(JENKINS_REPORTING_TYPE));
    }

    @Override
    public String getLoggedInIndicator() {
        return getStringOrEmpty(this.execution.getVariableTyped(LOGGED_IN_INDICATOR));
    }

    @Override
    public String getLoggedOutIndicator() {
        return getStringOrEmpty(this.execution.getVariableTyped(LOGGED_OUT_INDICATOR));
    }

    @Override
    public String getLoginPassword() {
        return getStringOrEmpty(this.execution.getVariableTyped(LOGIN_PASSWORD));
    }

    @Override
    public String getLoginSite() {
        return getStringOrEmpty(this.execution.getVariableTyped(LOGIN_SITE));
    }

    @Override
    public String getLoginUserName() {
        return getStringOrEmpty(this.execution.getVariableTyped(LOGIN_USER));
    }

    public String getMaxRiskLevel() {
        return getStringOrEmpty(this.execution.getVariableTyped(MAX_RISK));
    }

    public int getMaxVulnerabilityCount() {
        return this.execution.<IntegerValue>getVariableTyped(MAX_VULNERABILITY_COUNT).getValue();
    }

    @Override
    public String getPasswordFieldId() {
        return getStringOrEmpty(this.execution.getVariableTyped(PASSWORD_FIELD_ID));
    }

    @Override
    public String getAlternateDashboardUrl() {
        return getStringOrEmpty(this.execution.getVariableTyped(ALTERNATE_DASHBOARD_URL));
    }

    @Override
    public String getReportingApiKey() {
        return getStringOrEmpty(this.execution.getVariableTyped(REPORTING_API_KEY));
    }

    @Override
    public String getReportingAppId() {
        return getStringOrEmpty(this.execution.getVariableTyped(REPORTING_APP_ID));
    }

    @Override
    public String getReportingDashboardUrl() {
        return getStringOrEmpty(this.execution.getVariableTyped(REPORTING_DASHBOARD_URL));
    }

    @Override
    public String getReportingFalsePositives() {
        return getStringOrEmpty(this.execution.getVariableTyped(REPORTING_FALSE_POSITIVES));
    }

    @Override
    public String getReportingMicroserviceId() {
        return getStringOrEmpty(this.execution.getVariableTyped(REPORTING_MICROSERVICE_ID));
    }

    @Override
    public String getReportingMicroserviceRunId() {
        return getStringOrEmpty(this.execution.getVariableTyped(REPORTING_MICROSERVICE_RUN_ID));
    }

    @Override
    public String getReportingParser() {
        return getStringOrEmpty(this.execution.getVariableTyped(REPORTING_PARSER));
    }

    @Override
    public String getReportingServiceHost() {
        return getStringOrEmpty(this.execution.getVariableTyped(REPORTING_SERVICE_HOST));
    }

    @Override
    public String[] getReportingTypes() {
        //        String types = this.execution.getVariable("reportingTypes") if (types == null) {
        //            return new String[0]
        //        } String[] reportingTypes = types.substring(1, types.length() - 1).split(",")
        //        reportingTypes = reportingTypes.collect {
        //            it.trim()
        //        }
        //
        //        return reportingTypes;
        return new String[0];
    }

    @Override
    public String getReportingUrl() {
        return getStringOrEmpty(this.execution.getVariableTyped(REPORTING_URL));
    }

    @Override
    public UUID getReportingUuid() {
        UUID returnValue = convertToUuidOrGenerateRandom(this.execution.getVariable(REPORTING_UUID));

        setReportingUuid(returnValue);

        return returnValue;
    }

    @Override
    public String getScannerExcludeRegexes() {
        return getStringOrEmpty(this.execution.getVariableTyped(SCANNER_EXCLUDE_REGEXES));
    }

    @Override
    public String getScannerIncludeRegexes() {
        return getStringOrEmpty(this.execution.getVariableTyped(SCANNER_INCLUDE_REGEXES));
    }

    @Override
    public String getScannerMicroserviceId() {
        return getStringOrEmpty(this.execution.getVariableTyped(SCANNER_MICROSERVICE_ID));
    }

    @Override
    public String getScannerMicroserviceRunId() {
        return getStringOrEmpty(this.execution.getVariableTyped(SCANNER_MICROSERVICE_RUN_ID));
    }

    @Override
    public String getScannerTargetUrl() {
        return getStringOrEmpty(this.execution.getVariableTyped(SCANNER_TARGET_URL));
    }

    @Override
    public String getScannerType() {
        return getStringOrEmpty(this.execution.getVariableTyped(SCANNER_TYPE));
    }

    @Override
    public UUID getScannerUuid() {
        UUID returnValue = convertToUuidOrGenerateRandom(this.execution.getVariable(SCANNER_UUID));

        setScannerUuid(returnValue);

        return returnValue;
    }

    @Override
    public String getSpiderExcludeDuplicates() {
        return getStringOrEmpty(this.execution.getVariableTyped(SPIDER_EXCLUDE_DUPLICATES));
    }

    @Override
    public String getSpiderApiSpecUrl() {
        return getStringOrEmpty(this.execution.getVariableTyped(SPIDER_API_SPEC_URL));
    }

    @Override
    public FileValue getSpiderApiSpecFile() {
        return this.execution.getVariableTyped(SPIDER_API_SPEC_FILE);
    }

    @Override
    public String getSpiderExcludeRegexes() {
        return getStringOrEmpty(this.execution.getVariableTyped(SPIDER_EXCLUDE_REGEXES));
    }

    @Override
    public String getSpiderIncludeRegexes() {
        return getStringOrEmpty(this.execution.getVariableTyped(SPIDER_INCLUDE_REGEXES));
    }

    @Override
    public String getSpiderMaxDepth() {
        return getStringOrEmpty(this.execution.getVariableTyped(SPIDER_MAX_DEPTH));
    }

    @Override
    public String getSpiderMicroserviceId() {
        return getStringOrEmpty(this.execution.getVariableTyped(SPIDER_MICROSERVICE_ID));
    }

    @Override
    public String getSpiderMicroserviceRunId() {
        return getStringOrEmpty(this.execution.getVariableTyped(SPIDER_MICROSERVICE_RUN_ID));
    }

    @Override
    public String getSpiderTargetUrl() {
        return getStringOrEmpty(this.execution.getVariableTyped(SPIDER_TARGET_URL));
    }

    @Override
    public String getSpiderType() {
        return getStringOrEmpty(this.execution.getVariableTyped(SPIDER_TYPE));
    }

    @Override
    public UUID getSpiderUuid() {
        UUID returnValue = convertToUuidOrGenerateRandom(this.execution.getVariable(SPIDER_UUID));

        setSpiderUuid(returnValue);

        return returnValue;
    }

    @Override
    public String getTargetName() {
        return getStringOrEmpty(this.execution.getVariableTyped(TARGET_NAME));
    }

    @Override
    public String getTargetUrl() {
        return getStringOrEmpty(this.execution.getVariableTyped(TARGET_URL));
    }

    @Override
    public String getUsernameFieldId() {
        return getStringOrEmpty(this.execution.getVariableTyped(USERNAME_FIELD_ID));
    }

    @Override
    public UUID getProcessUuid() {
        UUID returnValue = convertToUuidOrGenerateRandom(this.execution.getVariable(PROCESS_UUID));

        setProcessUuid(returnValue);

        return returnValue;
    }

    @Override
    public String getTenantId() {
        return getStringOrEmpty(this.execution.getVariableTyped(TENANT_ID));
    }

    @Override
    public String getCSRFTokenId() {
        return getStringOrEmpty(this.execution.getVariableTyped(CSRF_TOKEN_ID));
    }

    @Override
    public String getCSRFAuthPayload() {
        return getStringOrEmpty(this.execution.getVariableTyped(CSRF_AUTH_PAYLOAD));
    }

    @Override
    public boolean isRunning() {
        return !this.execution.isCanceled();
    }

    @Override
    public void setAuthentication(boolean authentication) {
        this.execution.setVariable(AUTHENTICATION, authentication);
    }

    @Override
    public void setClientType(String clientType) {
        this.execution.setVariable(DefaultScanProcessExcecution.CLIENT_TYPE, clientType);
    }

    @Override
    public void setContext(String context) {
        this.execution.setVariable(CONTEXT, context);
    }

    @Override
    public void setJenkinsJobLink(String jenkinsJobLink) {
        this.execution.setVariable(JENKINS_JOB_LINK, jenkinsJobLink);
    }

    @Override
    public void setJenkinsLoginData(String jenkinsLoginData) {
        this.execution.setVariable(JENKINS_LOGIN_DATA, jenkinsLoginData);
    }

    @Override
    public void setJenkinsReportingType(String jenkinsReportingType) {
        this.execution.setVariable(JENKINS_REPORTING_TYPE, jenkinsReportingType);
    }

    @Override
    public void setLoggedInIndicator(String loggedInIndicator) {
        this.execution.setVariable(LOGGED_IN_INDICATOR, loggedInIndicator);
    }

    @Override
    public void setLoggedOutIndicator(String loggedOutIndicator) {
        this.execution.setVariable(LOGGED_OUT_INDICATOR, loggedOutIndicator);
    }

    @Override
    public void setLoginPassword(String loginPassword) {
        this.execution.setVariable(LOGIN_PASSWORD, loginPassword);
    }

    @Override
    public void setLoginSite(String loginSite) {
        this.execution.setVariable(LOGIN_SITE, loginSite);
    }

    @Override
    public void setLoginUserName(String loginUser) {
        this.execution.setVariable(LOGIN_USER, loginUser);
    }

    public void setMaxRiskLevel(String risk) {
        this.execution.setVariable(MAX_RISK, risk);
    }

    public void setMaxVulnerabilityCount(int count) {
        this.execution.setVariable(MAX_VULNERABILITY_COUNT, count);
    }

    @Override
    public void setPasswordFieldId(String passwordFieldId) {
        this.execution.setVariable(PASSWORD_FIELD_ID, passwordFieldId);
    }

    @Override
    public void setAlternateDashboardUrl(String url) {
        this.execution.setVariable(ALTERNATE_DASHBOARD_URL, url);
    }

    @Override
    public void setReportingApiKey(String apiKey) {
        this.execution.setVariable(REPORTING_API_KEY, apiKey);
    }

    @Override
    public void setReportingAppId(String appId) {
        this.execution.setVariable(REPORTING_APP_ID, appId);
    }

    @Override
    public void setReportingDashboardUrl(String reportingDashboardUrl) {
        this.execution.setVariable(REPORTING_DASHBOARD_URL, reportingDashboardUrl);
    }

    @Override
    public void setReportingFalsePositives(String reportingFalsePositives) {
        this.execution.setVariable(REPORTING_FALSE_POSITIVES, reportingFalsePositives);
    }

    @Override
    public void setReportingMicroserviceId(String reportingMicroservicId) {
        this.execution.setVariable(REPORTING_MICROSERVICE_ID, reportingMicroservicId);
    }

    @Override
    public void setReportingMicroserviceRunId(String reportingMicroserviceRunId) {
        this.execution.setVariable(REPORTING_MICROSERVICE_RUN_ID, reportingMicroserviceRunId);
    }

    @Override
    public void setReportingParser(String reportingParser) {
        this.execution.setVariable(REPORTING_PARSER, reportingParser);
    }

    @Override
    public void setReportingServiceHost(String reportingServiceHost) {
        this.execution.setVariable(REPORTING_SERVICE_HOST, reportingServiceHost);
    }

    @Override
    public void setReportingTypes(String[] reportingTypes) {
        this.execution.setVariable("reportingTypes", Arrays.toString(reportingTypes));
    }

    @Override
    public void setReportingUrl(String reportingUrl) {
        this.execution.setVariable(REPORTING_URL, reportingUrl);
    }

    @Override
    public void setReportingUuid(UUID reportingUuid) {
        this.execution.setVariable(REPORTING_UUID, reportingUuid);
    }

    @Override
    public void setScannerExcludeRegexes(String scannerExcludeRegexes) {
        this.execution.setVariable(SCANNER_EXCLUDE_REGEXES, scannerExcludeRegexes);
    }

    @Override
    public void setScannerIncludeRegexes(String scannerIncludeRegexes) {
        this.execution.setVariable(SCANNER_INCLUDE_REGEXES, scannerIncludeRegexes);
    }

    @Override
    public void setScannerMicroserviceId(String scannerMicroserviceId) {
        this.execution.setVariable(SCANNER_MICROSERVICE_ID, scannerMicroserviceId);
    }

    @Override
    public void setScannerMicroserviceRunId(String scannerMicroserviceRunId) {
        this.execution.setVariable(SCANNER_MICROSERVICE_RUN_ID, scannerMicroserviceRunId);
    }

    @Override
    public void setScannerTargetUrl(String scannerTargetUrl) {
        this.execution.setVariable(SCANNER_TARGET_URL, scannerTargetUrl);
    }

    @Override
    public void setScannerType(String scannerType) {
        this.execution.setVariable(SCANNER_TYPE, scannerType);
    }

    @Override
    public void setScannerUuid(UUID scannerUuid) {
        this.execution.setVariable(SCANNER_UUID, scannerUuid);
    }

    @Override
    public void setSpiderApiSpecUrl(String url) {
        this.execution.setVariable(SPIDER_API_SPEC_FILE, url);
    }

    @Override
    public void setSpiderApiSpecFile(File file) {
        this.execution.setVariable(SPIDER_API_SPEC_FILE, file);
    }

    @Override
    public void setSpiderExcludeDuplicates(String spiderExcludeDuplicates) {
        this.execution.setVariable(SPIDER_EXCLUDE_DUPLICATES, spiderExcludeDuplicates);
    }

    @Override
    public void setSpiderExcludeRegexes(String spiderExcludeRegexes) {
        this.execution.setVariable(SPIDER_EXCLUDE_REGEXES, spiderExcludeRegexes);
    }

    @Override
    public void setSpiderIncludeRegexes(String spiderIncludeRegexes) {
        this.execution.setVariable(SPIDER_INCLUDE_REGEXES, spiderIncludeRegexes);
    }

    @Override
    public void setSpiderMaxDepth(String spiderMaxDepth) {
        this.execution.setVariable(SPIDER_MAX_DEPTH, spiderMaxDepth);
    }

    @Override
    public void setSpiderMicroserviceId(String spiderMicroserviceId) {
        this.execution.setVariable(SPIDER_MICROSERVICE_ID, spiderMicroserviceId);
    }

    @Override
    public void setSpiderMicroserviceRunId(String spiderMicroserviceRunId) {
        this.execution.setVariable(SPIDER_MICROSERVICE_RUN_ID, spiderMicroserviceRunId);
    }

    @Override
    public void setSpiderTargetUrl(String spiderTargetUrl) {
        this.execution.setVariable(SPIDER_TARGET_URL, spiderTargetUrl);
    }

    @Override
    public void setSpiderType(String spiderType) {
        this.execution.setVariable(SPIDER_TYPE, spiderType);
    }

    @Override
    public void setSpiderUuid(UUID spiderUuid) {
        this.execution.setVariable(SPIDER_UUID, spiderUuid);
    }

    @Override
    public void setUsernameFieldId(String usernameFieldId) {
        this.execution.setVariable(USERNAME_FIELD_ID, usernameFieldId);
    }

    public void setProcessUuid(UUID uuid) {
        this.execution.setVariable(PROCESS_UUID, uuid);
    }

    @Override
    public void setTenantId(String tenantId) {
        this.execution.setVariable(TENANT_ID, tenantId);
    }

    @Override
    public void setCSRFTokenId(String tokenId) {
        this.execution.setVariable(CSRF_TOKEN_ID, tokenId);
    }

    @Override
    public void setCSRFAuthPayload(String payload) {
        this.execution.setVariable(CSRF_AUTH_PAYLOAD, payload);
    }

    @Override
    public String toJsonString() {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, Object> toMap() {
        return this.execution.getVariables();
    }

    protected String getStringOrEmpty(String o) {
        return o != null ? o : "";
    }

    protected UUID convertToUuidOrGenerateRandom(Object inputUuid) {
        try {
            return UUID.fromString(inputUuid.toString());
        } catch (IllegalArgumentException | NullPointerException e) {
            LOG.trace("The uuid seemed to be empty input: {}", inputUuid, e);
            return UUID.randomUUID();
        }
    }
}

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

import org.camunda.bpm.engine.variable.value.FileValue;

import java.io.File;
import java.util.Map;
import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 02.03.18
 */
public class DefaultScanProcessExecution implements ScanProcessExecution {

    protected String context;
    protected boolean authentication;
    protected String loginSite;
    protected String loginUserName;

    @Override
    public String getContext() {
        return context;
    }

    @Override
    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public boolean getAuthentication() {
        return authentication;
    }

    @Override
    public void setAuthentication(boolean authentication) {
        this.authentication = authentication;
    }

    @Override
    public String getLoginSite() {
        return loginSite;
    }

    @Override
    public void setLoginSite(String loginSite) {
        this.loginSite = loginSite;
    }

    @Override
    public String getLoginUserName() {
        return loginUserName;
    }

    @Override
    public void setLoginUserName(String loginUser) {
        this.loginUserName = loginUser;
    }

    @Override
    public String getLoginPassword() {
        return null;
    }

    @Override
    public void setLoginPassword(String loginPassword) {

    }

    @Override
    public String getPasswordFieldId() {
        return null;
    }

    @Override
    public void setPasswordFieldId(String passwordFieldId) {

    }

    @Override
    public String getUsernameFieldId() {
        return null;
    }

    @Override
    public void setUsernameFieldId(String usernameFieldId) {

    }

    @Override
    public String getLoggedInIndicator() {
        return null;
    }

    @Override
    public void setLoggedInIndicator(String loggedInIndicator) {

    }

    @Override
    public String getLoggedOutIndicator() {
        return null;
    }

    @Override
    public void setLoggedOutIndicator(String loggedOutIndicator) {

    }

    @Override
    public String getSpiderType() {
        return null;
    }

    @Override
    public void setSpiderType(String spiderType) {

    }

    @Override
    public String getSpiderApiSpecUrl() {
        return null;
    }

    @Override
    public void setSpiderApiSpecUrl(String url) {

    }

    @Override
    public FileValue getSpiderApiSpecFile() {
        return null;
    }

    @Override
    public void setSpiderApiSpecFile(File fileValue) {

    }

    @Override
    public String getSpiderIncludeRegexes() {
        return null;
    }

    @Override
    public void setSpiderIncludeRegexes(String spiderIncludeRegexes) {

    }

    @Override
    public String getSpiderExcludeRegexes() {
        return null;
    }

    @Override
    public void setSpiderExcludeRegexes(String spiderExcludeRegexes) {

    }

    @Override
    public String getSpiderExcludeDuplicates() {
        return null;
    }

    @Override
    public void setSpiderExcludeDuplicates(String spiderExcludeDuplicates) {

    }

    @Override
    public String getSpiderMaxDepth() {
        return null;
    }

    @Override
    public void setSpiderMaxDepth(String spiderMaxDepth) {

    }

    @Override
    public UUID getSpiderUuid() {
        return null;
    }

    @Override
    public void setSpiderUuid(UUID spiderUuid) {

    }

    @Override
    public String getSpiderMicroserviceRunId() {
        return null;
    }

    @Override
    public void setSpiderMicroserviceRunId(String spiderMicroserviceRunId) {

    }

    @Override
    public String getSpiderMicroserviceId() {
        return null;
    }

    @Override
    public void setSpiderMicroserviceId(String spiderMicroserviceId) {

    }

    @Override
    public String getScannerType() {
        return null;
    }

    @Override
    public void setScannerType(String scannerType) {

    }

    @Override
    public String getScannerIncludeRegexes() {
        return null;
    }

    @Override
    public void setScannerIncludeRegexes(String scannerIncludeRegexes) {

    }

    @Override
    public String getScannerExcludeRegexes() {
        return null;
    }

    @Override
    public void setScannerExcludeRegexes(String scannerExcludeRegexes) {

    }

    @Override
    public UUID getScannerUuid() {
        return null;
    }

    @Override
    public void setScannerUuid(UUID scannerUuid) {

    }

    @Override
    public String getScannerMicroserviceRunId() {
        return null;
    }

    @Override
    public void setScannerMicroserviceRunId(String scannerMicroserviceRunId) {

    }

    @Override
    public String getScannerMicroserviceId() {
        return null;
    }

    @Override
    public void setScannerMicroserviceId(String scannerMicroserviceId) {

    }

    @Override
    public String[] getReportingTypes() {
        return new String[0];
    }

    @Override
    public void setReportingTypes(String[] reportingTypes) {

    }

    @Override
    public String getReportingApiKey() {
        return null;
    }

    @Override
    public void setReportingApiKey(String apiKey) {

    }

    @Override
    public String getReportingAppId() {
        return null;
    }

    @Override
    public void setReportingAppId(String appId) {

    }

    @Override
    public String getReportingMicroserviceRunId() {
        return null;
    }

    @Override
    public void setReportingMicroserviceRunId(String reportingMicroserviceRun) {

    }

    @Override
    public UUID getReportingUuid() {
        return null;
    }

    @Override
    public String getReportingServiceHost() {
        return null;
    }

    @Override
    public void setReportingServiceHost(String reportingServiceHost) {

    }

    @Override
    public String getReportingDashboardUrl() {
        return null;
    }

    @Override
    public void setReportingDashboardUrl(String reportingDashboardUrl) {

    }

    @Override
    public void setReportingUuid(UUID reportingUuid) {

    }

    @Override
    public String getReportingMicroserviceId() {
        return null;
    }

    @Override
    public void setReportingMicroserviceId(String reportingMicroservicId) {

    }

    @Override
    public String getReportingFalsePositives() {
        return null;
    }

    @Override
    public void setReportingFalsePositives(String reportingFalsePositive) {

    }

    @Override
    public String getReportingUrl() {
        return null;
    }

    @Override
    public void setReportingUrl(String reportingUrl) {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public UUID getProcessUuid() {
        return null;
    }

    @Override
    public void setProcessUuid(UUID uuid) {

    }

    @Override
    public String getTargetUrl() {
        return null;
    }

    @Override
    public String getSpiderTargetUrl() {
        return null;
    }

    @Override
    public void setSpiderTargetUrl(String spiderTargetUrl) {

    }

    @Override
    public String getScannerTargetUrl() {
        return null;
    }

    @Override
    public void setScannerTargetUrl(String scannerTargetUrl) {

    }

    @Override
    public String getTargetName() {
        return null;
    }

    @Override
    public String toJsonString() {
        return null;
    }

    @Override
    public String getClientType() {
        return null;
    }

    @Override
    public void setClientType(String clientType) {

    }

    @Override
    public String getJenkinsJobLink() {
        return null;
    }

    @Override
    public void setJenkinsJobLink(String jenkinsJobLink) {

    }

    @Override
    public String getReportingParser() {
        return null;
    }

    @Override
    public void setReportingParser(String reportingParser) {

    }

    @Override
    public String getMaxRiskLevel() {
        return null;
    }

    @Override
    public void setMaxRiskLevel(String risk) {

    }

    @Override
    public int getMaxVulnerabilityCount() {
        return 0;
    }

    @Override
    public void setMaxVulnerabilityCount(int count) {

    }

    @Override
    public String getJenkinsLoginData() {
        return null;
    }

    @Override
    public void setJenkinsLoginData(String jenkinsLoginData) {

    }

    @Override
    public String getJenkinsReportingType() {
        return null;
    }

    @Override
    public void setJenkinsReportingType(String jenkinsReportingType) {

    }

    @Override
    public String getAlternateDashboardUrl() {
        return null;
    }

    @Override
    public void setAlternateDashboardUrl(String url) {

    }

    @Override
    public String getTenantId() {
        return null;
    }

    @Override
    public void setTenantId(String tenantId) {

    }

    @Override
    public String getCSRFTokenId() {
        return null;
    }

    @Override
    public void setCSRFTokenId(String tokenId) {

    }

    @Override
    public String getCSRFAuthPayload() {
        return null;
    }

    @Override
    public void setCSRFAuthPayload(String payload) {

    }

    @Override
    public Map<String, Object> toMap() {
        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}

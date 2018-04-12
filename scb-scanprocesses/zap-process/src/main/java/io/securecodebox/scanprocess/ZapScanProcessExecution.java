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

import io.securecodebox.constants.DefaultFields;
import io.securecodebox.constants.ZapFields;
import io.securecodebox.model.execution.DefaultScanProcessExecution;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.StringValue;

import java.util.Arrays;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 01.03.18
 */
public class ZapScanProcessExecution extends DefaultScanProcessExecution {

    /**
     * @param execution
     */
    public ZapScanProcessExecution(DelegateExecution execution) {
        super(execution);
    }


    public String getTargetUrl() {
        return execution.<StringValue>getVariableTyped(ZapFields.ZAP_TARGET_URL.name()).getValue();
    }

    public boolean getAuthentication() {
        return this.execution.<BooleanValue>getVariableTyped(ZapFields.ZAP_AUTHENTICATION.name()).getValue();
    }

    public void setLoginSite(String loginSite) {
        this.execution.setVariable("loginSite", loginSite);
    }

    public void setLoginUserName(String loginUser) {
        this.execution.setVariable("loginUser", loginUser);
    }

    public void setUsernameFieldId(String usernameFieldId) {
        this.execution.setVariable("usernameFieldId", usernameFieldId);
    }

    public void setLoginPassword(String loginPassword) {
        this.execution.setVariable("loginPassword", loginPassword);
    }

    public void setPasswordFieldId(String passwordFieldId) {
        this.execution.setVariable("passwordFieldId", passwordFieldId);
    }

    public void setLoggedInIndicator(String loggedInIndicator) {
        this.execution.setVariable("loggedInIndicator", loggedInIndicator);
    }

    public void setSpiderTargetUrl(String spiderTargetUrl) {
        this.execution.setVariable(ZapFields.ZAP_SPIDER_TARGET_URL.name(), spiderTargetUrl);
    }

    public void setSpiderType(String spiderType) {
        this.execution.setVariable(DefaultFields.PROCESS_SPIDER_TYPE.name(), spiderType);
    }

    public void setSpiderIncludeRegexes(String spiderIncludeRegexes) {
        this.execution.setVariable(ZapFields.ZAP_SPIDER_INCLUDE_REGEX.name(), spiderIncludeRegexes);
    }

    public void setSpiderExcludeRegexes(String spiderExcludeRegexes) {
        this.execution.setVariable(ZapFields.ZAP_SPIDER_EXCLUDE_REGEX.name(), spiderExcludeRegexes);
    }

    public void setSpiderExcludeDuplicates(String spiderExcludeDuplicates) {
        this.execution.setVariable(ZapFields.ZAP_SPIDER_EXLUDE_DUPLICATES.name(), spiderExcludeDuplicates);
    }

    public void setSpiderMaxDepth(String spiderMaxDepth) {
        this.execution.setVariable(ZapFields.ZAP_SPIDER_MAX_DEPTH.name(), spiderMaxDepth);
    }

    public void setScannerTargetUrl(String scannerTargetUrl) {
        this.execution.setVariable(ZapFields.ZAP_SCANNER_TARGET_URL.name(), scannerTargetUrl);
    }

    public void setScannerExcludeRegexes(String scannerExcludeRegexes) {
        this.execution.setVariable(ZapFields.ZAP_SCANNER_EXLUDE_REGEX.name(), scannerExcludeRegexes);
    }

    public void setScannerIncludeRegexes(String scannerIncludeRegexes) {
        this.execution.setVariable(ZapFields.ZAP_SCANNER_INCLUDE_REGEX.name(), scannerIncludeRegexes);
    }

    public void setReportingTypes(String[] reportingTypes) {
        this.execution.setVariable(ZapFields.ZAP_REPORTING_TYPES.name(), Arrays.toString(reportingTypes));
    }

    public void setReportingFalsePositives(String reportingFalsePositives) {
        this.execution.setVariable(ZapFields.ZAP_REPORTING_FALSE_POSITIVES.name(), reportingFalsePositives);
    }


}

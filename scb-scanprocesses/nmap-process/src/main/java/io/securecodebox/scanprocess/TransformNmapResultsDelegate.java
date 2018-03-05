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
import io.securecodebox.constants.CommonReportFields;
import io.securecodebox.constants.NmapConstants;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 01.03.18
 */
@Component
public class TransformNmapResultsDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(TransformNmapResultsDelegate.class);

    @Autowired
    ScanProcessExecutionFactory processExecutionFactory;

    private ScanProcessExecution process;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        LOG.debug("TransformNmapResultsDelegate execute");

        process = processExecutionFactory.get(delegateExecution, NmapScanProcessExecution.class);

        LOG.debug("VARS: {}", delegateExecution.getVariables());

        String portScannerResultJson = delegateExecution.<StringValue>getVariableTyped(
                NmapConstants.NMAP_RESULT_JSON).getValue();

        if (!StringUtils.isEmpty(portScannerResultJson)) {

            JSONObject json = new JSONObject(portScannerResultJson);
            JSONObject issues = json.getJSONObject("content");

            List<Map<String, String>> findingsList = new ArrayList<>();

            // split each finding (raw finding list) into a separate result entity and enrich them with meta data
            Stream.of(issues).forEach((issue) -> {
                // build a new generic (common reporting) result entry, based on the NMAP scanner specific raw results
                Map finding = new HashMap();

                //TODO: rhe: What about a lookup pattern or an own Finding Type?
                // reusing the existing meta data fields and adding them to each single result entry
                finding.put(CommonConstants.DOCUMENT_UUID, issue.get(CommonConstants.DOCUMENT_UUID));
                finding.put(CommonReportFields.SCANNER_NAME, issue.get(CommonConstants.MICROSERVICE));
                finding.put(CommonConstants.MICROSERVICE_ID, issue.get(CommonConstants.MICROSERVICE_ID));
                finding.put(CommonConstants.MICROSERVICE, issue.get(CommonConstants.MICROSERVICE));
                finding.put(CommonConstants.PROCESS_UUID, issue.get(CommonConstants.PROCESS_UUID));
                finding.put(CommonConstants.TENAND_ID, issue.get(CommonConstants.TENAND_ID));
                finding.put(CommonConstants.CONTEXT, issue.get(CommonConstants.CONTEXT));

                // for each finding transform the raw data fields into generic field
                finding.put(CommonReportFields.NAME, issue.get("service"));
                finding.put(CommonReportFields.CATEGORY, "Open Port");
                finding.put(CommonReportFields.TIER, "Network");
                finding.put(CommonReportFields.PORT, issue.get("port"));
                String description = String.format("Port %d is open using %s protocol.", issue.get(NmapConstants.PORT),
                        issue.get(NmapConstants.PROTOCOL));
                finding.put(CommonReportFields.DESCRIPTION, description);

                finding.put(CommonReportFields.URL, issue.get("ip"));
                finding.put(CommonReportFields.IP_ADDRESS, issue.get("ip"));
                finding.put(CommonReportFields.SEVERITY, issue.get("informational"));

                findingsList.add(finding);
            });

            LOG.debug("Found {} findings", findingsList.size());

            // persist all generic result entries
            //new GenericReporter(delegateExecution).setGenericResultsVariable(findingsList)
        } else {
            LOG.warn("Couldn't find the process variable or its content is empty: {}", NmapConstants.NMAP_RESULT_JSON);
        }
    }
}

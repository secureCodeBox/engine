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

import io.securecodebox.constants.NmapConstants;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Robert Seedorff - iteratec GmbH
 */
class FinishedPortScanTaskListener extends PersistenceAwareTaskListener {

    @Autowired
    ScanProcessExecutionFactory factory;

    private static final Logger LOG = LoggerFactory.getLogger(FinishedPortScanTaskListener.class);

    @Override
    public void notify(DelegateExecution execution) throws Exception {

        LOG.debug("FinishedPortscanTaskListener start");

        ScanProcessExecution process = factory.get(execution);

        // TODO: workaround - should be defined by the nmap microservice itself, not here
        process.setScannerType("nmap");

        String portScannerResultJson = (String) execution.getVariable(NmapConstants.NMAP_RESULT_JSON);
        //
        //        String microserviceType = (String) execution.getVariable(Variables.MICROSERVICE);
        //        String microserviceId = (String) execution.getVariable(Variables.MICROSERVICE_ID);
        //
        //        if (microserviceId == null) {
        //            microserviceId = "unkown-portscanner-id";
        //        }
        //
        //        log.debug("VARS: ${execution.getVariables().toMapString()}")
        //
        if (portScannerResultJson != null) {
            //
            //            // Add additional process informations
            //            def jsonSlurper = new JsonSlurper() Map jsonMap = jsonSlurper.parseText(portScannerResultJson)
            //
            //            // For a better elasticsearch usability store each result as document:
            //            if (jsonMap != null && jsonMap.content != null) {
            //
            //                this.addProcessInformationsToResult(jsonMap, process, microserviceId);
            //                log.debug("Enriched results with process specific informations.");
            //
            //                // enrich each result entry also:
            //                jsonMap.content.each {
            //                    def content = it
            //
            //                    content[Variables.DOCUMENT_UUID] = UUID.randomUUID().toString();
            //
            //                    content[Variables.MICROSERVICE] = microserviceType
            //                    content[Variables.MICROSERVICE_ID] = microserviceId
            //
            //                    // add processUuid to each content object to be able to filter each result entry by the assigned processUuid in kibana.
            //                    if (jsonMap[Variables.PROCESS_UUID] != null) {
            //                        log.debug("Adding processUuid '{}' to result entry.", jsonMap[Variables.PROCESS_UUID])
            //                        content[Variables.PROCESS_UUID] = jsonMap[Variables.PROCESS_UUID]
            //                    } else {
            //                        log.debug("Couldn't find processUuid")
            //                    }
            //
            //                    // add tenantId to each content object to be able to filter each result entry by the assigned tenantId in kibana.
            //                    if (jsonMap[Variables.TENAND_ID] != null) {
            //                        log.debug("Adding tenantId '{}' to result entry.", jsonMap[Variables.TENAND_ID])
            //                        content[Variables.TENAND_ID] = jsonMap[Variables.TENAND_ID]
            //                    } else {
            //                        log.debug("Couldn't find tenantId")
            //                    }
            //
            //                    // add context to each content object to be able to filter each result entry by the assigned context in kibana.
            //                    if (jsonMap[Variables.CONTEXT] != null) {
            //                        log.debug("Adding context '{}' to content object", jsonMap[Variables.CONTEXT])
            //                        content[Variables.CONTEXT] = jsonMap[Variables.CONTEXT]
            //                    } else {
            //                        log.debug("Couldn't find context")
            //                    }
            //                }
            //            } else {
            //                log.error("Couldn't parse the nmap content found as scan result in the process variable!");
            //            }
            //
            //            log.debug("Start json conversion of the extended nmap results");
            //
            //            String jsonString = JsonOutput.toJson(jsonMap) log.debug("Result json string: {}", jsonString);
            //
            //            // persist the extended results as raw data again
            //            execution.setVariable(NmapVariables.NMAP_RESULT_JSON, jsonString);
            //
            //            // persist the extended results to the persistence store
            //            this.storeResult(process.getUuid(), jsonMap, this.config.nmap.indexType, process.getTenantId(),
            //                    microserviceId);
        } else {
            LOG.error("Couldn't find any nmap scanner result or process variable named: {}",
                    NmapConstants.NMAP_RESULT_JSON);
        }
    }
}

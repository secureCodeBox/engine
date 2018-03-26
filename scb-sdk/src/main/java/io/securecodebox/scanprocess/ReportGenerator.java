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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.Report;
import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import io.securecodebox.persistence.PersistenceProvider;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Robert Seedorff - iteratec GmbH
 * @since 17.04.17
 */
@Component
public class ReportGenerator implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(ReportGenerator.class);

    @Autowired
    ScanProcessExecutionFactory processExecutionFactory;
    @Autowired
    private PersistenceProvider persistenceProvider;
    @Autowired
    ObjectMapper mapper;

    private String config = "";

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        ScanProcessExecution process = processExecutionFactory.get(delegateExecution);

        Report report = new Report(process);
        LOG.debug("Writing report={}", mapper.writeValueAsString(report));

        //        def results = (LinkedBlockingDeque) delegateExecution.getVariable(
        //                Variables.GENERIC_SCAN_RESULTS) //persistenceProvider.getJsonContentForType(process.getUuid())
        //
        //        def summary = [:]
        //
        //        def severity = [info:
        //        0, low:0, medium:0, high:0]
        //        def scanners_used = []
        //        def microservices_used = []
        //        def numFindings = 0
        //
        //        // What an elegant way...
        //        summary[SummaryVariables.SCAN_KEY] = delegateExecution.getProcessEngineServices()
        //                .getRepositoryService()
        //                .getProcessDefinition(delegateExecution.getProcessDefinitionId())
        //                .getKey()
        //
        //        if (results != null) {
        //
        //            numFindings = results.size()
        //
        //            // TODO evaluate if this is better done with elastic aggregation api
        //            results.each {
        //                switch (it.severity) {
        //                    case "informational":
        //                        severity.info++;
        //                        break case "low":
        //                        severity.low++;
        //                        break case "medium":
        //                        severity.medium++;
        //                        break case "high":
        //                        severity.high++;
        //                        break
        //                } if (!scanners_used.contains(it.scanner)) {
        //                    scanners_used.add(it.scanner)
        //                } if (!microservices_used.contains(it.microserviceId)) {
        //                    microservices_used.add(it.microserviceId)
        //                }
        //            }
        //
        //            // if the complete process only using one single microservice set the id as meta-description
        //            if (!microservices_used.empty && microservices_used.size() == 1) {
        //                summary[Variables.MICROSERVICE_ID] = microservices_used.get(0);
        //            }
        //
        //            this.addBuildVariables(summary, delegateExecution);
        //        } else {
        //            log.warn("No results found in '" + Variables.GENERIC_SCAN_RESULTS + "' to transform! regarding processId "
        //                    + process.getUuid());
        //        }
        //
        //        summary[SummaryVariables.SEVERITY_OVERVIEW] = severity
        //        summary[SummaryVariables.SEVERITY_TOTAL] = numFindings
        //        summary[SummaryVariables.SCANNERS_USED] = scanners_used
        //        summary[SummaryVariables.MICROSERVICES_USED] = microservices_used
        //
        //        summary[Variables.TARGET_URL] = process.getTargetUrl()
        //        summary[Variables.PROCESS_UUID] = process.getUuid()
        //        summary[Variables.TENAND_ID] = process.getTenantId()
        //        summary[Variables.CONTEXT] = process.getContext()
        //        summary[Variables.MICROSERVICE] = process.getScannerType();
        //        summary[Variables.CONTEXT] = process.getContext()
        //
        //        //TODO make this an array for all dashboard urls
        //        summary['dashboard_urls'] = process.getReportingDashboardUrl()
        //
        //        evaluateCIBuildStatus(delegateExecution, summary)
        //        if (delegateExecution.getVariable(Variables.CI_BUILD_SUCCESS) != null) {
        //            summary[Variables.CI_BUILD_SUCCESS] = delegateExecution.getVariable(Variables.CI_BUILD_SUCCESS)
        //        }
        //
        //        persistSummary(summary) delegateExecution.getVariables().put(Variables.RESULT_SUMMARY, summary)
        //    }
        //
        //    def evaluateCIBuildStatus(DelegateExecution delegateExecution, def summary) {
        //        def maxSeverityAllowed = delegateExecution.getVariable(Variables.CI_MAX_SEVERITY_LVL)
        //        def maxCountAllowed = delegateExecution.getVariable(Variables.CI_MAX_THREAT_COUNT) if (!maxCountAllowed)
        //            maxCountAllowed = 0
        //
        //        if (maxSeverityAllowed) {
        //            def buildSuccess = true
        //
        //            if (maxSeverityViolated(maxSeverityAllowed, summary[SummaryVariables.SEVERITY_OVERVIEW])) {
        //                buildSuccess = false
        //            }
        //
        //            def actualCount = summary[SummaryVariables.SEVERITY_OVERVIEW][maxSeverityAllowed]
        //            if (actualCount > maxCountAllowed) {
        //                buildSuccess = false
        //            }
        //
        //            delegateExecution.setVariable(Variables.CI_BUILD_SUCCESS, buildSuccess)
        //
        //        }
        //
        //    }
        //
        //    boolean maxSeverityViolated(def maxSeverityAllowed, def severityOverview) {
        //        def allowedLvl = SeverityLevels.numeric(maxSeverityAllowed) def violation = false severityOverview.each {
        //            k, v -> if (v > 0 && SeverityLevels.numeric(k) > allowedLvl) {
        //                violation = true
        //            }
        //        } return violation
        //    }
        //
        //    /**
        //     * Eventually consistent: try to persist if the persistence provider is currently available.
        //     *
        //     * @param summary The generic summary of findings to persist.
        //     */
        //    private void persistSummary(def summary) {
        //        try {
        //            this.persistenceProvider = PersistenceService.getInstance()
        //                    .getPersistenceProvider(config, this.config.summary.indexType, process.getTenantId())
        //            this.persistenceProvider.saveJsonContent(this.config.summary.indexType, process.getUuid(), summary, "")
        //
        //        } catch (ServiceNotFoundException e) {
        //            log.error("Error while trying to persist a Document! PersistenceProvider could't be reached!", e)
        //        } catch (Exception e) {
        //            log.error("Unexpected Error while trying to init a persistence provider!", e)
        //        }
        //
        //        log.debug("scan summary persisted")
        //    }
        //
        //    /**
        //     * Adds some build specific process variables to the summary document, if they exists.
        //     *
        //     * @param summary
        //     * @param execution
        //     */
        //    private void addBuildVariables(def summary, DelegateExecution execution) {
        //
        //        if (execution.getVariableNames().contains(SummaryVariables.CLIENT_TYPE) && (execution.getVariable(
        //                SummaryVariables.CLIENT_TYPE)).equals("jenkins")) {
        //            summary[SummaryVariables.CLIENT_TYPE] = execution.getVariable(SummaryVariables.CLIENT_TYPE)
        //            summary[SummaryVariables.BUILD_ID] = execution.getVariable(SummaryVariables.BUILD_ID)
        //            summary[SummaryVariables.BUILD_NUMBER] = execution.getVariable(SummaryVariables.BUILD_NUMBER)
        //            summary[SummaryVariables.BUILD_TAG] = execution.getVariable(SummaryVariables.BUILD_TAG)
        //            summary[SummaryVariables.BUILD_URL] = execution.getVariable(SummaryVariables.BUILD_URL)
        //        }
    }
}


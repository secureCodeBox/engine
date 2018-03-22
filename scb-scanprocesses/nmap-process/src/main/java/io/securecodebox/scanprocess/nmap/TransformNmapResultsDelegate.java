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

package io.securecodebox.scanprocess.nmap;

import io.securecodebox.constants.DefaultFields;
import io.securecodebox.constants.NmapFindingAttributes;
import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.findings.OsiLayer;
import io.securecodebox.model.findings.Severity;
import io.securecodebox.scanprocess.nmap.model.Host;
import io.securecodebox.scanprocess.nmap.model.NmapRawResult;
import io.securecodebox.scanprocess.nmap.model.Port;
import io.securecodebox.scanprocess.nmap.model.Ports;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 01.03.18
 */
@Component
public class TransformNmapResultsDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(TransformNmapResultsDelegate.class);

    @Autowired
    ScanProcessExecutionFactory processExecutionFactory;

    @Autowired
    DocumentBuilderFactory documentBuilderFactory;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        ScanProcessExecution process = processExecutionFactory.get(delegateExecution);

        LOG.trace("VARS: {}", delegateExecution.getVariables());

        String rawFindingResultXML = process.getScanner().getRawFindings();

        if (!StringUtils.isEmpty(rawFindingResultXML)) {
            final JAXBContext context = JAXBContext.newInstance(NmapRawResult.class);
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            NmapRawResult rawResult = (NmapRawResult) unmarshaller.unmarshal(new StringReader(rawFindingResultXML));

            for (Host host : rawResult.getHosts()) {
                for (Ports ports : host.getPorts()) {
                    for (Port port : ports.getPort()) {
                        Finding finding = new Finding();
                        finding.setId(UUID.randomUUID());
                        finding.setCategory("Open Port");
                        finding.setName(String.format("Open %s Port", port.getService().getName()));
                        finding.setOsiLayer(OsiLayer.NETWORK);
                        finding.setDescription(String.format("Port %d is open using %s protocol.", port.getPortid(),
                                port.getProtocol()));
                        finding.setLocation(
                                port.getProtocol() + "://" + host.getAdress().getAddr() + ":" + port.getPortid());
                        finding.setServerity(Severity.INFORMATIONAL);
                        finding.addAttribute(NmapFindingAttributes.PORT, port.getPortid());
                        finding.addAttribute(NmapFindingAttributes.SERVICE, port.getService().getName());
                        finding.addAttribute(NmapFindingAttributes.PROTOCOL, port.getProtocol());
                        finding.addAttribute(NmapFindingAttributes.HOST, host.getAdress().getAddr());
                        finding.addAttribute(NmapFindingAttributes.STATE, port.getState().getState());
                        finding.addAttribute(NmapFindingAttributes.START, host.getStarttime());
                        finding.addAttribute(NmapFindingAttributes.END, host.getEndtime());
                        process.appendFinding(finding);
                        LOG.trace("Finding: {}", finding);
                    }
                }
            }

            LOG.debug("Found {} findings", process.getFindings().size());

            // persist all generic result entries
            //new GenericReporter(delegateExecution).setGenericResultsVariable(findingsList)
        } else {
            LOG.warn("Couldn't find the process variable or its content is empty: {}",
                    DefaultFields.PROCESS_RAW_FINDINGS);
        }
    }

}

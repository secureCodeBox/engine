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

package io.securecodebox.scanprocesses.amassnmap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransformAmassResultsToNmapInput implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(TransformAmassResultsToNmapInput.class);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private OriginalTargetRestorer originalTargetRestorer;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        LOG.debug("Converting amass output to nmap input");

        try {
            String findingsAsString = objectMapper.writeValueAsString(execution.getVariable(DefaultFields.PROCESS_FINDINGS.name()));
            List<Finding> findings = objectMapper.readValue(objectMapper.readValue(findingsAsString, String.class),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Finding.class));

            String targetsAsString = objectMapper.writeValueAsString(execution.getVariable(DefaultFields.PROCESS_TARGETS.name()));
            List<Target> targets = objectMapper.readValue(objectMapper.readValue(targetsAsString, String.class),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Target.class));

            Target originalTarget = targets.get(0);
            originalTargetRestorer.storeOriginalTargetInSeparateProcessVariable(originalTarget, execution);

            String nmapProfile = (String) originalTarget.getAttributes().get(AdditionalTargetAttributes.NMAP_CONFIGURATION_PROFILE.name());
            Boolean nmapHttpHeaders = (Boolean) originalTarget.getAttributes().get(AdditionalTargetAttributes.NMAP_HTTP_HEADERS.name());
            if (nmapHttpHeaders == null) nmapHttpHeaders = false;
            String nmapParameters = getNmapParameters(nmapProfile, nmapHttpHeaders);


            List<Target> newTargets = new ArrayList<>();
            for (Finding finding : findings) {
                Target newTarget = new Target();
                newTarget.setLocation(finding.getLocation());
                newTarget.appendOrUpdateAttribute("NMAP_PARAMETER", nmapParameters);
                newTargets.add(newTarget);
            }
            LOG.debug("Transformed findings to new targets: " + newTargets);

            ObjectValue objectValue = Variables.objectValue(objectMapper.writeValueAsString(newTargets))
                    .serializationDataFormat(Variables.SerializationDataFormats.JSON)
                    .create();
            execution.setVariable(DefaultFields.PROCESS_TARGETS.name(), objectValue);

            execution.setVariable("NMAP_CONFIGURATION_TYPE","default");
            execution.setVariable("PARSE_HTTP_HEADERS", nmapHttpHeaders);

            LOG.debug("Finished TransformAmassResultsToNmapInput Service Task. Continue with nmap scan");

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Can't write field to process!", e);
        }
    }

    private String getNmapParameters(String nmapProfile, boolean withHttpHeaders) {
        final String scriptModules = withHttpHeaders ? " " + NmapConfigProfile.WITH_HTTP_HEADERS.getParameter() : "";
        String defaultNmapParameters = NmapConfigProfile.HTTP_PORTS.getParameter();

        if(nmapProfile == null) {
            LOG.info("No nmap profile set for combined amass-nmap test. Use http ports as default");
            return defaultNmapParameters + scriptModules;
        }

        switch (NmapConfigProfile.valueOf(nmapProfile)) {
            case HTTP_PORTS:
                return NmapConfigProfile.HTTP_PORTS.getParameter() + scriptModules;
            case TOP_100_PORTS:
                return NmapConfigProfile.TOP_100_PORTS.getParameter() + scriptModules;
            default:
                LOG.info("Invalid nmap profile set for combined amass-nmap test. Use http ports as default");
                return defaultNmapParameters + scriptModules;
        }
    }

}

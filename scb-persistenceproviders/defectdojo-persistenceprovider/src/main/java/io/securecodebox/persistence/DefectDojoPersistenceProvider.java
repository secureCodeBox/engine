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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.securitytest.CommonMetaFields;
import io.securecodebox.model.securitytest.SecurityTest;

import io.securecodebox.persistence.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@ConditionalOnProperty(name = "securecodebox.persistence.defectdojo.enabled", havingValue = "true")
public class DefectDojoPersistenceProvider implements PersistenceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DefectDojoPersistenceProvider.class);

    @Autowired
    DefectDojoService defectDojoService;

    @Autowired
    DescriptionGenerator descriptionGenerator;

    protected static final String DATE_FORMAT = "yyyy-MM-dd";

    @Value("${securecodebox.persistence.defectdojo.baseurl}")
    String defectDojoUrl;

    @Value("${securecodebox.persistence.defectdojo.apikey}")
    protected String defectDojoApiKey;

    Clock clock = Clock.systemDefaultZone();

    public void setClock(Clock clock){
        this.clock = clock;
    }

    @Override
    public void persist(SecurityTest securityTest) throws PersistenceException {
        LOG.debug("Starting defectdojo persistence provider");
        LOG.debug("RawFindings: {}", securityTest.getReport().getRawFindings());

        checkConnection();
        checkToolTypes();

        EngagementResponse res = createEngagement(securityTest);
        String engagementUrl = res.getUrl();
        LOG.debug("Created engagement: '{}'", engagementUrl);

        String username = securityTest.getMetaData().get(DefectDojoMetaFields.DEFECT_DOJO_USER.name());
        String userUrl = defectDojoService.getUserUrl(username);

        List<String> results = getDefectDojoScanName(securityTest.getName()).equals("Generic Findings Import") ? getGenericResults(securityTest) : getRawResults(securityTest);
            for (String result : results) {
                defectDojoService.createFindings(
                        result,
                        engagementUrl,
                        userUrl,
                        currentDate(),
                        getDefectDojoScanName(securityTest.getName())
                );
            }
    }

    static final String GIT_SERVER_NAME = "GitServer";

    static final String BUILD_SERVER_NAME = "BuildServer";
    static final String SECURITY_TEST_SERVER_NAME = "SecurityTestOrchestrationEngine";
    private void checkToolTypes() {
        DefectDojoResponse<ToolType> toolTypeGitResponse = defectDojoService.getToolTypeByName(GIT_SERVER_NAME);
        if(toolTypeGitResponse.getCount() == 0){
            defectDojoService.createToolType(GIT_SERVER_NAME, "Source Code Management Server");
        }

        DefectDojoResponse<ToolType> toolTypeScmResponse = defectDojoService.getToolTypeByName(BUILD_SERVER_NAME);
        if(toolTypeScmResponse.getCount() == 0){
            defectDojoService.createToolType(BUILD_SERVER_NAME, "Build Server responsible for starting Security Scan");
        }

        DefectDojoResponse<ToolType> toolTypeStoeResponse = defectDojoService.getToolTypeByName(SECURITY_TEST_SERVER_NAME);
        if(toolTypeStoeResponse.getCount() == 0){
            defectDojoService.createToolType(SECURITY_TEST_SERVER_NAME, "Security Test Orchestration Engine");
        }
    }

    void checkConnection() throws DefectDojoUnreachableException {
        try {
            final URLConnection connection = new URL(defectDojoUrl).openConnection();
            connection.connect();
        }catch (final MalformedURLException e){
            throw new DefectDojoUnreachableException("Could not reach defectdojo at '" + defectDojoUrl + "'!");
        }catch (final IOException e){
            throw new DefectDojoUnreachableException("Could not reach defectdojo at '" + defectDojoUrl + "'!");
        }
    }

    private List<String> getRawResults(SecurityTest securityTest) throws DefectDojoPersistenceException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String rawRawFindings = objectMapper.readValue(securityTest.getReport().getRawFindings(), String.class);

            return objectMapper.readValue(rawRawFindings,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (IOException e) {
            throw new DefectDojoPersistenceException("RawResults were in an unexpected format. Might be something wrong with the scanner implementation?");
        }
    }

    private List<String> getGenericResults(SecurityTest securityTest) {
        List<String> genericResults = new LinkedList<>();
        for(Finding finding: securityTest.getReport().getFindings()){
            genericResults.add(MessageFormat.format("date,title,cweid,url,severity,description,mitigation,impact,references,active,verified,falsepositive,duplicate\n" +
                            "{0},{1},,{2},{3},{4},,,,,,{5},{6}",
                    currentDate(),
                    finding.getName().replace(",", "  "),
                    finding.getLocation().replace(",", "  "),
                    finding.getSeverity(),
                    finding.getDescription().replace(",", "  "),
                    finding.isFalsePositive(),
                    "false"
            ));
        }
        return genericResults;
    }

    private EngagementResponse createEngagement(SecurityTest securityTest) {
        EngagementPayload engagementPayload = new EngagementPayload();
        engagementPayload.setName(securityTest.getContext());

        String productId = securityTest.getMetaData().get(DefectDojoMetaFields.DEFECT_DOJO_PRODUCT.name());
        if (productId == null) {
            throw new DefectDojoProductNotProvided("DefectDojo persistence provider was configured but no product id was provided in the security test meta fields.");
        }
        String username = securityTest.getMetaData().get(DefectDojoMetaFields.DEFECT_DOJO_USER.name());

        engagementPayload.setProduct(defectDojoUrl + "/api/v2/products/" + productId + "/");
        engagementPayload.setLead(defectDojoService.getUserUrl(username));
        engagementPayload.setDescription(descriptionGenerator.generate(securityTest));
        engagementPayload.setBranch(securityTest.getMetaData().get(CommonMetaFields.SCB_BRANCH.name()));
        engagementPayload.setBuildID(securityTest.getMetaData().get(CommonMetaFields.SCB_BUILD_ID.name()));
        engagementPayload.setCommitHash(securityTest.getMetaData().get(CommonMetaFields.SCB_COMMIT_HASH.name()));
        engagementPayload.setRepo(securityTest.getMetaData().get(CommonMetaFields.SCB_REPO.name()));
        engagementPayload.setTracker(securityTest.getMetaData().get(CommonMetaFields.SCB_TRACKER.name()));

        engagementPayload.setBuildServer(defectDojoService.getToolConfiguration(securityTest.getMetaData().get(CommonMetaFields.SCB_BUILD_SERVER.name()), "BuildServer"));
        engagementPayload.setScmServer(defectDojoService.getToolConfiguration(securityTest.getMetaData().get(CommonMetaFields.SCB_SCM_SERVER.name()), "GitServer"));
        engagementPayload.setOrchestrationEngine(defectDojoService.getToolConfiguration("https://github.com/secureCodeBox","SecurityTestOrchestrationEngine"));

        engagementPayload.setTargetStart(currentDate());
        engagementPayload.setTargetEnd(currentDate());

        engagementPayload.setStatus(EngagementPayload.Status.COMPLETED);

        return defectDojoService.createEngagement(engagementPayload);
    }

    private String currentDate() {
        return LocalDate.now(clock).format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    protected static String getDefectDojoScanName(String securityTestName) {
        Map<String, String> scannerDefectDojoMapping = new HashMap<>();

        // Officially supported by secureCodeBox
        scannerDefectDojoMapping.put("arachni", "Arachni Scan");
        scannerDefectDojoMapping.put("nmap", "Nmap Scan");
        scannerDefectDojoMapping.put("zap", "ZAP Scan");

        // Nikto is a supported tool as well but currently not accessible for supported import.
        // Nikto thus will use Generic Findings Import.

        // Can be used by 3rd party integrations to
        // import these scan results directly into defectdojo
        scannerDefectDojoMapping.put("appspider", "AppSpider Scan");
        scannerDefectDojoMapping.put("bandit", "Bandit Scan");
        scannerDefectDojoMapping.put("burp", "Burp Scan");
        scannerDefectDojoMapping.put("checkmarx", "Checkmarx Scan");
        scannerDefectDojoMapping.put("dependencycheck", "Dependency Check Scan");
        scannerDefectDojoMapping.put("gosec", "Gosec Scanner");
        scannerDefectDojoMapping.put("nessus", "Nessus Scan");
        scannerDefectDojoMapping.put("nexpose", "Nexpose Scan");
        scannerDefectDojoMapping.put("nodesecurityplattform", "Node Security Platform Scan");
        scannerDefectDojoMapping.put("openvas", "OpenVAS CSV");
        scannerDefectDojoMapping.put("qualys", "Qualys Scan");
        scannerDefectDojoMapping.put("qualyswebapp", "Qualys Webapp Scan");
        scannerDefectDojoMapping.put("retirejs", "Retire.js Scan");
        scannerDefectDojoMapping.put("skf", "SKF Scan");
        scannerDefectDojoMapping.put("ssllabs", "SSL Labs Scan");
        scannerDefectDojoMapping.put("snyk", "Snyk Scan");
        scannerDefectDojoMapping.put("trustwave", "Trustwave Scan (CSV)");
        scannerDefectDojoMapping.put("vgg", "VCG Scan");
        scannerDefectDojoMapping.put("veracode", "Veracode Scan");

        if (scannerDefectDojoMapping.containsKey(securityTestName)) {
            return scannerDefectDojoMapping.get(securityTestName);
        }else{
            return "Generic Findings Import";
        }
    }
}

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

import io.securecodebox.persistence.exceptions.DefectDojoPersistenceException;
import io.securecodebox.persistence.exceptions.DefectDojoUnreachableException;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@ConditionalOnProperty(name = "securecodebox.persistence.defectdojo.enabled", havingValue = "true")
public class DefectDojoPersistenceProvider implements PersistenceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DefectDojoPersistenceProvider.class);
    private static final String GIT_SERVER_NAME = "Git Server";
    private static final String BUILD_SERVER_NAME = "Build Server";
    private static final String SECURITY_TEST_SERVER_NAME = "Security Test Orchestration Engine";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Value("${securecodebox.persistence.defectdojo.optional:false}")
    protected boolean isOptional;

    @Autowired
    private DefectDojoService defectDojoService;

    @Autowired
    private DescriptionGenerator descriptionGenerator;

    private Clock clock = Clock.systemDefaultZone();

    /**
     * Persists the given securityTest within DefectDojo.
     * @param securityTest The securitTest to persist.
     * @throws PersistenceException If any persistence error occurs.
     */
    @Override
    public void persist(SecurityTest securityTest) throws PersistenceException {
        LOG.debug("Starting DefectDojo persistence provider");
        LOG.debug("RawFindings: {}", securityTest.getReport().getRawFindings());

        try {
            persistInDefectDojo(securityTest);
        } catch (Exception e) {
            // ignore error if defect dojo provider is set to optional
            if(isOptional) {
                LOG.error("Failed to persist security test in defect dojo", e);
                return;
            } else throw e;
        }
    }

    /**
     * Persists a given securityTest within DefectDojo.
     * @param securityTest The securitTest to persist.
     * @throws PersistenceException If any persistence error occurs.
     */
    private void persistInDefectDojo(SecurityTest securityTest) throws PersistenceException {
        checkConnection();
        ensureToolTypesExistence();

        EngagementResponse res = createEngagement(securityTest);
        long engagementId = res.getId();
        LOG.debug("Created engagement: '{}'", engagementId);

        String username = securityTest.getMetaData().get(DefectDojoMetaFields.DEFECT_DOJO_USER.name());
        long userUrl = defectDojoService.getUserId(username);

        List<String> results = getDefectDojoScanName(securityTest.getName()).equals("Generic Findings Import") ? getGenericResults(securityTest) : getRawResults(securityTest);
        for (String result : results) {
            defectDojoService.createFindings(
                    result,
                    engagementId,
                    userUrl,
                    currentDate(),
                    getDefectDojoScanName(securityTest.getName())
            );
        }
    }

    /**
     * Checks if DefectDojo is available and reachable.
     * @throws DefectDojoUnreachableException If DefectDojo is not reachable
     */
    protected void checkConnection() throws DefectDojoUnreachableException {
        try {
            final URLConnection connection = new URL(defectDojoService.defectDojoUrl).openConnection();
            connection.connect();
        } catch (final MalformedURLException e){
            throw new DefectDojoUnreachableException("Could not reach defectdojo at '" + defectDojoService.defectDojoUrl + "'!");
        } catch (final IOException e){
            throw new DefectDojoUnreachableException("Could not reach defectdojo at '" + defectDojoService.defectDojoUrl + "'!");
        }
    }

    /**
     * Creates Tool Types for GIT_SERVER_NAME, BUILD_SERVER_NAME, SECURITY_TEST_SERVER_NAME if they are not existing.
     */
    private void ensureToolTypesExistence() {
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

    /**
     * Returns the rawResults (original security scanner results) of the given securityTests.
     * @param securityTest The securityTest to return the rawResults for.
     * @return the rawResults (original security scanner results) of the given securityTests.
     * @throws DefectDojoPersistenceException
     */
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

    /**
     * Returns a list of generic finding results as string.
     * @param securityTest
     * @return
     */
    private List<String> getGenericResults(SecurityTest securityTest) {
        final String GENERIC_RESULT_CSV_HEADER = "date,title,cweid,url,severity,description,mitigation,impact,references,active,verified,falsepositive,duplicate";

        List<Finding> findings = securityTest.getReport().getFindings();

        String genericFindingsCsv = Stream.concat(
                Stream.of(GENERIC_RESULT_CSV_HEADER),
                findings.stream()
                        .map(finding -> ensureNameOrDescriptionIsNotNull(finding))
                        .map(finding -> MessageFormat.format(
                                "{0},{1},,{2},{3},{4},,,,,,{5},{6}",
                                currentDate(),
                                finding.getName().replace(",", "  "),
                                finding.getLocation().replace(",", "  "),
                                finding.getSeverity(),
                                finding.getDescription().replace(",", "  "),
                                finding.isFalsePositive(),
                                "false"
                        ))
        ).collect(Collectors.joining("\n"));

        return Collections.singletonList(genericFindingsCsv);
    }

    /**
     * Ensures that the name and description property of the given finding is not null.
     * If the property is null it will be set to an empty string.
     * @param finding The finding to ensure the not null conditions for.
     * @return The given finding without null properties.
     */
    private Finding ensureNameOrDescriptionIsNotNull(Finding finding) {
        if (null == finding.getName()) {
            finding.setName("");
        } else if (null == finding.getDescription()) {
            finding.setDescription("");
        }
        return finding;
    }

    /**
     * Creates a new DefectDojo engagement for the given securityTest.
     * @param securityTest The securityTest to crete an DefectDojo engagement for.
     * @return The newly created engagement.
     */
    private EngagementResponse createEngagement(SecurityTest securityTest) {
        EngagementPayload engagementPayload = new EngagementPayload();
        engagementPayload.setProduct(defectDojoService.getProductId(securityTest.getContext()));

        if(securityTest.getMetaData() == null){
            securityTest.setMetaData(new HashMap<>());
        }

        engagementPayload.setName(securityTest.getMetaData().get(CommonMetaFields.SCB_ENGAGEMENT_TITLE.name()) != null ?
                securityTest.getMetaData().get(CommonMetaFields.SCB_ENGAGEMENT_TITLE.name()) : getDefectDojoScanName(securityTest.getName()));
        engagementPayload.setLead(defectDojoService.getUserId(securityTest.getMetaData().get(DefectDojoMetaFields.DEFECT_DOJO_USER.name())));
        engagementPayload.setDescription(descriptionGenerator.generate(securityTest));
        engagementPayload.setBranch(securityTest.getMetaData().get(CommonMetaFields.SCB_BRANCH.name()));
        engagementPayload.setBuildID(securityTest.getMetaData().get(CommonMetaFields.SCB_BUILD_ID.name()));
        engagementPayload.setCommitHash(securityTest.getMetaData().get(CommonMetaFields.SCB_COMMIT_HASH.name()));
        engagementPayload.setRepo(securityTest.getMetaData().get(CommonMetaFields.SCB_REPO.name()));
        engagementPayload.setTracker(securityTest.getMetaData().get(CommonMetaFields.SCB_TRACKER.name()));

        engagementPayload.setBuildServer(defectDojoService.retrieveOrCreateToolConfiguration(securityTest.getMetaData().get(CommonMetaFields.SCB_BUILD_SERVER.name()), BUILD_SERVER_NAME));
        engagementPayload.setScmServer(defectDojoService.retrieveOrCreateToolConfiguration(securityTest.getMetaData().get(CommonMetaFields.SCB_SCM_SERVER.name()), GIT_SERVER_NAME));
        engagementPayload.setOrchestrationEngine(defectDojoService.retrieveOrCreateToolConfiguration("https://github.com/secureCodeBox", SECURITY_TEST_SERVER_NAME));

        engagementPayload.setTargetStart(currentDate());
        engagementPayload.setTargetEnd(currentDate());
        engagementPayload.setStatus(EngagementPayload.Status.COMPLETED);

        engagementPayload.getTags().add("secureCodeBox");
        engagementPayload.getTags().add("automated");

        return defectDojoService.createEngagement(engagementPayload);
    }

    /**
     * Returns the current date as string based on the DATE_FORMAT.
     * @return the current date as string based on the DATE_FORMAT.
     */
    private String currentDate() {
        return LocalDate.now(clock).format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * Returns the DefectDojo specific scanName for the given scb scbSecurityTestName.
     * @param scbSecurityTestName The scb scbSecurityTestName to return the corresponding DefectDojo scanName for.
     * @return The DefectDojo specific scanName for the given scb scbSecurityTestName.
     */
    protected static String getDefectDojoScanName(String scbSecurityTestName) {
        Map<String, String> scannerDefectDojoMapping = new HashMap<>();

        // Officially supported by secureCodeBox
        scannerDefectDojoMapping.put("arachni", "Arachni Scan");
        scannerDefectDojoMapping.put("nmap", "Nmap Scan");
        scannerDefectDojoMapping.put("zap", "ZAP Scan");

        // Map amass-nmap raw results to be imported as Nmap Results
        scannerDefectDojoMapping.put("amass-nmap", "Nmap Scan");

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

        if (scannerDefectDojoMapping.containsKey(scbSecurityTestName)) {
            return scannerDefectDojoMapping.get(scbSecurityTestName);
        } else {
            return "Generic Findings Import";
        }
    }
}

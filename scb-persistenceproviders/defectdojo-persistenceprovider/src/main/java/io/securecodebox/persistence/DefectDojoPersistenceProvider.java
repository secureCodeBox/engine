/*
 *
 *  SecureCodeBox (SCB)
 *  Copyright 2015-2020 iteratec GmbH
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
import io.securecodebox.model.securitytest.SecurityTest;
import io.securecodebox.persistence.exceptions.DefectDojoPersistenceException;
import io.securecodebox.persistence.exceptions.DefectDojoUnreachableException;
import io.securecodebox.persistence.models.DefectDojoMetaFields;
import io.securecodebox.persistence.models.EngagementResponse;
import io.securecodebox.persistence.service.DefectDojoEngagementService;
import io.securecodebox.persistence.service.DefectDojoFindingService;
import io.securecodebox.persistence.service.DefectDojoToolService;
import io.securecodebox.persistence.service.DefectDojoUserService;
import io.securecodebox.persistence.util.DescriptionGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@ConditionalOnProperty(name = "securecodebox.persistence.defectdojo.enabled", havingValue = "true")
public class DefectDojoPersistenceProvider implements PersistenceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DefectDojoPersistenceProvider.class);

    @Value("${securecodebox.persistence.defectdojo.optional:false}")
    protected boolean isOptional;

    @Autowired
    private DefectDojoUserService defectDojoUserService;

    @Autowired
    private DescriptionGenerator descriptionGenerator;

    @Autowired
    private DefectDojoFindingService defectFindingService;

    @Autowired
    private DefectDojoToolService defectDojoToolService;

    @Autowired
    private DefectDojoEngagementService defectDojoEngagementService;

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
        this.defectDojoToolService.ensureToolTypesExistence();

        String username = securityTest.getMetaData().get(DefectDojoMetaFields.DEFECT_DOJO_USER.name());
        long userId = defectDojoUserService.getUserId(username);

        EngagementResponse res = this.defectDojoEngagementService.createEngagement(securityTest, userId);
        long engagementId = res.getId();
        LOG.debug("Created engagement: '{}'", engagementId);

        List<String> results = this.descriptionGenerator.getDefectDojoScanName(securityTest.getName()).equals("Generic Findings Import") ? getGenericResults(securityTest) : getRawResults(securityTest);
        for (String result : results) {
            defectFindingService.createFindings (
                    result,
                    engagementId,
                    userId,
                    this.descriptionGenerator.currentDate(),
                    this.descriptionGenerator.getDefectDojoScanName(securityTest.getName())
            );
        }
    }

    /**
     * Checks if DefectDojo is available and reachable.
     * @throws DefectDojoUnreachableException If DefectDojo is not reachable
     */
    public void checkConnection() throws DefectDojoUnreachableException {
        try {
            final URLConnection connection = new URL(defectDojoUserService.defectDojoUrl).openConnection();
            connection.connect();
        } catch (final Exception e) {
            throw new DefectDojoUnreachableException("Could not reach defectdojo at '" + defectDojoUserService.defectDojoUrl + "'!");
        }
    }



    /**
     * Returns the rawResults (original security scanner results) of the given securityTests.
     *
     * @param securityTest The securityTest to return the rawResults for.
     * @return the rawResults (original security scanner results) of the given securityTests.
     * @throws DefectDojoPersistenceException If the raw
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
     * Returns a list of generic finding results as string based on the given securityTest.
     *
     * @param securityTest The securityTest to return a list of generic finding for.
     * @return A list of generic finding results as string.
     */
    private List<String> getGenericResults(SecurityTest securityTest) {
        final String GENERIC_RESULT_CSV_HEADER = "date,title,cweid,url,severity,description,mitigation,impact,references,active,verified,falsepositive,duplicate";

        List<Finding> findings = securityTest.getReport().getFindings();

        String genericFindingsCsv = Stream.concat(
                Stream.of(GENERIC_RESULT_CSV_HEADER),
                findings.stream()
                        .map(this::ensureNameOrDescriptionIsNotNull)
                        .map(finding -> MessageFormat.format(
                                "{0},{1},,{2},{3},{4},,,,,,{5},{6}",
                                this.descriptionGenerator.currentDate(),
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
     *
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
}

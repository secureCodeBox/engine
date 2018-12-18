package io.securecodebox.persistence.s3;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.securitytest.SecurityTest;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class FindingWriter {

    private static final Logger LOG = LoggerFactory.getLogger(FindingWriter.class);

    @Autowired
    private ObjectMapper mapper;

    File writeFindingToFile(Finding finding, SecurityTest securityTest) throws IOException {
        LOG.debug("Write finding " + finding.getId() + " to tempFile");
        File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".json");

        Map<String, Object> securityTestAsMap = convertToMap(finding);

        addSecurityTestInformationToMap(securityTestAsMap, securityTest);

        mapper.writeValue(tempFile, securityTestAsMap);
        return tempFile;
    }

    private void addSecurityTestInformationToMap(Map<String, Object> securityTestAsMap, SecurityTest securityTest) {
        securityTestAsMap.put("context", securityTest.getContext());
        securityTestAsMap.put("security_test_name", securityTest.getName());
        securityTestAsMap.put("security_test_id", securityTest.getId());
        securityTestAsMap.put("target_name", securityTest.getTarget().getName());
        securityTestAsMap.put("target_location", securityTest.getTarget().getLocation());
        securityTestAsMap.put("security_test_metaData", securityTest.getMetaData());
    }

    private Map<String, Object> convertToMap(Finding finding) throws IOException {
        String jsonString = mapper.writeValueAsString(finding);
        Map<String, Object> result = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        return result;
    }
}

package io.securecodebox.persistence.s3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.securitytest.SecurityTest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ReportWriter {

    private static final Logger LOG = LoggerFactory.getLogger(ReportWriter.class);

    @Autowired
    private ObjectMapper mapper;

    File writeReportToFile(SecurityTest securityTest) throws IOException {
        File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".json");
        Map<String, Object> securityTestAsMap = serializeAndRemove(securityTest, "report");
        mapper.writeValue(tempFile, securityTestAsMap);
        return tempFile;
    }


    private Map<String, Object> serializeAndRemove(Object object, String... toRemove) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writeValueAsString(object);
            Map<String, Object> result = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
            for (String attribute : toRemove) {
                result.remove(attribute);
            }
            return result;
        } catch (IOException e) {
            return new HashMap<>();
        }
    }
}

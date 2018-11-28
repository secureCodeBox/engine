package io.securecodebox.model.securitytest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;


import static org.junit.Assert.*;

public class SecurityTestConfigurationTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testDeserializeMetaData() throws IOException {
        String json = "{\"name\": \"myTestScan\", \"metaData\": { \"metaKey1\": \"metaValue1\", \"metaKey2\": \"metaValue2\" } }";
        SecurityTestConfiguration testConfiguration = objectMapper.readValue(json, SecurityTestConfiguration.class);
        assertEquals("metaValue1", testConfiguration.getMetaData().get("metaKey1"));
    }

    @Test
    public void testSerializeMetaData() throws JsonProcessingException {
        SecurityTestConfiguration configuration = new SecurityTestConfiguration();
        Map<String, String> metaData = new HashMap<>();
        metaData.put("myKey", "myValue");
        configuration.setMetaData(metaData);

        String serializedTestConfiguration = objectMapper.writeValueAsString(configuration);

        assertTrue(serializedTestConfiguration.contains("myValue"));
    }


}

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

package io.securecodebox.model.execution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.results.FindingTest;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 17.04.18
 */
public class ScannerTest {

    ObjectMapper mapper = new ObjectMapper();

    public static final String SERIALIZE_RESULT = "{\"id\":\"62fa8ffb-e3bc-433e-b322-9c02108c5171\",\"type\":\"Test_SCANNER\",\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\",\"false_positive\":false}],\"rawFindings\":\"[{\\\"pudding\\\":\\\"Bier\\\"}]\"}";

    @Test
    public void testSerialize() throws JsonProcessingException {
        Scanner scanner = new Scanner(UUID.fromString("62fa8ffb-e3bc-433e-b322-9c02108c5171"), "Test_SCANNER",
                "[{\"pudding\":\"Bier\"}]", new LinkedList<Finding>() {{
            add(FindingTest.createBasicFinding());
        }});
        assertEquals(SERIALIZE_RESULT, mapper.writeValueAsString(scanner));

    }

    @Test
    public void testDeserialize() throws IOException {
        Scanner scannerExp = new Scanner(UUID.fromString("62fa8ffb-e3bc-433e-b322-9c02108c5171"), "Test_SCANNER",
                "[{\"pudding\":\"Bier\"}]", new LinkedList<Finding>() {{
            add(FindingTest.createBasicFinding());
        }});

        Scanner scanner = mapper.readValue(SERIALIZE_RESULT, Scanner.class);

        assertEquals(UUID.fromString("62fa8ffb-e3bc-433e-b322-9c02108c5171"), scanner.getScannerId());
        assertEquals("Test_SCANNER", scanner.getScannerType());
        assertEquals("[{\"pudding\":\"Bier\"}]", scanner.getRawFindings());
        assertTrue(scanner.getFindings().size() == 1);

        assertEquals(scannerExp, scanner);

    }
}

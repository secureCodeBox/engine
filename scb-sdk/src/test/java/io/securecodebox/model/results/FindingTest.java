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

package io.securecodebox.model.results;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.findings.OsiLayer;
import io.securecodebox.model.findings.Reference;
import io.securecodebox.model.findings.Severity;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 08.03.18
 */
public class FindingTest {

    private String defaultFindingJson = "{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\",\"false_positive\":false}";
    private String defaultFindingJson2 = "{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd126\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"location\":\"mett.brot.securecodebox.io\",\"false_positive\":false}";

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSerialize() throws Exception {
        Finding finding = createBasicFinding();

        String result = objectMapper.writeValueAsString(finding);
        assertEquals(defaultFindingJson, result);
        System.out.println("OUT: " + result);
    }

    @Test
    public void testDeSerialize() throws Exception {

        Finding finding = objectMapper.readValue(defaultFindingJson, Finding.class);
        assertEquals(createBasicFinding(), finding);

        Finding finding2 = objectMapper.readValue(defaultFindingJson2, Finding.class);
        assertNotEquals(finding2, finding);
    }

    @Test
    public void testSeverityEnum() {

        // If you change the ordinal or name of an Enum this can break the Persistence!
        Severity[] values = Severity.values();
        assertEquals(values[0].ordinal(), 0);
        assertEquals(values[0].name(), "INFORMATIONAL");
        assertEquals(values[1].ordinal(), 1);
        assertEquals(values[1].name(), "LOW");
        assertEquals(values[2].ordinal(), 2);
        assertEquals(values[2].name(), "MEDIUM");
        assertEquals(values[3].ordinal(), 3);
        assertEquals(values[3].name(), "HIGH");
    }

    @Test
    public void testOsiEnum() {

        // If you change the ordinal or name of an Enum this can break the Persistence!
        OsiLayer[] values = OsiLayer.values();
        assertEquals(values[0].ordinal(), 0);
        assertEquals(values[0].name(), "APPLICATION");
        assertEquals(values[1].ordinal(), 1);
        assertEquals(values[1].name(), "PRESENTATION");
        assertEquals(values[2].ordinal(), 2);
        assertEquals(values[2].name(), "SESSION");
        assertEquals(values[3].ordinal(), 3);
        assertEquals(values[3].name(), "TRANSPORT");
        assertEquals(values[4].ordinal(), 4);
        assertEquals(values[4].name(), "NETWORK");
        assertEquals(values[5].ordinal(), 5);
        assertEquals(values[5].name(), "DATA_LINK");
        assertEquals(values[6].ordinal(), 6);
        assertEquals(values[6].name(), "PHYSICAL");
        assertEquals(values[7].ordinal(), 7);
        assertEquals(values[7].name(), "NOT_APPLICABLE");

    }

    public static Finding createBasicFinding() {
        Finding finding = new Finding();
        finding.setId(UUID.fromString("49bf7fd3-8512-4d73-a28f-608e493cd726"));
        Reference reference = new Reference();
        reference.setId("UNI_CODE_STUFF");
        reference.setSource("RISCOOL");
        finding.setReference(reference);
        finding.setCategory("COOL_TEST_STUFF");
        finding.setName("BAD_TEST_FINDIG");
        finding.setDescription("Some coder has tested this!");
        finding.setHint("You might wan't to blame Rüdiger!");
        finding.setSeverity(Severity.HIGH);
        finding.setOsiLayer(OsiLayer.NOT_APPLICABLE);
        finding.setLocation("mett.brot.securecodebox.io");
        finding.addAttribute("TEST", "Kekse");
        finding.addAttribute("HORRIBLE", "Coke");
        return finding;
    }
}

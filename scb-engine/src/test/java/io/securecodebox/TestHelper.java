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

package io.securecodebox;

import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.findings.OsiLayer;
import io.securecodebox.model.findings.Reference;
import io.securecodebox.model.findings.Severity;

import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 08.05.18
 */
public class TestHelper {

    private TestHelper() {
        // No Instances
    }

    public static Finding createBasicFinding() {
        return createBasicFinding(UUID.fromString("49bf7fd3-8512-4d73-a28f-608e493cd726"));
    }

    public static Finding createBasicFinding(UUID id) {
        Finding finding = new Finding();
        finding.setId(id);
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

    public static Finding createBasicFindingDifferent(UUID id) {
        Finding finding = new Finding();
        finding.setId(id);
        Reference reference = new Reference();
        reference.setId("OTHER REF");
        reference.setSource("http://www.google.de");
        finding.setReference(reference);
        finding.setCategory("TEST CATEGORY");
        finding.setName("TEST FINDING");
        finding.setDescription("Ouh Test!");
        finding.setHint("You might wan't to Test!");
        finding.setSeverity(Severity.LOW);
        finding.setOsiLayer(OsiLayer.DATA_LINK);
        finding.setLocation("udp://brot.securecodebox.io:99999");
        finding.addAttribute("TEST", "Kuchen");
        finding.addAttribute("HORRIBLE", "Beer");
        return finding;
    }

    public static Target createBaiscTarget() {
        return createTarget("udp://brot.securecodebox.io:1234", "Brot!");
    }

    public static Target createTarget(String location, String name) {
        Target target = new Target();
        target.setLocation(location);
        target.setName(name);
        target.appendAttribute("TEST", "Kuchen");
        target.appendAttribute("HORRIBLE", "Beer");
        target.appendAttribute("ObjeCT", new Reference());
        return target;
    }
}

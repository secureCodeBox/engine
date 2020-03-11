package io.securecodebox.scanprocess;

import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.findings.OsiLayer;
import io.securecodebox.model.findings.Reference;
import io.securecodebox.model.findings.Severity;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NmapToSshTransformListenerTest {

    private static NmapToSshTransformListener listener = new NmapToSshTransformListener();
    private static final String TARGET_HOST = "test-host";

    @Test
    protected void filteredServiceShouldBecomeNewTargets() {
        Finding finding1 = createFindingWithService("https");
        Finding finding2 = createFindingWithService("ssl");
        Finding finding3 = createFindingWithService("tls");
        Finding finding4 = createFindingWithService("ssh");
        List<Finding> findings = new ArrayList<Finding>();
        findings.add(finding1);
        findings.add(finding2);
        findings.add(finding3);
        findings.add(finding4);      
        assertEquals(1, listener.createTargetsFromFindings(findings).size(), "services are detected correctly");
    }

    @Test
    protected void noFilteredServiceFoundReturnEmptyTargets() {
        Finding finding1 = createFindingWithService("https");
        Finding finding2 = createFindingWithService("ssl");
        Finding finding3 = createFindingWithService("tls");
        List<Finding> findings = new ArrayList<Finding>();
        findings.add(finding1);
        findings.add(finding2);
        findings.add(finding3);
        assertEquals(0, listener.createTargetsFromFindings(findings).size(), "irrelevant services are not transformed into new targets");
    }

    public static Finding createFindingWithService(String service) {
        Finding finding = new Finding();
        finding.setId(UUID.fromString("49bf7fd3-8512-4d73-a28f-608e493cd726"));
        Reference reference = new Reference();
        reference.setId("UNI_CODE_STUFF");
        reference.setSource("RISCOOL");
        finding.setReference(reference);
        finding.setCategory("Open Port");
        finding.setName("BAD_TEST_FINDIG");
        finding.setDescription("Some coder has tested this!");
        finding.setHint("You might wan't to blame Rüdiger!");
        finding.setSeverity(Severity.HIGH);
        finding.setOsiLayer(OsiLayer.NOT_APPLICABLE);
        finding.setLocation("mett.brot.securecodebox.io");
        finding.addAttribute(OpenPortAttributes.ip_address, TARGET_HOST);
        finding.addAttribute(OpenPortAttributes.service, service);
        finding.addAttribute(OpenPortAttributes.port, 80);
        return finding;
    }
}
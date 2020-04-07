package io.securecodebox.scanprocess;

import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.OsiLayer;
import io.securecodebox.model.findings.Reference;
import io.securecodebox.model.findings.Severity;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NmapToSslyzeTransformListenerTest {

    private static NmapToSslyzeTransformListener listener = new NmapToSslyzeTransformListener();

    @Test
    protected void filteredServicesShouldBecomeNewTargets() {
        Finding finding1 = createFindingWithService("https");
        Finding finding2 = createFindingWithService("ssl");
        Finding finding3 = createFindingWithService("tls");
        Finding finding4 = createFindingWithService("ssh");
        List<Finding> findings = new ArrayList<Finding>();
        findings.add(finding1);
        findings.add(finding2);
        findings.add(finding3);
        findings.add(finding4);
        List<Target> targets = listener.createTargetsFromFindings(findings);  
        assertEquals(3, targets.size(), "services are detected correctly");
        assertTrue(targets.get(0).getLocation().contains("https"), "https service detected");
        assertTrue(targets.get(1).getLocation().contains("ssl"), "ssl service detected");
        assertTrue(targets.get(2).getLocation().contains("tls"), "tls service detected");
    }

    @Test
    protected void noFilteredServicesFoundReturnEmptyTargets() {
        Finding finding = createFindingWithService("ssh");
        List<Finding> findings = new ArrayList<Finding>();
        findings.add(finding);
        assertEquals(0, listener.createTargetsFromFindings(findings).size(), "irrelevant services are not transformed into new targets");
    }

    @Test
    protected void shouldUseFindingsHostnameForTargetIfSet() {
        Finding finding = createFindingWithService("https");
        finding.addAttribute("hostname", "foo.example.com");
        finding.addAttribute(OpenPortAttributes.port, 443);
        List<Finding> findings = new ArrayList<Finding>();
        findings.add(finding);

        List<Target> targets = listener.createTargetsFromFindings(findings);


        assertEquals(1, targets.size());
        assertEquals("foo.example.com:443", targets.get(0).getLocation());
    }

    @Test
    protected void shouldNotCrashAndUseTheIPWhenTheHostnameIsNull() {
        Finding finding = createFindingWithService("https");
        finding.addAttribute("hostname", null);
        finding.addAttribute(OpenPortAttributes.ip_address, "192.168.42.42");
        finding.addAttribute(OpenPortAttributes.port, 443);
        List<Finding> findings = new ArrayList<Finding>();
        findings.add(finding);

        List<Target> targets = listener.createTargetsFromFindings(findings);


        assertEquals(1, targets.size());
        assertEquals("192.168.42.42:443", targets.get(0).getLocation());
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
        finding.addAttribute(OpenPortAttributes.ip_address, service);
        finding.addAttribute(OpenPortAttributes.service, service);
        finding.addAttribute(OpenPortAttributes.port, 80);
        return finding;
    }
}
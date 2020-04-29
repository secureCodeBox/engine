package io.securecodebox.scanprocess.listener;

import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NmapToNiktoTransformListenerTest {

    private final NmapToNiktoTransformListener listener = new NmapToNiktoTransformListener();
    private final String TARGET_NAME = "test-host";
    private final String TARGET_LOCATION = "test-location";
    private final List<Target> oldTargets = new LinkedList<>();
    private final List<Finding> findings = new LinkedList<>();
    private Set<Target> newTargets = new HashSet<>();
    private String niktoPorts;

    @Test
    protected void relevantPortsShouldBeIntersection() {
        Target target = createTarget(TARGET_NAME, TARGET_LOCATION, "80, 443, 3000, 8080, 8443");
        Finding finding1 = createFinding(TARGET_LOCATION, 3000, "Open Port");
        Finding finding2 = createFinding(TARGET_LOCATION, 8080, "Open Port");
        Finding finding3 = createFinding(TARGET_LOCATION, 8443, "Open Port");
        oldTargets.add(target);
        findings.add(finding1);
        findings.add(finding2);
        findings.add(finding3);
        this.transform();
        assertEquals(1, newTargets.size(), "should create only one target");
        assertTrue(niktoPorts.contains("3000"), "should contain port 3000");
        assertTrue(niktoPorts.contains("8080"), "should contain port 8080");
        assertTrue(niktoPorts.contains("8443"), "should contain port 8443");
    }

    @Test
    protected void shouldFilterNonNumericPorts() {
        Target target = createTarget(TARGET_NAME, TARGET_LOCATION, "80, shouldBeGone, 3000");
        Finding finding = createFinding(TARGET_LOCATION, 3000, "Open Port");
        Finding finding2 = createFinding(TARGET_LOCATION, 80, "Open Port");
        oldTargets.add(target);
        findings.add(finding);
        findings.add(finding2);
        this.transform();
        assertEquals(1, newTargets.size(), "should create only one Target");
        assertTrue(niktoPorts.contains("80"), "should contain port 80");
        assertTrue(niktoPorts.contains("3000"), "should contain port 3000");
    }

    @Test
    protected void shouldContainAllOpenPorts() {
        Target target = createTarget(TARGET_NAME, TARGET_LOCATION, "80, 443, 3000, 8080, 8443");
        Finding finding1 = createFinding(TARGET_LOCATION, 80, "Open Port");
        Finding finding2 = createFinding(TARGET_LOCATION, 443, "Open Port");
        Finding finding3 = createFinding(TARGET_LOCATION, 3000, "Open Port");
        Finding finding4 = createFinding(TARGET_LOCATION, 8080, "Open Port");
        Finding finding5 = createFinding(TARGET_LOCATION, 8443, "Open Port");
        oldTargets.add(target);
        findings.add(finding1);
        findings.add(finding2);
        findings.add(finding3);
        findings.add(finding4);
        findings.add(finding5);
        transform();
        assertEquals(1, newTargets.size(), "should create only one target");
        assertTrue(niktoPorts.contains("80"), "should contain Port 80");
        assertTrue(niktoPorts.contains("443"), "should contain Port 443");
        assertTrue(niktoPorts.contains("3000"), "should contain port 3000");
        assertTrue(niktoPorts.contains("8080"), "should contain port 8080");
        assertTrue(niktoPorts.contains("8443"), "should contain port 8443");
    }

    @Test
    protected void shouldHandleEmptyTargetsList() {
        Finding finding = createFinding(TARGET_NAME, 3000, "Version Issue");
        findings.add(finding);
        transform();
        assertTrue(newTargets.isEmpty(), "no new targets should be created");
    }

    @Test
    protected void shouldHandleEmptyFindingsList() {
        Target target = createTarget(TARGET_NAME, TARGET_LOCATION, "80");
        oldTargets.add(target);
        this.transform();
        assertTrue(newTargets.isEmpty(), "no new targets should be created");
    }

    @Test
    protected void shouldHandleEmptyTargetAndFindingList() {
        this.transform();
        assertTrue(listener.nmapToNiktoTransformAction(findings, oldTargets).isEmpty());
    }

    @Test
    protected void shouldIgnoreDuplicateFindings() {
        Target target = createTarget(TARGET_NAME, TARGET_LOCATION, "80, shouldBeGone, 3000");
        Finding finding = createFinding(TARGET_LOCATION, 3000, "Open Port");
        Finding finding2 = createFinding(TARGET_LOCATION, 3000, "Open Port");
        oldTargets.add(target);
        findings.add(finding);
        findings.add(finding2);
        transform();
        assertEquals(1, newTargets.size(), "should create only one Target");
        assertEquals("3000", niktoPorts, "should be port 3000");
    }

    @Test
    protected void shouldIgnoreIrrelevantPorts() {
        Finding finding1 = createFinding(TARGET_LOCATION, 5555, "Open Port");
        Finding finding2 = createFinding(TARGET_LOCATION, 3000, "Open Port");

        findings.add(finding1);
        findings.add(finding2);

        Target target = createTarget(TARGET_NAME, TARGET_LOCATION, "3000, 8080");
        oldTargets.add(target);
        transform();
        assertFalse(niktoPorts.contains("5555"), "should not contain 5555");
    }

    @Test
    protected void shouldIgnoreClosedPorts() {
        Finding finding1 = createFinding(TARGET_LOCATION, 9999, "Open Port");
        Finding finding2 = createFinding(TARGET_LOCATION, 88888, "Open Port");
        findings.add(finding1);
        findings.add(finding2);
        Target target = createTarget(TARGET_NAME, TARGET_LOCATION, "3000, 8080");
        oldTargets.add(target);
        this.transform();
        assertTrue(newTargets.isEmpty());
    }

    @Test
    protected void shouldUseDefaultPorts() {
        Finding finding = createFinding(TARGET_LOCATION, 80, "Open Port");
        findings.add(finding);
        Target target = createTarget(TARGET_NAME, TARGET_LOCATION, "");
        oldTargets.add(target);
        this.transform();
        assertEquals(1, newTargets.size(), "should have exactly one target");
        assertEquals("80", niktoPorts, "should be equal to 3000 as result of using default ports");
    }

    @Test
    protected void shouldPerformBlackBoxScan() {
        Finding finding = createFinding(TARGET_LOCATION, 80, "Open Port");
        finding.addAttribute(OpenPortAttributes.service, "http");
        findings.add(finding);
        Target target = new Target();
        target.setName(TARGET_NAME);
        target.setLocation(TARGET_LOCATION);
        target.appendOrUpdateAttribute(NmapToNiktoTransformListener.ATTRIBUTE_COMBINED_NMAP_NIKTO_PORTS, "3000");
        target.appendOrUpdateAttribute(NmapToNiktoTransformListener.ATTRIBUTE_BLACKBOX, "true");
        oldTargets.add(target);
        this.transform();
        assertEquals("80", niktoPorts, "should be equal to 80 as result of blackbox test");
    }

    @Test
    protected void shouldNotPerformBlackBoxScan() {
        Finding finding = createFinding(TARGET_LOCATION, 80, "Open Port");
        findings.add(finding);
        Target target = new Target();
        target.setLocation(TARGET_LOCATION);
        target.setName(TARGET_NAME);
        target.appendOrUpdateAttribute(NmapToNiktoTransformListener.ATTRIBUTE_BLACKBOX, "false");
        target.appendOrUpdateAttribute(NmapToNiktoTransformListener.ATTRIBUTE_COMBINED_NMAP_NIKTO_PORTS, "3000");
        oldTargets.add(target);
        this.transform();
        assertTrue(newTargets.isEmpty());
    }

    @Test
    protected void testForCidrNotation() {
        Finding finding = createFinding("192.168.178.2", 443, "Open Port");
        findings.add(finding);
        Target target = createTarget(TARGET_NAME, "192.168.178.2/32", "80, 443");
        oldTargets.add(target);
        this.transform();
        assertFalse(newTargets.isEmpty());
    }

    private Finding createFinding(String hostname, int port, String category) {
        Finding finding = new Finding();
        finding.addAttribute(OpenPortAttributes.hostname, hostname);
        finding.addAttribute(OpenPortAttributes.port, port);
        finding.setCategory(category);
        return finding;
    }

    private Target createTarget(String name, String location, String combinedNmapNiktoPorts) {
        Target target = new Target();
        target.setLocation(location);
        target.setName(name);
        target.appendOrUpdateAttribute(NmapToNiktoTransformListener.ATTRIBUTE_COMBINED_NMAP_NIKTO_PORTS, combinedNmapNiktoPorts);
        return target;
    }

    private void transform() {
        this.newTargets = this.listener.nmapToNiktoTransformAction(this.findings, this.oldTargets);
        if (newTargets.iterator().hasNext())
            this.niktoPorts = (String) newTargets.iterator().next().getAttributes().get(NmapToNiktoTransformListener.ATTRIBUTE_NIKTO_PORTS);
    }
}
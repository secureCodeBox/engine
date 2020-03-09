package io.securecodebox.scanprocess.listener;

import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class NmapToNiktoTransformListenerTest {

    private static Set<String> portsToScanByNikto;
    private static NmapToNiktoTransformListener listener;

    private final String TARGET_NAME = "test-host";
    private final String TARGET_LOCATION = "test-location";

    @BeforeAll
    private static void setUp() {
        listener = new NmapToNiktoTransformListener();
    }

    @Test
    protected void relevantPortsShouldBeIntersection() {
        Target target = createTarget(TARGET_NAME, TARGET_LOCATION, "80, 443, 3000, 8080, 8443");
        Finding finding1 = createFinding(TARGET_LOCATION, "3000", "Open Port");
        Finding finding2 = createFinding(TARGET_LOCATION, "8080", "Open Port");
        Finding finding3 = createFinding(TARGET_LOCATION, "8443", "Open Port");
        List<Target> oldTargets = new LinkedList<>();
        List<Finding> findings = new LinkedList<>();
        oldTargets.add(target);
        findings.add(finding1);
        findings.add(finding2);
        findings.add(finding3);

        Set<Target> newTargets = listener.nmapToNiktoTransformAction(findings, oldTargets);
        assertEquals(1, newTargets.size(), "should create only one target");
        String niktoPorts = (String) newTargets.iterator().next().getAttributes().get("NIKTO_PORTS");
        assertTrue(niktoPorts.contains("3000"), "should contain port 3000");
        assertTrue(niktoPorts.contains("8080"), "should contain port 8080");
        assertTrue(niktoPorts.contains("8443"), "should contain port 8443");
    }

    @Test
    protected void shouldFilterNonNumericPorts() {
        Target target = createTarget(TARGET_NAME, TARGET_LOCATION, "80, shouldBeGone, 3000");
        Finding finding = createFinding(TARGET_LOCATION, "3000", "Open Port");
        Finding finding2 = createFinding(TARGET_LOCATION, "80", "Open Port");
        List<Target> oldTargets = new LinkedList<>();
        List<Finding> findings = new LinkedList<>();
        oldTargets.add(target);
        findings.add(finding);
        findings.add(finding2);
        Set<Target> newTargets = listener.nmapToNiktoTransformAction(findings, oldTargets);
        assertEquals(1, newTargets.size(), "should create only one Target");
        String niktoPorts = (String) newTargets.iterator().next().getAttributes().get("NIKTO_PORTS");
        assertTrue(niktoPorts.contains("80"), "should contain port 80");
        assertTrue(niktoPorts.contains("3000"), "should contain port 3000");
    }

    @Test
    protected void shouldContainAllOpenPorts() {
        Target target = createTarget(TARGET_NAME, TARGET_LOCATION, "80, 443, 3000, 8080, 8443");
        Finding finding1 = createFinding(TARGET_LOCATION, "80", "Open Port");
        Finding finding2 = createFinding(TARGET_LOCATION, "443", "Open Port");
        Finding finding3 = createFinding(TARGET_LOCATION, "3000", "Open Port");
        Finding finding4 = createFinding(TARGET_LOCATION, "8080", "Open Port");
        Finding finding5 = createFinding(TARGET_LOCATION, "8443", "Open Port");
        List<Target> oldTargets = new LinkedList<>();
        List<Finding> findings = new LinkedList<>();
        oldTargets.add(target);
        findings.add(finding1);
        findings.add(finding2);
        findings.add(finding3);
        findings.add(finding4);
        findings.add(finding5);

        Set<Target> newTargets = listener.nmapToNiktoTransformAction(findings, oldTargets);
        assertEquals(1, newTargets.size(), "should create only one target");
        String niktoPorts = (String) newTargets.iterator().next().getAttributes().get("NIKTO_PORTS");
        assertTrue(niktoPorts.contains("80"), "should contain Port 80");
        assertTrue(niktoPorts.contains("443"), "should contain Port 443");
        assertTrue(niktoPorts.contains("3000"), "should contain port 3000");
        assertTrue(niktoPorts.contains("8080"), "should contain port 8080");
        assertTrue(niktoPorts.contains("8443"), "should contain port 8443");
    }

    @Test
    protected void shouldHandleEmptyTargetsList() {
        Finding finding = createFinding(TARGET_NAME, "3000", "Version Issue");
        List<Finding> findings = new LinkedList<>();
        findings.add(finding);
        List<Target> oldTargets = new LinkedList<>();
        assertTrue(listener.nmapToNiktoTransformAction(findings, oldTargets).isEmpty(), "no new targets should be created");
    }

    @Test
    protected void shouldHandleEmptyFindingsList() {
        List<Finding> findings = new LinkedList<>();
        List<Target> oldTargets = new LinkedList<>();
        Target target = createTarget(TARGET_NAME, TARGET_LOCATION, "80");
        oldTargets.add(target);
        assertTrue(listener.nmapToNiktoTransformAction(findings, oldTargets).isEmpty(), "no new targets should be created");
    }

    @Test
    protected void shouldHandleEmptyTargetAndFindingList() {
        List<Finding> findings = new LinkedList<>();
        List<Target> oldTargets = new LinkedList<>();
        assertTrue(listener.nmapToNiktoTransformAction(findings, oldTargets).isEmpty());
    }

    @Test
    protected void shouldIgnoreDuplicateFindings() {
        Target target = createTarget(TARGET_NAME, TARGET_LOCATION, "80, shouldBeGone, 3000");
        Finding finding = createFinding(TARGET_LOCATION, "3000", "Open Port");
        Finding finding2 = createFinding(TARGET_LOCATION, "3000", "Open Port");
        List<Target> oldTargets = new LinkedList<>();
        List<Finding> findings = new LinkedList<>();
        oldTargets.add(target);
        findings.add(finding);
        findings.add(finding2);
        Set<Target> newTargets = listener.nmapToNiktoTransformAction(findings, oldTargets);
        assertEquals(1, newTargets.size(), "should create only one Target");
        String niktoPorts = (String) newTargets.iterator().next().getAttributes().get("NIKTO_PORTS");
        assertEquals("3000", niktoPorts, "should be port 3000");
    }

    @Test
    protected void shouldIgnoreIrrelevantPorts() {
        Finding finding1 = createFinding(TARGET_LOCATION, "5555", "Open Port");
        Finding finding2 = createFinding(TARGET_LOCATION, "3000", "Open Port");

        List<Finding> findings = new LinkedList<>();
        findings.add(finding1);
        findings.add(finding2);

        List<Target> oldTargets = new LinkedList<>();
        Target target1 = createTarget(TARGET_NAME, TARGET_LOCATION, "3000, 8080");
        oldTargets.add(target1);

        Set<Target> newTargets = listener.nmapToNiktoTransformAction(findings, oldTargets);
        String niktoPorts = (String) newTargets.iterator().next().getAttributes().get("NIKTO_PORTS");

        assertFalse(niktoPorts.contains("5555"), "should not contain 5555");
    }

    @Test
    protected void shouldIgnoreClosedPorts() {
        Finding finding1 = createFinding(TARGET_LOCATION, "9999", "Open Port");
        Finding finding2 = createFinding(TARGET_LOCATION, "88888", "Open Port");

        List<Finding> findings = new LinkedList<>();
        findings.add(finding1);
        findings.add(finding2);

        Target target = createTarget(TARGET_NAME, TARGET_LOCATION, "3000, 8080");

        List<Target> oldTargets = new LinkedList<>();
        oldTargets.add(target);
        Set<Target> newTargets = listener.nmapToNiktoTransformAction(findings, oldTargets);

        assertTrue(newTargets.isEmpty());
    }

    private Finding createFinding(String hostname, String port, String category) {
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
        target.appendOrUpdateAttribute("COMBINED_NMAP_NIKTO_PORTS", combinedNmapNiktoPorts);
        return target;
    }
}
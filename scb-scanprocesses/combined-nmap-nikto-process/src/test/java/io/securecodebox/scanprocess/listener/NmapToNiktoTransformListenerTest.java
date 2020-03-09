package io.securecodebox.scanprocess.listener;

import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NmapToNiktoTransformListenerTest {

    private static Set<String> portsToScanByNikto;
    private static NmapToNiktoTransformListener listener;

    private final String TARGET_NAME = "test-host";
    private final String TARGET_LOCATION = "test-location";

    @BeforeAll
    private static void setUp() {
        portsToScanByNikto = new HashSet<>();
        portsToScanByNikto.add("80");
        portsToScanByNikto.add("443");
        portsToScanByNikto.add("3000");
        portsToScanByNikto.add("8080");
        portsToScanByNikto.add("8443");

        listener = new NmapToNiktoTransformListener();
    }

    @Test
    protected void relevantPortsShouldBeIntersection() {
        Set<String> openPorts = new HashSet<>();
        openPorts.add("80");
        openPorts.add("3000");
        openPorts.add("9999");
        Set<String> intersection = new HashSet<>();
        intersection.add("80");
        intersection.add("3000");
        assertEquals(intersection, listener.filterIrrelevantPorts(portsToScanByNikto, openPorts), "intersection should be equal to relevant ports");
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
        assertTrue(niktoPorts.contains("80" ), "should contain port 80");
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
        assertTrue(niktoPorts.contains("443"),  "should contain Port 443");
        assertTrue(niktoPorts.contains("3000"), "should contain port 3000");
        assertTrue(niktoPorts.contains("8080"), "should contain port 8080");
        assertTrue(niktoPorts.contains("8443"), "should contain port 8443");
    }

    @Test
    protected void shouldFindOpenPort() {
        Finding finding = createFinding(TARGET_NAME, "3000", "Open Port");
        List<Finding> findings = new LinkedList<>();
        findings.add(finding);
        Map<String, Set<String>> openPortsPerTarget = listener.getOpenPortsPerTarget(findings);
        assertTrue(openPortsPerTarget.get(TARGET_NAME).contains("3000"), "should contain open port 3000");
    }

    @Test
    protected void shouldNotFindOpenPorts() {
        Finding finding = createFinding(TARGET_NAME, "3000", "Version Issue");
        List<Finding> findings = new LinkedList<>();
        findings.add(finding);
        Map<String, Set<String>> openPortsPerTarget = listener.getOpenPortsPerTarget(findings);
        assertTrue(openPortsPerTarget.isEmpty());
    }

    @Test
    protected void shouldIgnoreEmptyFindingsList() {
        List<Finding> findings = new LinkedList<>();
        Map<String, Set<String>> openPortsPerTarget = listener.getOpenPortsPerTarget(findings);
        assertTrue(openPortsPerTarget.isEmpty());
    }

    @Test
    protected void shouldIgnoreDuplicateFindings() {
        Finding finding = createFinding(TARGET_NAME, "3000", "Open Port");
        Finding finding2 = createFinding(TARGET_NAME, "3000", "Open Port");

        List<Finding> findings = new LinkedList<>();

        findings.add(finding);
        findings.add(finding2);

        Map<String, Set<String>> openPortsPerTarget = listener.getOpenPortsPerTarget(findings);
        assertEquals(1, openPortsPerTarget.size());
    }

    @Test
    protected void shouldHandleEmptyFindingsList() {
        List<Finding> findings = new LinkedList<>();
        Map<String, Set<String>> openPortsPerTarget = listener.getOpenPortsPerTarget(findings);
        assertTrue(openPortsPerTarget.isEmpty());
    }

    @Test
    protected void shouldTransformTargetsToEmptyTargetList() {
        Finding finding = createFinding(TARGET_NAME, "3000", "Open Port");

        List<Finding> findings = new LinkedList<>();
        findings.add(finding);

        List<Target> oldTargets = new LinkedList<>();
        Set<Target> newTargets = listener.nmapToNiktoTransformAction(findings, oldTargets);

        assertTrue(newTargets.isEmpty());
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

        List<String> nmapPorts = new LinkedList<>();
        newTargets.forEach(target -> nmapPorts.add((String) target.getAttributes().get("NIKTO_PORTS")));
        assertEquals("3000", nmapPorts.get(0));
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
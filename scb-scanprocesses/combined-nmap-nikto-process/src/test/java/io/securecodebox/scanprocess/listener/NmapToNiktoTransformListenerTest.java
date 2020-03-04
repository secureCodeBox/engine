package io.securecodebox.scanprocess.listener;

import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class NmapToNiktoTransformListenerTest {

    private static Set<String> portsToScanByNikto;
    private static NmapToNiktoTransformListener listener;

    private final String TARGET_HOST = "test-host";
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
    protected void relevantPortsShouldBeEqualToOpenPorts() {
        Set<String> openPorts = new HashSet<>();
        openPorts.add("80");
        openPorts.add("443");
        assertEquals(listener.filterIrrelevantPorts(portsToScanByNikto, openPorts), openPorts, "relevant Ports are equal to open ports");
    }

    @Test
    protected void shouldFilterIrrelevantPort() {
        Set<String> openPorts = new HashSet<>();
        openPorts.add("999999");
        assertTrue(listener.filterIrrelevantPorts(portsToScanByNikto, openPorts).isEmpty(), "relevant ports are empty");
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
    protected void shouldFilterNonNumericChars() {
        Target target = new Target();
        target.setName("Test Case");
        target.setLocation(TARGET_LOCATION);
        target.appendOrUpdateAttribute("COMBINED_NMAP_NIKTO_PORTS", "80, shouldBeGone, 3000");
        assertFalse(listener.getRelevantPorts(target).contains("shouldBeGone"));
    }

    @Test
    protected void shouldFindOpenPort() {
        Finding finding = createFinding(TARGET_HOST, "3000", "Open Port");
        List<Finding> findings = new LinkedList<>();
        findings.add(finding);
        Map<String, Set<String>> openPortsPerTarget = listener.getOpenPortsPerTarget(findings);
        assertTrue(openPortsPerTarget.get(TARGET_HOST).contains("3000"), "should contain open port 3000");
    }

    @Test
    protected void shouldNotFindOpenPorts() {
        Finding finding = createFinding(TARGET_HOST, "3000", "Version Issue");
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
        Finding finding = createFinding(TARGET_HOST, "3000", "Open Port");
        Finding finding2 = createFinding(TARGET_HOST, "3000", "Open Port");

        List<Finding> findings = new LinkedList<>();

        findings.add(finding);
        findings.add(finding2);

        Map<String, Set<String>> openPortsPerTarget = listener.getOpenPortsPerTarget(findings);
        assertTrue(openPortsPerTarget.size() == 1);
    }

    @Test
    protected void shouldHandleEmptyFindingsList() {
        List<Finding> findings = new LinkedList<>();
        Map<String, Set<String>> openPortsPerTarget = listener.getOpenPortsPerTarget(findings);
        assertTrue(openPortsPerTarget.isEmpty());
    }

    @Test
    protected void shouldTransformTargetsToEmptyTargetList() {
        Finding finding = createFinding(TARGET_HOST, "3000", "Open Port");

        List<Finding> findings = new LinkedList<>();
        findings.add(finding);

        Set<Target> newTargets = new HashSet<>();
        List<Target> oldTargets = new LinkedList<>();
        newTargets = listener.nmapToNiktoTransformAction(findings, oldTargets);

        assertTrue(newTargets.isEmpty());
    }

    private Finding createFinding(String hostname, String port, String category) {
        Finding finding = new Finding();
        finding.addAttribute(OpenPortAttributes.hostname, hostname);
        finding.addAttribute(OpenPortAttributes.port, port);
        finding.setCategory(category);
        return finding;
    }
}
package io.securecodebox.scanprocess.listener;

import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NmapToNiktoTransformListenerTest {

    private static Set<String> portsToScanByNikto;
    private static NmapToNiktoTransformListener listener;
    private static Set<String> openPorts;
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
        openPorts = new HashSet<>();
    }

    @AfterEach
    private void clearOpenPorts() {
        openPorts.clear();
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
        openPorts.add("999999");
        assertTrue(listener.filterIrrelevantPorts(portsToScanByNikto, openPorts).isEmpty(), "relevant ports are empty");
    }

    @Test
    protected void relevantPortsShouldBeIntersection() {
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
        assertFalse(listener.getSanitizedPortSet(target).contains("shouldBeGone"));
    }

    @Test
    protected void shouldFindOpenPort() {
        Finding finding = new Finding();
        finding.setCategory("Open Port");
        finding.addAttribute(OpenPortAttributes.port, "3000");
        finding.addAttribute(OpenPortAttributes.hostname, TARGET_HOST);
        List<Finding> findings = new LinkedList<>();
        findings.add(finding);
        Map<String, Set<String>> openPortsPerTarget = listener.findOpenPortsPerTarget(findings);
        assertTrue(openPortsPerTarget.get(TARGET_HOST).contains("3000"), "should contain open port 3000");
    }

}
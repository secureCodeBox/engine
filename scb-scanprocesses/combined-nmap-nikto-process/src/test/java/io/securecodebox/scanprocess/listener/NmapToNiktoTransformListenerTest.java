package io.securecodebox.scanprocess.listener;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.*;

class NmapToNiktoTransformListenerTest {

    private static Set<String> portsToScanByNikto;
    private static NmapToNiktoTransformListener listener;
    private static Set<String> openPorts;

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

        assertTrue("relevant Ports are equal to open ports", listener.filterIrrelevantPorts(portsToScanByNikto, openPorts).equals(openPorts));
    }

    @Test
    protected void relevantPortsShouldBeEmpty() {
        openPorts.add("999999");
        assertTrue("relevant ports are empty", listener.filterIrrelevantPorts(portsToScanByNikto, openPorts).isEmpty());
    }

    @Test
    protected void relevantPortsShouldBeIntersection() {
        openPorts.add("80");
        openPorts.add("3000");
        openPorts.add("9999");

        Set<String> intersection = new HashSet<>();
        intersection.add("80");
        intersection.add("3000");

        assertEquals("intersection should be equal to relevant ports", intersection, listener.filterIrrelevantPorts(portsToScanByNikto, openPorts));
    }

}
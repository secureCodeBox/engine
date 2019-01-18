package io.securecodebox.scanprocess.nmap.util;

import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.findings.OsiLayer;
import io.securecodebox.model.findings.Severity;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class HttpHeaderStrategyTest {

    private HttpHeaders headers;
    private Finding finding;

    @Before
    public void init () {
        final StringBuilder rawHeaders = new StringBuilder();
        rawHeaders.append("Content-Type: text/imaginary\n");
        rawHeaders.append("Location: https://localhost:443/\n");
        rawHeaders.append("Done: yeah;\n\n");
        headers = new HttpHeaders(rawHeaders.toString());

        finding = new Finding();
        finding.setName("DefaultName");
        finding.setId(UUID.randomUUID());
        finding.setDescription("DefaultDescription");
        finding.setOsiLayer(OsiLayer.NOT_APPLICABLE);
        finding.setCategory("DefaultCategory");
    }

    @Test
    public void testHttpHeaders () {
        assertEquals(true, headers.has("Content-Type"));
        assertEquals("text/imaginary", headers.get("Content-Type"));
        assertEquals(true, headers.has("Location"));
        assertEquals("https://localhost:443/", headers.get("Location"));
        assertEquals(true, headers.has("Done"));
        assertEquals("yeah;", headers.get("Done"));
        assertEquals(false, headers.has("Nothing"));
        assertNull(headers.get("Nothing"));
    }

    @Test
    public void testExistingHeader () {
        HttpHeaderStrategy strategy = createStrategy("Location");
        ArrayList<Finding> findings = strategy.apply(headers, finding);

        assertEquals(2, findings.size());
        assertEquals(findings.get(0).getName(), "Location header present");
        assertEquals(findings.get(1).getName(), "Does not start with 0");
    }

    @Test
    public void testNonExistantHeader () {
        HttpHeaderStrategy strategy = createStrategy("Nothing");
        ArrayList<Finding> findings = strategy.apply(headers, finding);

        assertEquals(1, findings.size());
        assertEquals(findings.get(0).getName(), "Nothing header missing");
    }

    private HttpHeaderStrategy createStrategy(String headerName) {
        return new HttpHeaderStrategy(headerName)

                .ifPresent ()
                .createFinding (Severity.MEDIUM, headerName + " header present")

                .ifMissingCreateFinding(Severity.MEDIUM)

                .ifTrue(value -> !value.startsWith ("0"))
                .createFinding (Severity.MEDIUM, "Does not start with 0", value -> "Actual value: " + value);
    }

}
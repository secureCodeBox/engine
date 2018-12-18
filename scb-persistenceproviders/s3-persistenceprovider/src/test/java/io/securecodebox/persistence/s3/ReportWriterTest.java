package io.securecodebox.persistence.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.findings.Severity;
import io.securecodebox.model.rest.Report;
import io.securecodebox.model.securitytest.SecurityTest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;


import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportWriterTest {

    @InjectMocks
    private ReportWriter reportWriter;

    @Spy
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testWriteReportToFile() throws IOException {
        // given
        Finding finding = new Finding();
        finding.setId(UUID.fromString("49bf7fd3-8512-4d73-a28f-608e493cd726"));
        finding.setDescription("BAD_TEST_FINDIG_DESC");
        finding.setSeverity(Severity.HIGH);

        List<Finding> findings = new LinkedList<>();
        findings.add(finding);

        Report report = new Report(findings, "<rawFindings/>");
        report.setId(UUID.fromString("49bf7fd3-8512-4d73-a28f-608e493cd781"));

        Map<String, String> metaData = new HashMap<>();
        metaData.put("started-by", "PersistencProviderTest");

        SecurityTest securityTest = new SecurityTest(UUID.fromString("281ccc0f-a933-4106-a3d3-209954e6305e"), "testContext","nmap", null, report, metaData);

        // when
        File file = this.reportWriter.writeReportToFile(securityTest);

        // then
        String content = FileUtils.readFileToString(file, "UTF-8");
        assertEquals(
                "{\"context\":\"testContext\"," +
                        "\"target\":null," +
                        "\"metaData\":{\"started-by\":\"PersistencProviderTest\"}," +
                        "\"id\":\"281ccc0f-a933-4106-a3d3-209954e6305e\"," +
                        "\"name\":\"nmap\"," +
                        "\"finished\":true}",
                content
        );
    }

    @Test
    public void testNullReport() throws IOException {
        File file = reportWriter.writeReportToFile(null);
        assertEquals("null", readFile(file.getPath(), Charset.forName("UTF-8")));
    }

    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}

/*
 *
 *  SecureCodeBox (SCB)
 *  Copyright 2015-2018 iteratec GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package io.securecodebox.persistence.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.findings.Severity;
import io.securecodebox.model.rest.Report;
import io.securecodebox.model.securitytest.SecurityTest;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static junit.framework.TestCase.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class S3PersistenceProviderTest {

    @Spy
    ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    S3PersistenceProvider s3PersistenceProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testWriteReportToFile() throws IOException {
        Finding finding = new Finding();
        finding.setId(UUID.fromString("49bf7fd3-8512-4d73-a28f-608e493cd726"));
        finding.setDescription("BAD_TEST_FINDIG_DESC");
        finding.setName("BAD_TEST_FINDIG");
        finding.setSeverity(Severity.HIGH);

        List<Finding> findings = new LinkedList<>();
        findings.add(finding);

        Report report = new Report(findings, "<rawFindings/>");
        report.setId(UUID.fromString("49bf7fd3-8512-4d73-a28f-608e493cd781"));

        Map<String, String> metaData = new HashMap<>();
        metaData.put("started-by", "PersistencProviderTest");

        SecurityTest securityTest = new SecurityTest(UUID.fromString("281ccc0f-a933-4106-a3d3-209954e6305e"), "testContext","nmap", null, report, metaData);
        
        File file = s3PersistenceProvider.writeReportToFile(securityTest);
        String content = FileUtils.readFileToString(file, "UTF-8");
        assertEquals(
            "{\"context\":\"testContext\",\"target\":null,\"metaData\":{\"started-by\":\"PersistencProviderTest\"},\"id\":\"281ccc0f-a933-4106-a3d3-209954e6305e\",\"report\":{\"report_id\":\"49bf7fd3-8512-4d73-a28f-608e493cd781\",\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"BAD_TEST_FINDIG_DESC\",\"severity\":\"HIGH\",\"false_positive\":false}],\"raw_findings\":\"<rawFindings/>\",\"severity_highest\":\"HIGH\",\"severity_overview\":{\"HIGH\":1}},\"name\":\"nmap\",\"finished\":true}",
            content
        );
    }

    @Test
    public void testNullReport() throws IOException {
        File file = s3PersistenceProvider.writeReportToFile(null);
        assertEquals("null", readFile(file.getPath(), Charset.forName("UTF-8")));
    }

    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}

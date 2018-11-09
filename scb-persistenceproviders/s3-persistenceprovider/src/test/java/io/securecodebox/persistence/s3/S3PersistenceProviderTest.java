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
import io.securecodebox.model.Report;
import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.Scanner;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.findings.Severity;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class S3PersistenceProviderTest {

    ScanProcessExecution execution = new ScanProcessExecution() {
        @Override
        public UUID getId() {
            return UUID.fromString("23701e7b-c9ec-46c9-aae8-57f5520f6c6c");
        }

        @Override
        public void setContext(String id) {
        }

        @Override
        public String getContext() {
            return "KEKSE!!";
        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public boolean hasScanner() {
            return false;
        }

        @Override
        public void addScanner(Scanner scanner) {
        }

        @Override
        public List<Scanner> getScanners() {
            return Collections.emptyList();
        }

        @Override
        public List<Finding> getFindings() {
            Finding finding = new Finding();
            finding.setId(UUID.fromString("49bf7fd3-8512-4d73-a28f-608e493cd726"));
            finding.setDescription("BAD_TEST_FINDIG_DESC");
            finding.setName("BAD_TEST_FINDIG");
            finding.setSeverity(Severity.HIGH);
            findings.add(finding);
            ;
            return findings;
        }

        @Override
        public String getRawFindings() {
            return "";
        }

        @Override
        public void clearFindings() {
        }

        @Override
        public void appendFinding(Finding finding) {
        }

        @Override
        public void appendTarget(Target target) {
        }

        @Override
        public List<Target> getTargets() {
            return Collections.emptyList();
        }

        @Override
        public void clearTargets() {
        }

        @Override
        public boolean isAutomated() {
            return false;
        }

        @Override
        public String getScannerType() {
            return "TEST";
        }
    };

    @Spy
    ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    S3PersistenceProvider s3PersistenceProvider;

    List<Finding> findings = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testWriteReportToFile() throws IOException {
        Report report = new Report(execution);
        report.setId(UUID.fromString("281ccc0f-a933-4106-a3d3-209954e6305e"));
        
        File file = s3PersistenceProvider.writeReportToFile(report);
        String content = FileUtils.readFileToString(file, "UTF-8");
        assertEquals(
                "{\"execution\":{\"id\":\"23701e7b-c9ec-46c9-aae8-57f5520f6c6c\",\"context\":\"KEKSE!!\",\"automated\":false,\"scanner_type\":\"TEST\",\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"BAD_TEST_FINDIG_DESC\",\"severity\":\"HIGH\",\"false_positive\":false}]},\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"BAD_TEST_FINDIG_DESC\",\"severity\":\"HIGH\",\"false_positive\":false},{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"BAD_TEST_FINDIG_DESC\",\"severity\":\"HIGH\",\"false_positive\":false}],\"severity_highest\":\"HIGH\",\"severity_overview\":{\"HIGH\":4},\"report_id\":\"281ccc0f-a933-4106-a3d3-209954e6305e\"}",
                content);
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

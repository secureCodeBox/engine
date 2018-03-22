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

package io.securecodebox.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.execution.DefaultScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.results.FindingTest;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 09.03.18
 */
public class ReportTest {

    public static final String DEFAULT_RESULT_STRING="{\"execution\":{\"id\":\"5a4e9d37-09b0-4109-badd-d79dfa8fce2a\",\"context\":\"TEST_CONTEXT\",\"automated\":false,\"spiderId\":\"5b419056-8907-4f68-8bb5-dc8a93065b6b\",\"spiderType\":\"TestSpider\",\"scannerId\":\"b3e2ae19-5660-4af4-9ec8-6ed94eb9b67b\",\"scannerType\":\"TestScanner\"},\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"serverity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd716\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"serverity\":\"LOW\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Pudding\",\"HORRIBLE\":\"Coffee\"},\"location\":\"kaese.brot.securecodebox.io\"}]}";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSerialize() throws Exception {
        DelegateExecution process = DefaultScanProcessExecutionTest.mockDelegateExcecution();
        Finding basicFinding = FindingTest.createBasicFinding();
        ScanProcessExecution execution = new DefaultScanProcessExecution(process);
        execution.appendFinding(basicFinding);
        Report report = new Report(execution);

        String result = objectMapper.writeValueAsString(report);

        System.out.println(result);
        assertEquals(DEFAULT_RESULT_STRING, result);
    }
}

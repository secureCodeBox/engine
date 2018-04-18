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
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.DefaultScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.results.FindingTest;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 09.03.18
 */
public class ReportTest {

    public static final String DEFAULT_RESULT_STRING = "{\"execution\":{\"id\":\"5a4e9d37-09b0-4109-badd-d79dfa8fce2a\",\"context\":\"TEST_CONTEXT\",\"automated\":false,\"scanners\":[{\"id\":\"62fa8ffb-e3bc-433e-b322-9c02108c5171\",\"type\":\"Test_SCANNER\",\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"[{\\\"pudding\\\":\\\"Bier\\\"}]\"}],\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}]},\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"severity_highest\":\"HIGH\",\"severity_overview\":{\"HIGH\":1},\"report_id\":\"3cd25d74-2a31-41cd-9d71-9cc70c0d3169\"}";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSerialize() throws Exception {
        DelegateExecution process = DefaultScanProcessExecutionTest.mockDelegateExcecution();
        when(process.getVariable(DefaultFields.PROCESS_FINDINGS.name())).thenReturn(null);
        when(process.hasVariable(DefaultFields.PROCESS_FINDINGS.name())).thenReturn(false);

        Finding basicFinding = FindingTest.createBasicFinding();
        ScanProcessExecution execution = new DefaultScanProcessExecution(process);
        execution.appendFinding(basicFinding);

        when(process.hasVariable(DefaultFields.PROCESS_FINDINGS.name())).thenReturn(true);
        when(process.getVariable(DefaultFields.PROCESS_FINDINGS.name())).thenReturn(
                "[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}]");

        Report report = new Report(execution);
        report.setId(UUID.fromString("3cd25d74-2a31-41cd-9d71-9cc70c0d3169"));

        String result = objectMapper.writeValueAsString(report);

        System.out.println(result);
        assertEquals(DEFAULT_RESULT_STRING, result);

        verify(process, times(1)).setVariable(eq(DefaultFields.PROCESS_FINDINGS.name()), any());
    }
}

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
import io.securecodebox.model.execution.ScannerTest;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 09.03.18
 */
public class DefaultScanProcessExecutionTest {

    private static final String DEFAULT_EXECUTION = "{\"id\":\"5a4e9d37-09b0-4109-badd-d79dfa8fce2a\",\"context\":\"TEST_CONTEXT\",\"automated\":false,\"scanners\":[{\"id\":\"62fa8ffb-e3bc-433e-b322-9c02108c5171\",\"type\":\"Test_SCANNER\",\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"[{\\\"pudding\\\":\\\"Bier\\\"}]\"}]}";

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSerialize() throws Exception {
        DelegateExecution process = mockDelegateExcecution();

        ScanProcessExecution execution = new DefaultScanProcessExecution(process);
        String s = objectMapper.writeValueAsString(execution);

        System.out.println(s);
        assertEquals(DEFAULT_EXECUTION, s);
    }

    public static DelegateExecution mockDelegateExcecution() {
        DelegateExecution process = mock(DelegateExecution.class);
        when(process.getVariableTyped(DefaultFields.PROCESS_CONTEXT.name())).thenReturn(
                new PrimitiveTypeValueImpl.StringValueImpl("TEST_CONTEXT"));
        when(process.getId()).thenReturn("5a4e9d37-09b0-4109-badd-d79dfa8fce2a");
        when(process.getVariable(DefaultFields.PROCESS_SCANNERS.name())).thenReturn(
                "[" + ScannerTest.SERIALIZE_RESULT + "]");

        return process;
    }

}

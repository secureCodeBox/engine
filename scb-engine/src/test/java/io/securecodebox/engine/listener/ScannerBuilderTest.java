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

package io.securecodebox.engine.listener;

import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Scanner;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 17.04.18
 */
public class ScannerBuilderTest {
    private String defaultFindingJson = "[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}]";

    @Mock
    DelegateExecution executionMock;

    ScannerBuilder underTest = ScannerBuilder.init();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(executionMock.hasVariable(DefaultFields.PROCESS_SCANNER_ID.name())).thenReturn(true);
        when(executionMock.getVariableTyped(DefaultFields.PROCESS_SCANNER_ID.name())).thenReturn(
                new PrimitiveTypeValueImpl.StringValueImpl("fc12a44c-fe8d-4acd-97b8-375fa7e4e5dc"));

        when(executionMock.hasVariable(DefaultFields.PROCESS_SCANNER_TYPE.name())).thenReturn(true);
        when(executionMock.getVariableTyped(DefaultFields.PROCESS_SCANNER_TYPE.name())).thenReturn(
                new PrimitiveTypeValueImpl.StringValueImpl("TEST_SCANNER"));

        when(executionMock.hasVariable(eq(DefaultFields.PROCESS_FINDINGS.name()))).thenReturn(true);
        when(executionMock.getVariable(eq(DefaultFields.PROCESS_FINDINGS.name()))).thenAnswer(
                (answer) -> defaultFindingJson);

    }

    @Test
    public void testInitExecutionCorrect() {
        Scanner scanner = underTest.buildByExecution(executionMock);

        assertEquals(UUID.fromString("fc12a44c-fe8d-4acd-97b8-375fa7e4e5dc"), scanner.getScannerId());
        assertEquals("TEST_SCANNER", scanner.getScannerType());
        assertEquals(1, scanner.getFindings().size());

    }

    @Test(expected = IllegalStateException.class)
    public void testInitExecutionNoId() {
        when(executionMock.getVariableTyped(DefaultFields.PROCESS_SCANNER_ID.name())).thenReturn(null);

        underTest.buildByExecution(executionMock);
    }
}

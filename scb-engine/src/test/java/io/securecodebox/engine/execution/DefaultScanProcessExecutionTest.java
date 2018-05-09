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

package io.securecodebox.engine.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.TestHelper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import io.securecodebox.model.findings.OsiLayer;
import io.securecodebox.model.findings.Severity;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 09.03.18
 */
public class DefaultScanProcessExecutionTest {

    private static final String DEFAULT_EXECUTION = "{\"id\":\"5a4e9d37-09b0-4109-badd-d79dfa8fce2a\",\"context\":\"TEST_CONTEXT\",\"automated\":false,\"scanners\":[{\"id\":\"62fa8ffb-e3bc-433e-b322-9c02108c5171\",\"type\":\"Test_SCANNER\",\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"[{\\\"pudding\\\":\\\"Bier\\\"}]\"}]}";
    public static final String SCANNER_SERIALIZE_RESULT = "{\"id\":\"62fa8ffb-e3bc-433e-b322-9c02108c5171\",\"type\":\"Test_SCANNER\",\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"[{\\\"pudding\\\":\\\"Bier\\\"}]\"}";

    String findingCache = "";
    String targetCache = "";

    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    ScanProcessExecutionFactory processExecutionFactory;
    @Mock
    DelegateExecution executionMock;

    DefaultScanProcessExecution underTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new DefaultScanProcessExecution(executionMock);

        when(processExecutionFactory.get(executionMock)).thenReturn(underTest);
        when(executionMock.hasVariable(eq(DefaultFields.PROCESS_FINDINGS.name()))).thenReturn(true);
        when(executionMock.getVariable(eq(DefaultFields.PROCESS_FINDINGS.name()))).thenAnswer((answer) -> findingCache);
        doAnswer((Answer) invocation -> {
            findingCache = (String) invocation.getArgumentAt(1, ObjectValueImpl.class).getValue();
            return Void.TYPE;
        }).when(executionMock).setVariable(eq(DefaultFields.PROCESS_FINDINGS.name()), any());

        when(executionMock.hasVariable(eq(DefaultFields.PROCESS_TARGETS.name()))).thenReturn(true);
        when(executionMock.getVariable(eq(DefaultFields.PROCESS_TARGETS.name()))).thenAnswer((answer) -> targetCache);
        doAnswer((Answer) invocation -> {
            targetCache = (String) invocation.getArgumentAt(1, ObjectValueImpl.class).getValue();
            return Void.TYPE;
        }).when(executionMock).setVariable(eq(DefaultFields.PROCESS_TARGETS.name()), any());
    }

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
                "[" + SCANNER_SERIALIZE_RESULT + "]");

        return process;
    }

    @Test
    public void testAppendAndClearFindings() throws Exception {

        //
        // Append findings!
        //

        UUID finding1Id = UUID.randomUUID();
        UUID finding2Id = UUID.randomUUID();

        underTest.appendFinding(TestHelper.createBasicFinding(finding1Id));
        underTest.appendFinding(TestHelper.createBasicFindingDifferent(finding2Id));

        Mockito.verify(executionMock, times(2)).setVariable(eq(DefaultFields.PROCESS_FINDINGS.name()), anyString());

        ScanProcessExecution processExecution = processExecutionFactory.get(executionMock);

        assertEquals(2, processExecution.getFindings().size());

        // First Finding
        assertEquals("Some coder has tested this!", processExecution.getFindings().get(0).getDescription());
        assertEquals("COOL_TEST_STUFF", processExecution.getFindings().get(0).getCategory());
        assertEquals(OsiLayer.NOT_APPLICABLE, processExecution.getFindings().get(0).getOsiLayer());
        assertEquals(Severity.HIGH, processExecution.getFindings().get(0).getSeverity());
        assertEquals("BAD_TEST_FINDIG", processExecution.getFindings().get(0).getName());
        assertEquals(finding1Id, processExecution.getFindings().get(0).getId());
        assertEquals("mett.brot.securecodebox.io", processExecution.getFindings().get(0).getLocation());
        assertEquals(2, processExecution.getFindings().get(0).getAttributes().size());

        assertEquals("Kekse", processExecution.getFindings().get(0).getAttributes().get("TEST"));
        assertEquals("Coke", processExecution.getFindings().get(0).getAttributes().get("HORRIBLE"));

        // Secound Finding
        assertEquals("Ouh Test!", processExecution.getFindings().get(1).getDescription());
        assertEquals("TEST CATEGORY", processExecution.getFindings().get(1).getCategory());
        assertEquals(OsiLayer.DATA_LINK, processExecution.getFindings().get(1).getOsiLayer());
        assertEquals(Severity.LOW, processExecution.getFindings().get(1).getSeverity());
        assertEquals("TEST FINDING", processExecution.getFindings().get(1).getName());
        assertEquals(finding2Id, processExecution.getFindings().get(1).getId());
        assertEquals("udp://brot.securecodebox.io:99999", processExecution.getFindings().get(1).getLocation());
        assertEquals(2, processExecution.getFindings().get(1).getAttributes().size());

        assertEquals("Kuchen", processExecution.getFindings().get(1).getAttributes().get("TEST"));
        assertEquals("Beer", processExecution.getFindings().get(1).getAttributes().get("HORRIBLE"));

        //
        // Clear findings!
        //
        underTest.clearFindings();

        Mockito.verify(executionMock, atLeastOnce()).getVariable(eq(DefaultFields.PROCESS_FINDINGS.name()));
        Mockito.verify(executionMock, times(3)).setVariable(eq(DefaultFields.PROCESS_FINDINGS.name()), anyString());
        Mockito.verifyNoMoreInteractions(executionMock);
        assertEquals(0, processExecution.getFindings().size());
    }

    @Test
    public void testAppendAndClearTargets() throws Exception {
        //
        // Append Targets!
        //
        underTest.appendTarget(TestHelper.createBaiscTarget());
        underTest.appendTarget(TestHelper.createTarget("http://w1.w2.www", "some wired"));

        Mockito.verify(executionMock, times(2)).setVariable(eq(DefaultFields.PROCESS_TARGETS.name()), anyString());

        ScanProcessExecution processExecution = processExecutionFactory.get(executionMock);

        assertEquals(2, processExecution.getTargets().size());

        // First Finding
        assertEquals("Brot!", processExecution.getTargets().get(0).getName());
        assertEquals("udp://brot.securecodebox.io:1234", processExecution.getTargets().get(0).getLocation());
        assertEquals(3, processExecution.getTargets().get(0).getAttributes().size());

        assertEquals("Kuchen", processExecution.getTargets().get(0).getAttributes().get("TEST"));
        assertEquals("Beer", processExecution.getTargets().get(0).getAttributes().get("HORRIBLE"));
        assertNotNull(processExecution.getTargets().get(0).getAttributes().get("ObjeCT"));

        // Secound Finding
        assertEquals("some wired", processExecution.getTargets().get(1).getName());
        assertEquals("http://w1.w2.www", processExecution.getTargets().get(1).getLocation());
        assertEquals(3, processExecution.getTargets().get(1).getAttributes().size());

        //
        // Clear targets
        //
        underTest.clearTargets();
        Mockito.verify(executionMock, atLeastOnce()).getVariable(eq(DefaultFields.PROCESS_TARGETS.name()));
        Mockito.verify(executionMock, times(3)).setVariable(eq(DefaultFields.PROCESS_TARGETS.name()), anyString());
        Mockito.verifyNoMoreInteractions(executionMock);
        assertEquals(0, processExecution.getTargets().size());

    }

}

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

package io.securecodebox.scanprocesses.amassnmap;

import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import io.securecodebox.model.findings.Finding;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoveUnresolvableHostFindingsTest {

    @InjectMocks
    RemoveUnresolvableHostFindings classUnderTest;

    @Mock
    ScanProcessExecutionFactory processExecutionFactory;

    @Mock
    DelegateExecution execution;

    @Mock
    ScanProcessExecution scanProcess;


    @Test
    public void shouldRemoveUnresolvableHostFinding() throws Exception {
        when(processExecutionFactory.get(any())).thenReturn(scanProcess);
        List<Finding> findings = createDummyFindings();
        when(scanProcess.getFindings()).thenReturn(findings);

        classUnderTest.notify(execution);

        verify(scanProcess, times(1)).clearFindings();
        verify(scanProcess, times(2)).appendFinding(any());
    }

    private List<Finding> createDummyFindings() {
        Finding f1 = new Finding();
        f1.setCategory("Open Port");
        Finding f2 = new Finding();
        f2.setCategory("Host Unresolvable");
        Finding f3 = new Finding();
        f3.setCategory("Open Port");

        List<Finding> findings = new ArrayList<>();
        findings.add(f1);
        findings.add(f2);
        findings.add(f3);

        return findings;
    }

}

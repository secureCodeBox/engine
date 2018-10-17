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
        List<Finding> findings = creageDummyFindings();
        when(scanProcess.getFindings()).thenReturn(findings);

        classUnderTest.notify(execution);

        verify(scanProcess, times(1)).clearFindings();
        verify(scanProcess, times(2)).appendFinding(any());
    }

    private List<Finding> creageDummyFindings() {
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

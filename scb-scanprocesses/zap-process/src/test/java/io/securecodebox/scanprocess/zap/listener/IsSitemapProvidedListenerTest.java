package io.securecodebox.scanprocess.zap.listener;

import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import io.securecodebox.model.execution.Target;
import io.securecodebox.scanprocess.zap.constants.ZapProcessVariables;
import io.securecodebox.scanprocess.zap.constants.ZapTargetAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;


import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IsSitemapProvidedListenerTest {

    @InjectMocks
    @Spy
    IsSitemapProvidedListener underTest;

    @Mock
    ScanProcessExecutionFactory processExecutionFactory;

    @Mock
    DelegateExecution execution;

    @Mock
    ScanProcessExecution scanProcessExecution;

    @Test
    public void shouldSetSkipSpiderFlagWhenSitemapIsProvided() throws Exception {
        // given
        Target t1 = new Target();
        Target t2 = new Target();
        List<Target> targets = Arrays.asList(t1, t2);
        when(processExecutionFactory.get(execution)).thenReturn(scanProcessExecution);
        when(scanProcessExecution.getTargets()).thenReturn(targets);
        when(underTest.hasSitemap(t1)).thenReturn(true);
        when(underTest.hasSitemap(t2)).thenReturn(true);

        // when
        underTest.notify(execution);

        //then
        verify(execution, times(1)).setVariable(ZapProcessVariables.ZAP_SKIP_SPIDER.name(),true);
    }

    @Test
    public void shouldNotSkipSpiderFlagWhenSitemapIsNotProvidedInAllTargets() throws Exception {
        // given
        Target t1 = new Target();
        Target t2 = new Target();
        List<Target> targets = Arrays.asList(t1, t2);
        when(processExecutionFactory.get(execution)).thenReturn(scanProcessExecution);
        when(scanProcessExecution.getTargets()).thenReturn(targets);
        when(underTest.hasSitemap(t1)).thenReturn(true);
        when(underTest.hasSitemap(t2)).thenReturn(false);

        // when
        underTest.notify(execution);

        //then
        verify(execution, times(0)).setVariable(ZapProcessVariables.ZAP_SKIP_SPIDER.name(),true);
        verify(execution, times(1)).setVariable(ZapProcessVariables.ZAP_SKIP_SPIDER.name(),false);
    }

    @Test
    public void shouldFindSitemapWhenTargetContainSitemap() {
        Target target = new Target();
        target.getAttributes().put(ZapTargetAttributes.ZAP_SITEMAP.name(), Collections.emptyList());

        assertTrue(underTest.hasSitemap(target));
    }

    @Test
    public void shouldSayNoSitemapWhenTargetContainNoSitemap() {
        Target target = new Target();

        assertFalse(underTest.hasSitemap(target));
    }
}

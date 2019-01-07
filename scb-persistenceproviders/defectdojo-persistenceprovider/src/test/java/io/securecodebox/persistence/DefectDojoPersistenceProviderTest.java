package io.securecodebox.persistence;

import io.securecodebox.model.rest.Report;
import io.securecodebox.model.securitytest.SecurityTest;
import io.securecodebox.persistence.models.DefectDojoResponse;
import io.securecodebox.persistence.models.ToolType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefectDojoPersistenceProviderTest {

    @Mock
    DefectDojoService defectDojoService;

    @InjectMocks
    DefectDojoPersistenceProvider persistenceProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createsANewToolTypeIfItCouldNotBeFound(){
        DefectDojoResponse<ToolType> responseExisting = new DefectDojoResponse<>();
        responseExisting.setCount(1);
        when(defectDojoService.getToolTypeByName(any())).thenReturn(responseExisting);

        DefectDojoResponse<ToolType> responseEmpty = new DefectDojoResponse<>();
        responseEmpty.setCount(0);
        when(defectDojoService.getToolTypeByName("GitServer")).thenReturn(responseEmpty);

        SecurityTest securityTest = new SecurityTest();
        securityTest.setReport(new Report());

        persistenceProvider.persist(securityTest);

        verify(defectDojoService, times(1)).createToolType(eq("GitServer"), any());
        verify(defectDojoService, times(0)).createToolType(eq("BuildServer"), any());
        verify(defectDojoService, times(0)).createToolType(eq("SecurityTestOrchestrationEngine"), any());
    }

    @Test
    public void doesntCreateAnyToolTypesIfAllAreAlreadyExisting(){
        DefectDojoResponse<ToolType> responseExisting = new DefectDojoResponse<>();
        responseExisting.setCount(1);
        when(defectDojoService.getToolTypeByName(any())).thenReturn(responseExisting);

        SecurityTest securityTest = new SecurityTest();
        securityTest.setReport(new Report());

        persistenceProvider.persist(securityTest);

        verify(defectDojoService, times(0)).createToolType(any(), any());
    }

}
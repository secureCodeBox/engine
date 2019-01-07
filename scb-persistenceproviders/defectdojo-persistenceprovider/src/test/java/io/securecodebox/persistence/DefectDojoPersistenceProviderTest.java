package io.securecodebox.persistence;

import io.securecodebox.model.rest.Report;
import io.securecodebox.model.securitytest.CommonMetaFields;
import io.securecodebox.model.securitytest.SecurityTest;
import io.securecodebox.persistence.models.DefectDojoResponse;
import io.securecodebox.persistence.models.EngagementPayload;
import io.securecodebox.persistence.models.EngagementResponse;
import io.securecodebox.persistence.models.ToolType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefectDojoPersistenceProviderTest {

    @Mock
    DefectDojoService defectDojoService;

    @Mock
    DescriptionGenerator descriptionGenerator;

    @InjectMocks
    DefectDojoPersistenceProvider persistenceProvider;

    Map<String, String> metaData;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(descriptionGenerator.generate(any())).thenReturn("Foobar Description");

        persistenceProvider.defectDojoUrl = "http://localhost:8000";
        defectDojoService.defectDojoUrl = "http://localhost:8000";

        DefectDojoResponse<ToolType> responseExisting = new DefectDojoResponse<>();
        responseExisting.setCount(1);
        when(defectDojoService.getToolTypeByName(any())).thenReturn(responseExisting);

        EngagementResponse response = new EngagementResponse();
        response.setUrl("http://localhost:8000/api/v2/engagements/2/");
        when(defectDojoService.createEngagement(any())).thenReturn(response);

        metaData = new HashMap<>();
        metaData.put(DefectDojoMetaFields.DEFECT_DOJO_PRODUCT.name(), "1");
        metaData.put(DefectDojoMetaFields.DEFECT_DOJO_USER.name(), "John Doe");
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
        securityTest.setMetaData(metaData);

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
        securityTest.setMetaData(metaData);

        persistenceProvider.persist(securityTest);

        verify(defectDojoService, times(0)).createToolType(any(), any());
    }

    @Test
    public void createsTheEngagement(){
        when(defectDojoService.getUserUrl(eq("John Doe"))).thenReturn("http://localhost:8000/api/v2/users/5/");

        when(defectDojoService.getToolConfiguration(eq("http://crazy.buildserver"), eq("BuildServer"))).thenReturn("http://localhost:8000/api/v2/tool_types/5/");
        when(defectDojoService.getToolConfiguration(eq("http://crazy.scm_server"), eq("GitServer"))).thenReturn("http://localhost:8000/api/v2/tool_types/7/");
        when(defectDojoService.getToolConfiguration(eq("https://github.com/secureCodeBox"), eq("SecurityTestOrchestrationEngine"))).thenReturn("http://localhost:8000/api/v2/tool_types/9/");

        UUID securityTestUuid = UUID.randomUUID();

        SecurityTest securityTest = new SecurityTest();
        securityTest.setId(securityTestUuid);
        securityTest.setContext("Nmap Scan 11");
        Report report = new Report();

        metaData.put(CommonMetaFields.SCB_BRANCH.name(), "master");
        metaData.put(CommonMetaFields.SCB_REPO.name(), "https://github.com/secureCodeBox/engine");
        metaData.put(CommonMetaFields.SCB_BUILD_SERVER.name(), "http://crazy.buildserver");
        metaData.put(CommonMetaFields.SCB_SCM_SERVER.name(), "http://crazy.scm_server");
        securityTest.setMetaData(metaData);

        report.setFindings(Collections.emptyList());
        report.setRawFindings("[\"<custom xml stuff>\"]");

        securityTest.setReport(report);

        EngagementPayload payload = new EngagementPayload();
        payload.setStatus(EngagementPayload.Status.COMPLETED);
        payload.setName("Nmap Scan 11");
        payload.setProduct("http://localhost:8000/api/v2/products/1/");
        payload.setLead("http://localhost:8000/api/v2/users/5/");
        payload.setBranch("master");
        payload.setRepo("https://github.com/secureCodeBox/engine");
        payload.setDescription("Foobar Description");
        payload.setTargetStart("2019-01-07");
        payload.setTargetEnd("2019-01-07");
        payload.setBuildServer("http://localhost:8000/api/v2/tool_types/5/");
        payload.setScmServer("http://localhost:8000/api/v2/tool_types/7/");
        payload.setOrchestrationEngine("http://localhost:8000/api/v2/tool_types/9/");

        persistenceProvider.persist(securityTest);

        verify(defectDojoService, times(1)).createEngagement(
            eq(payload)
        );
    }

    @Test(expected = DefectDojoUserNotFound.class)
    public void failsIfUserCouldNotBeFound(){
        when(defectDojoService.getUserUrl(any())).thenThrow(new DefectDojoUserNotFound(""));

        when(defectDojoService.getToolConfiguration(eq("http://crazy.buildserver"), eq("BuildServer"))).thenReturn("http://localhost:8000/api/v2/tool_types/5/");
        when(defectDojoService.getToolConfiguration(eq("http://crazy.scm_server"), eq("GitServer"))).thenReturn("http://localhost:8000/api/v2/tool_types/7/");
        when(defectDojoService.getToolConfiguration(eq("https://github.com/secureCodeBox"), eq("SecurityTestOrchestrationEngine"))).thenReturn("http://localhost:8000/api/v2/tool_types/9/");

        UUID securityTestUuid = UUID.randomUUID();

        SecurityTest securityTest = new SecurityTest();
        securityTest.setId(securityTestUuid);
        securityTest.setContext("Nmap Scan 11");

        metaData.put(DefectDojoMetaFields.DEFECT_DOJO_USER.name(), "This User really does not exist");
        securityTest.setMetaData(metaData);
        Report report = new Report();
        securityTest.setReport(report);

        persistenceProvider.persist(securityTest);
    }

    @Test(expected = DefectDojoProductNotProvided.class)
    public void failsIfProductCouldNotBeFound(){
        when(defectDojoService.getToolConfiguration(eq("http://crazy.buildserver"), eq("BuildServer"))).thenReturn("http://localhost:8000/api/v2/tool_types/5/");
        when(defectDojoService.getToolConfiguration(eq("http://crazy.scm_server"), eq("GitServer"))).thenReturn("http://localhost:8000/api/v2/tool_types/7/");
        when(defectDojoService.getToolConfiguration(eq("https://github.com/secureCodeBox"), eq("SecurityTestOrchestrationEngine"))).thenReturn("http://localhost:8000/api/v2/tool_types/9/");

        UUID securityTestUuid = UUID.randomUUID();

        SecurityTest securityTest = new SecurityTest();
        securityTest.setId(securityTestUuid);
        securityTest.setContext("Nmap Scan 11");

        metaData.remove(DefectDojoMetaFields.DEFECT_DOJO_PRODUCT.name());
        securityTest.setMetaData(metaData);
        Report report = new Report();
        securityTest.setReport(report);

        persistenceProvider.persist(securityTest);
    }
}
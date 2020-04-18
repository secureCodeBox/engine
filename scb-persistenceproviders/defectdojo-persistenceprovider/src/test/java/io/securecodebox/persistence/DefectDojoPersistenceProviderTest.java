package io.securecodebox.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.rest.Report;
import io.securecodebox.model.securitytest.CommonMetaFields;
import io.securecodebox.model.securitytest.SecurityTest;
import io.securecodebox.persistence.exceptions.DefectDojoProductNotFound;
import io.securecodebox.persistence.exceptions.DefectDojoUserNotFound;
import io.securecodebox.persistence.models.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

import static io.securecodebox.model.findings.Severity.INFORMATIONAL;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefectDojoPersistenceProviderTest {

    @Mock
    DefectDojoService defectDojoService;

    @Mock
    DescriptionGenerator descriptionGenerator;

    @InjectMocks
    DefectDojoPersistenceProvider persistenceProvider = spy(new DefectDojoPersistenceProvider());

    Map<String, String> metaData;

    Report report;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        persistenceProvider.setClock(Clock.fixed(Instant.ofEpochSecond(1546876203), ZoneId.systemDefault()));
        when(descriptionGenerator.generate(any())).thenReturn("Foobar Description");
        doNothing().when(persistenceProvider).checkConnection();

        defectDojoService.defectDojoUrl = "http://localhost:8000";

        DefectDojoResponse<ToolType> responseExisting = new DefectDojoResponse<>();
        responseExisting.setCount(1);
        when(defectDojoService.getToolTypeByName(any())).thenReturn(responseExisting);

        EngagementResponse engagementResponse = new EngagementResponse();
        engagementResponse.setId(2);
        when(defectDojoService.createEngagement(any())).thenReturn(engagementResponse);
        when(defectDojoService.retrieveProductId("Nmap Scan 11")).thenReturn(1l);
        when(defectDojoService.retrieveProductId("Nonexisting")).thenThrow(DefectDojoProductNotFound.class);


        metaData = new HashMap<>();
        metaData.put(DefectDojoMetaFields.DEFECT_DOJO_USER.name(), "John Doe");
        when(defectDojoService.retrieveUserId(eq("John Doe"))).thenReturn(5l);

        report = new Report();
        report.setRawFindings("\"[]\"");
        report.setFindings(Collections.emptyList());

        when(defectDojoService.retrieveOrCreateToolConfiguration(eq("http://crazy.buildserver"), eq("Build Server"))).thenReturn(5l);
        when(defectDojoService.retrieveOrCreateToolConfiguration(eq("http://crazy.scm_server"), eq("Git Server"))).thenReturn(7l);
        when(defectDojoService.retrieveOrCreateToolConfiguration(eq("https://github.com/secureCodeBox"), eq("Security Test Orchestration Engine"))).thenReturn(9l);

    }

    @Test
    public void createsANewToolTypeIfItCouldNotBeFound(){
        DefectDojoResponse<ToolType> responseExisting = new DefectDojoResponse<>();
        responseExisting.setCount(1);
        when(defectDojoService.getToolTypeByName(any())).thenReturn(responseExisting);

        DefectDojoResponse<ToolType> responseEmpty = new DefectDojoResponse<>();
        responseEmpty.setCount(0);
        when(defectDojoService.getToolTypeByName("Git Server")).thenReturn(responseEmpty);

        SecurityTest securityTest = new SecurityTest();
        securityTest.setReport(report);
        securityTest.setMetaData(metaData);
        securityTest.setName("nmap");

        persistenceProvider.persist(securityTest);

        verify(defectDojoService, times(1)).createToolType(eq("Git Server"), any());
        verify(defectDojoService, times(0)).createToolType(eq("Build Server"), any());
        verify(defectDojoService, times(0)).createToolType(eq("Security Test Orchestration Engine"), any());
    }

    @Test
    public void doesntCreateAnyToolTypesIfAllAreAlreadyExisting(){
        DefectDojoResponse<ToolType> responseExisting = new DefectDojoResponse<>();
        responseExisting.setCount(1);
        when(defectDojoService.getToolTypeByName(any())).thenReturn(responseExisting);

        SecurityTest securityTest = new SecurityTest();
        securityTest.setReport(report);
        securityTest.setMetaData(metaData);
        securityTest.setName("nmap");
        securityTest.setContext("Nmap Scan 11");

        persistenceProvider.persist(securityTest);

        verify(defectDojoService, times(0)).createToolType(any(), any());
    }

    @Test
    public void createsTheEngagement(){
        SecurityTest securityTest = new SecurityTest();
        securityTest.setContext("Nmap Scan 11");

        metaData.put(CommonMetaFields.SCB_BRANCH.name(), "master");
        metaData.put(CommonMetaFields.SCB_REPO.name(), "https://github.com/secureCodeBox/engine");
        metaData.put(CommonMetaFields.SCB_BUILD_SERVER.name(), "http://crazy.buildserver");
        metaData.put(CommonMetaFields.SCB_SCM_SERVER.name(), "http://crazy.scm_server");
        securityTest.setMetaData(metaData);
        securityTest.setReport(report);
        securityTest.setName("nmap");

        EngagementPayload payload = new EngagementPayload();
        payload.setStatus(EngagementPayload.Status.COMPLETED);
        payload.setName("Nmap Scan");
        payload.setProduct(1l);
        payload.setLead(5l);
        payload.setBranch("master");
        payload.setRepo("https://github.com/secureCodeBox/engine");
        payload.setDescription("Foobar Description");
        payload.setTargetStart("2019-01-07");
        payload.setTargetEnd("2019-01-07");
        payload.setBuildServer(5l);
        payload.setScmServer(7l);
        payload.setOrchestrationEngine(9l);
        payload.getTags().add("secureCodeBox");
        payload.getTags().add("automated");

        persistenceProvider.persist(securityTest);

        verify(defectDojoService, times(1)).createEngagement(
            eq(payload)
        );
    }

    @Test(expected = DefectDojoUserNotFound.class)
    public void failsIfUserCouldNotBeFound(){
        when(defectDojoService.retrieveUserId(any())).thenThrow(new DefectDojoUserNotFound(""));

        SecurityTest securityTest = new SecurityTest();
        securityTest.setContext("Nmap Scan 11");

        metaData.put(DefectDojoMetaFields.DEFECT_DOJO_USER.name(), "This User really does not exist");
        securityTest.setMetaData(metaData);
        securityTest.setReport(report);
        securityTest.setName("nmap");

        persistenceProvider.persist(securityTest);
    }

    @Test(expected = DefectDojoProductNotFound.class)
    public void failsIfProductCouldNotBeFound(){
        SecurityTest securityTest = new SecurityTest();
        securityTest.setContext("Nonexisting");

        securityTest.setMetaData(metaData);
        securityTest.setReport(report);
        securityTest.setName("nmap");

        persistenceProvider.persist(securityTest);
    }

    @Test
    public void createsFindingsForSupportedScanner() throws JsonProcessingException {
        SecurityTest securityTest = new SecurityTest();
        securityTest.setContext("Nmap Scan 11");
        securityTest.setName("nmap");

        ObjectMapper objectMapper = new ObjectMapper();

        List<String> list = new ArrayList();
        list.add("<Some Xml stuff>\n<Don't know how the form is>");
        String doubleSer = objectMapper.writeValueAsString(objectMapper.writeValueAsString(list));

        report.setRawFindings(doubleSer);
        securityTest.setMetaData(metaData);
        securityTest.setReport(report);
        securityTest.setName("nmap");

        persistenceProvider.persist(securityTest);
        verify(defectDojoService, times(1)).createFindings(
                eq("<Some Xml stuff>\n<Don't know how the form is>"),
                eq(2l),
                eq(5l),
                eq("2019-01-07"),
                eq("Nmap Scan")
        );
    }

    @Test
    public void createsFindingsForNonSupportedScanner() {
        SecurityTest securityTest = new SecurityTest();
        securityTest.setContext("Non supported Scan 11");
        securityTest.setName("any non supported scanner");

        List<Finding> findings = new ArrayList<>();
        Finding finding = new Finding();
        finding.setName("findingname");
        finding.setDescription("description");
        finding.setFalsePositive(false);
        finding.setLocation("http://someadress");
        finding.setSeverity(INFORMATIONAL);
        findings.add(finding);

        report.setFindings(findings);
        securityTest.setMetaData(metaData);
        securityTest.setReport(report);

        persistenceProvider.persist(securityTest);
        verify(defectDojoService, times(1)).createFindings(
                eq( "date,title,cweid,url,severity,description,mitigation,impact,references,active,verified,falsepositive,duplicate\n"+
                        "2019-01-07,findingname,,http://someadress,INFORMATIONAL,description,,,,,,false,false"),
                eq(2l),
                eq(5l),
                eq("2019-01-07"),
                eq("Generic Findings Import")
        );
    }

    @Test
    public void createProduct() {
        String productName = "mytestproduct";
        defectDojoService.createProduct(productName);
        verify(defectDojoService, times(1)).createProduct(
            eq(productName)
        );
    }
}

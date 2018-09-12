package io.securecodebox.scanprocess.test.zap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.Target;
import io.securecodebox.scanprocess.zap.constants.ZapProcessVariables;
import io.securecodebox.scanprocess.zap.listener.IsSitemapProvidedListener;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.bpm.scenario.delegate.ExternalTaskDelegate;
import org.camunda.bpm.scenario.delegate.TaskDelegate;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.processEngine;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.extension.mockito.CamundaMockito.autoMock;
import static org.camunda.bpm.extension.mockito.CamundaMockito.verifyExecutionListenerMock;
import static org.camunda.bpm.extension.mockito.CamundaMockito.verifyJavaDelegateMock;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@Deployment(resources = "bpmn/zap_process.bpmn")
public class ZapProcessTest {

    //Define the Process Activity IDs
    private static final String PROCESS_ID = "zap-process";

    private static final String CONFIGURE_AUTHENTICATION_TASK = "UserTask_ConfigureAuthentication";
    private static final String CONFIGURE_SPIDER_DETAILS_TASK = "UserTask_ConfigureSpiderDetails";
    private static final String CONFIGURE_SCANNER_DETAILS_TASK = "UserTask_ConfigureScannerDetails";
    private static final String APPROVE_RESULTS_TASK = "UserTask_ApproveResults";

    private static final String RUN_SPIDER_TASK = "ServiceTask_StartSpider";
    private static final String RUN_SCANNER_TASK = "ServiceTask_StartScanner";
    private static final String CREATE_SUMMARY_TASK = "ServiceTask_CreateSummary";

    private final Map<String, Object> defaultVariables = new HashMap<>();

    @Rule
    @ClassRule
    public static ProcessEngineRule processEngineRule = TestCoverageProcessEngineRuleBuilder.create().build();

    @Mock
    private ProcessScenario zapProcess;

    @Mock
    private IsSitemapProvidedListener isSitemapProvidedListener;

    /**
     * Executed before every test-case
     * In this method default variables for the process and a default behaviour for the mocks
     * in the process are defined
     */
    @Before
    public void init() {

        defaultVariables.put(DefaultFields.PROCESS_AUTOMATED.name(), false);
        defaultVariables.put(DefaultFields.PROCESS_CONTEXT.name(), "BodgeIT");

        defaultVariables.put("ZAP_AUTHENTICATION", false);
        defaultVariables.put("ZAP_SPIDER_CONFIGURATION_TYPE", "default");
        defaultVariables.put("ZAP_SCANNER_CONFIGURATION_TYPE", "default");

        List<Target> scanTargets = new LinkedList<>();
        Target bodgeit = new Target();
        bodgeit.setName("Bodgeit");
        bodgeit.setLocation("http://bodgeit:8080/bodgeit");
        bodgeit.appendOrUpdateAttribute("ZAP_BASE_URL", "http://bodgeit:8080/bodgeit");
        bodgeit.appendOrUpdateAttribute("ZAP_SPIDER_CONFIGURATION_TYPE", "default");
        bodgeit.appendOrUpdateAttribute("ZAP_SCANNER_CONFIGURATION_TYPE", "default");

        scanTargets.add(bodgeit);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            defaultVariables.put(DefaultFields.PROCESS_TARGETS.name(), objectMapper.writeValueAsString(scanTargets));
        }
        catch (JsonProcessingException e){
            //not possible
        }

        autoMock("bpmn/zap_process.bpmn");

        when(zapProcess.waitsAtUserTask(Mockito.anyString())).thenReturn(TaskDelegate::complete);
        when(zapProcess.waitsAtServiceTask(Mockito.anyString())).thenReturn(
                ExternalTaskDelegate::complete);
        when(zapProcess.waitsAtServiceTask(RUN_SPIDER_TASK)).thenReturn(
                task -> startExternalMockProcess("zap_spider"));
        when(zapProcess.waitsAtServiceTask(RUN_SCANNER_TASK)).thenReturn(
                task -> startExternalMockProcess("zap_scan"));

        Mocks.register("isSitemapProvidedListener", (ExecutionListener) delegateExecution ->
                delegateExecution.setVariable(ZapProcessVariables.ZAP_SKIP_SPIDER.name(), false)
        );
    }

    @Test
    public void testDeployment() {
    }

    @Test
    public void testAutomatedStart_shouldPass() {

        Map<String, Object> variables = new HashMap<>(defaultVariables);
        changeVariable(variables, "PROCESS_AUTOMATED", true);
        ProcessInstance processInstance = startProcessInstance(variables);

        assertThat(processInstance).isStarted();
    }

    @Test(expected = ProcessEngineException.class)
    public void testStartWithoutVariables_shouldFail() {
        runtimeService().startProcessInstanceByKey(PROCESS_ID);
    }

    @Test(expected = ProcessEngineException.class)
    public void testManualStartWithoutConfiguration_shouldFail() {
        Map<String, Object> variables = new HashMap<>();
        variables.put(DefaultFields.PROCESS_AUTOMATED.name(), false);
        startProcessInstance(variables);
    }

    @Test
    public void testManualStartWithDefaultConfiguration_shouldPass() {
        ProcessInstance processInstance = startProcessInstance(defaultVariables);

        assertThat(processInstance).isStarted();
        assertThat(processInstance).isWaitingAt(RUN_SPIDER_TASK);
    }

    @Test
    public void testConfigureAuthenticationLetsUserConfigure(){
        Map<String, Object> variables = new HashMap<>(defaultVariables);
        changeVariable(variables, "ZAP_AUTHENTICATION", true);

        ProcessInstance processInstance = startProcessInstance(variables);
        assertThat(processInstance).isWaitingAt(CONFIGURE_AUTHENTICATION_TASK);
    }

    @Test
    public void testConfigureSpiderLetsUserConfigure(){
        Map<String, Object> variables = new HashMap<>(defaultVariables);
        changeVariable(variables, "ZAP_SPIDER_CONFIGURATION_TYPE", "advanced");

        ProcessInstance processInstance = startProcessInstance(variables);
        assertThat(processInstance).isWaitingAt(CONFIGURE_SPIDER_DETAILS_TASK);
    }

    @Test
    public void testConfigureScannerLetsUserConfigure(){
        Map<String, Object> variables = new HashMap<>(defaultVariables);
        changeVariable(variables, "ZAP_SCANNER_CONFIGURATION_TYPE", "advanced");

        ProcessInstance processInstance = startProcessInstance(variables);
        assertThat(processInstance).isWaitingAt(CONFIGURE_SCANNER_DETAILS_TASK);
    }

    @Test
    public void testCorrectAdvancedConfiguration(){
        Map<String, Object> variables = new HashMap<>(defaultVariables);
        changeVariable(variables, "ZAP_SCANNER_CONFIGURATION_TYPE", "advanced");
        changeVariable(variables, "ZAP_SPIDER_CONFIGURATION_TYPE", "advanced");
        changeVariable(variables, "ZAP_AUTHENTICATION", true);

        when(zapProcess.waitsAtUserTask(CONFIGURE_AUTHENTICATION_TASK)).thenReturn(task -> {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Target> targets = objectMapper.readValue((String)variables.get("PROCESS_TARGETS"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Target.class));

            Target scanTarget = targets.get(0);
            scanTarget.appendOrUpdateAttribute("ZAP_LOGIN_SITE", "http://example.at");
            scanTarget.appendOrUpdateAttribute("ZAP_LOGIN_USER", "AmazingDestroyer6XX");
            scanTarget.appendOrUpdateAttribute("ZAP_LOGIN_PW", "Smashing everything");
            scanTarget.appendOrUpdateAttribute("ZAP_USERNAME_FIELD_ID", "username");
            scanTarget.appendOrUpdateAttribute("ZAP_PW_FIELD_ID", "pw");

            targets.clear();
            targets.add(scanTarget);

            changeVariable(variables, "PROCESS_TARGETS", objectMapper.writeValueAsString(targets));
            task.complete(variables);
        });
        when(zapProcess.waitsAtUserTask(CONFIGURE_SPIDER_DETAILS_TASK)).thenReturn(task -> {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Target> targets = objectMapper.readValue((String)variables.get("PROCESS_TARGETS"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Target.class));

            Target scanTarget = targets.get(0);
            scanTarget.appendOrUpdateAttribute("ZAP_SPIDER_MAX_DEPTH", 5);

            targets.clear();
            targets.add(scanTarget);

            changeVariable(variables, "PROCESS_TARGETS", objectMapper.writeValueAsString(targets));
            task.complete(variables);
        });
        when(zapProcess.waitsAtServiceTask(RUN_SPIDER_TASK)).thenReturn(task -> {});

        Scenario scenario = Scenario.run(zapProcess).startByKey(PROCESS_ID, variables).execute();

        assertThat(scenario.instance(zapProcess)).hasPassed(CONFIGURE_AUTHENTICATION_TASK);
        assertThat(scenario.instance(zapProcess)).hasPassed(CONFIGURE_SPIDER_DETAILS_TASK);
        assertThat(scenario.instance(zapProcess)).hasPassed(CONFIGURE_SCANNER_DETAILS_TASK);
        assertThat(scenario.instance(zapProcess)).hasVariables("PROCESS_TARGETS");

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<Target> targets = objectMapper.readValue((String) variables.get("PROCESS_TARGETS"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Target.class));
            Target target = targets.get(0);
            assertTrue(target.getAttributes().get("ZAP_LOGIN_SITE").equals("http://example.at"));
            assertTrue(target.getAttributes().get("ZAP_LOGIN_USER").equals("AmazingDestroyer6XX"));
            assertTrue(target.getAttributes().get("ZAP_LOGIN_PW").equals("Smashing everything"));
            assertTrue(target.getAttributes().get("ZAP_USERNAME_FIELD_ID").equals("username"));
            assertTrue(target.getAttributes().get("ZAP_PW_FIELD_ID").equals("pw"));
        }
        catch (IOException e){
            fail(e.getMessage());
        }

        assertThat(scenario.instance(zapProcess)).isWaitingAt(RUN_SPIDER_TASK);
    }

    @Test
    public void shouldSkipSpiderTaskIfSitemapProvided(){
        // given
        Mocks.register("isSitemapProvidedListener", (ExecutionListener) delegateExecution ->
            delegateExecution.setVariable(ZapProcessVariables.ZAP_SKIP_SPIDER.name(), true)
        );

        // when
        ProcessInstance processInstance = startProcessInstance(defaultVariables);

        // then
        assertThat(processInstance).isWaitingAt(RUN_SCANNER_TASK);
    }

    @Test
    public void testFindingToTargetsTransformationCalled(){

        when(zapProcess.waitsAtServiceTask(RUN_SCANNER_TASK)).thenReturn(task -> {});

        Scenario scenario = Scenario.run(zapProcess).startByKey(PROCESS_ID, defaultVariables).execute();

        assertThat(scenario.instance(zapProcess)).isWaitingAt(RUN_SCANNER_TASK);
        verifyExecutionListenerMock("transformZapFindingsToZapTargetsListener");
    }

    @Test
    public void testCreateSummaryCalled(){
        Map<String, Object> variables = new HashMap<>(defaultVariables);
        changeVariable(variables, "PROCESS_AUTOMATED", true);

        Scenario scenario = Scenario.run(zapProcess).startByKey(PROCESS_ID, variables).execute();
        assertThat(scenario.instance(zapProcess)).isEnded();
        assertThat(scenario.instance(zapProcess)).hasPassed(CREATE_SUMMARY_TASK);
        verifyJavaDelegateMock("summaryGeneratorDelegate");
    }

    @Test
    public void testManualRunWithApprovedTestResults() {

        Map<String, Object> variables = new HashMap<>(defaultVariables);

        when(zapProcess.waitsAtUserTask(APPROVE_RESULTS_TASK)).thenReturn(task -> {
            variables.put(DefaultFields.PROCESS_RESULT_APPROVED.name(), "approved");
            task.complete(variables);
        });
        
        Scenario scenario = Scenario.run(zapProcess).startByKey(PROCESS_ID, variables).execute();

        assertThat(scenario.instance(zapProcess)).isEnded();
        assertThat(scenario.instance(zapProcess)).hasPassed(APPROVE_RESULTS_TASK);
        assertThat(scenario.instance(zapProcess)).variables().containsEntry(DefaultFields.PROCESS_RESULT_APPROVED.name(), "approved");
    }

    @Test
    public void testManualRunWithRejectedTestResultsShouldGoBackToConfiguration() {

        Map<String, Object> variables = new HashMap<>(defaultVariables);

        when(zapProcess.waitsAtUserTask(APPROVE_RESULTS_TASK)).thenReturn(task -> {
            variables.put(DefaultFields.PROCESS_RESULT_APPROVED.name(), "disapproved");
            task.complete(variables);
        });
        final AtomicBoolean spiderRun = new AtomicBoolean(true);
        when(zapProcess.waitsAtServiceTask(RUN_SPIDER_TASK)).thenReturn(task ->  {
            if(spiderRun.get()){
                startExternalMockProcess("zap_spider");
                spiderRun.set(false);
            }
        });

        Scenario scenario = Scenario.run(zapProcess).startByKey(PROCESS_ID, variables).execute();

        assertThat(scenario.instance(zapProcess)).hasPassed(APPROVE_RESULTS_TASK);
        assertThat(scenario.instance(zapProcess)).variables().containsEntry(DefaultFields.PROCESS_RESULT_APPROVED.name(), "disapproved");
        assertThat(scenario.instance(zapProcess)).isWaitingAt(RUN_SPIDER_TASK);
    }

    /**
     * Executes an external process without doing anything in the task.
     * In the first step the job is executed on the Camunda engine. Therefore the token for the
     * provided topic gets pushed. Then an external service is called to pull the token and execute the task
     *
     * @param topic the topic for the external task
     */
    private void startExternalMockProcess(String topic) {

        ExternalTaskService externalTaskService = processEngine().getExternalTaskService();
        List<LockedExternalTask> lockedExternalTasks = externalTaskService.fetchAndLock(1, "worker")
                .topic(topic, 5000L).execute();

        assertThat(lockedExternalTasks.size()).isEqualTo(1);

        LockedExternalTask task = lockedExternalTasks.get(0);
        externalTaskService.complete(task.getId(), "worker");
    }

    private void changeVariable(Map<String, Object> variables, String key, Object value) {

        if (variables.containsKey(key)) {
            variables.remove(key);
        }
        variables.put(key, value);
    }

    private ProcessInstance startProcessInstance( Map<String, Object> var){

        return runtimeService().startProcessInstanceByKey(PROCESS_ID, var);
    }
}

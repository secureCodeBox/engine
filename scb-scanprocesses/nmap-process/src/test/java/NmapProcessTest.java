import io.securecodebox.constants.DefaultFields;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.bpm.scenario.delegate.ExternalTaskDelegate;
import org.camunda.bpm.scenario.delegate.TaskDelegate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.processEngine;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.camunda.bpm.extension.mockito.CamundaMockito.*;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@Deployment(resources = "bpmn/nmap_scan.bpmn")
public class NmapProcessTest {

    private static final String PROCESS_ID = "SecureBoxExternalPortscan";
    private static final String DO_PORTSCAN_TASK_ID = "ServiceTask_DoPortscan";
    private static final String TRANSFORM_RESULTS_TASK_ID = "ServiceTask_TransformResult";
    private static final String CREATE_REPORT_TASK_ID = "ServiceTask_CreateSummary";
    private static final String MARK_FALSE_POSITIVES_TASK_ID = "ServiceTask_MarkFalsePositive";
    private static final String APPROVE_RESULTS_TASK_ID = "UserTask_ApproveResults";
    private static final String CONFIGURE_ADVANCED_PORTSCAN_TASK_ID = "UserTask_ConfigureAdvancedPortscan";

    private final Map<String, Object> defaultVariables = new HashMap<>();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private ProcessScenario nmapProcess;

    @Before
    public void init() {

        /*
        Defining default behaviour of tasks
        This might have to be overridden in test cases
         */
        defaultVariables.put(DefaultFields.PROCESS_AUTOMATED.name(), true);
        defaultVariables.put("context", "BodgeIT");
        defaultVariables.put("nmap_target", "localhost");
        defaultVariables.put("targetName", "BodgeIT Public Host");
        defaultVariables.put("markFalsePositive", false);
        defaultVariables.put("nmap_configuration_type", "default");
        defaultVariables.put("scanner_type", "nmap");

        autoMock("bpmn/nmap_scan.bpmn");

        when(nmapProcess.waitsAtUserTask(Mockito.anyString())).thenReturn(
                task -> executeUserTask(task.getId()));
        when(nmapProcess.waitsAtServiceTask(Mockito.anyString())).thenReturn(
                ExternalTaskDelegate::complete);
        when(nmapProcess.waitsAtServiceTask(DO_PORTSCAN_TASK_ID)).thenReturn(
                task -> {
                    startExternalMockProcess("nmap_portscan");
                });
        when(nmapProcess.waitsAtServiceTask(MARK_FALSE_POSITIVES_TASK_ID)).thenReturn(
                task -> startExternalMockProcess("mark_falsepositive"));
    }

    @Test
    public void testDeployment() {
    }

    @Test
    public void testAutomatedStart_shouldPass() {
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_ID, defaultVariables);
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
        runtimeService().startProcessInstanceByKey(PROCESS_ID, variables);
    }

    @Test
    public void testManualStartWithDefaultConfiguration_shouldPass() {
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_ID, defaultVariables);
        assertThat(processInstance).isStarted();
        assertThat(processInstance).isWaitingAt(DO_PORTSCAN_TASK_ID);
    }

    @Test
    public void testAdvancedConfigurationLetsUserConfigureScan() {
        Map<String, Object> variables = new HashMap<>(defaultVariables);
        changeVariable(variables, "nmap_configuration_type", "advanced");
        changeVariable(variables, DefaultFields.PROCESS_AUTOMATED.name(), false);
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_ID, variables);
        assertThat(processInstance).isWaitingAt(CONFIGURE_ADVANCED_PORTSCAN_TASK_ID);
    }

    @Test
    public void testAfterAdvancedConfigurationPortscanShouldBeStarted() {
        Map<String, Object> variables = new HashMap<>(defaultVariables);
        changeVariable(variables, "nmap_configuration_type", "advanced");
        changeVariable(variables, DefaultFields.PROCESS_AUTOMATED.name(), false);

        when(nmapProcess.waitsAtUserTask(Mockito.anyString())).thenReturn(TaskDelegate::complete);
        when(nmapProcess.waitsAtServiceTask(Mockito.anyString())).thenReturn(task -> {
        });

        Scenario scenario = Scenario.run(nmapProcess).startByKey(PROCESS_ID, variables).execute();
        assertThat(scenario.instance(nmapProcess)).isWaitingAt(DO_PORTSCAN_TASK_ID);
        assertThat(scenario.instance(nmapProcess)).hasPassedInOrder(CONFIGURE_ADVANCED_PORTSCAN_TASK_ID);
        assertThat(scenario.instance(nmapProcess)).variables().containsEntry(DefaultFields.PROCESS_AUTOMATED.name(), false)
                .containsEntry("nmap_configuration_type", "advanced");
    }

    @Test
    public void testAutomatedScanWithoutMarkingFalsePositive() {

        Scenario scenario = Scenario.run(nmapProcess).startByKey(PROCESS_ID, defaultVariables).execute();

        assertThat(scenario.instance(nmapProcess)).isEnded();
        assertThat(scenario.instance(nmapProcess)).hasVariables(
                "context", "nmap_target", "targetName", "markFalsePositive", "nmap_configuration_type");
        assertThat(scenario.instance(nmapProcess)).hasPassedInOrder(DO_PORTSCAN_TASK_ID, TRANSFORM_RESULTS_TASK_ID, CREATE_REPORT_TASK_ID);
        verifyExecutionListenerMock("finishPortScanListener").executed();
        verifyJavaDelegateMock("transformNmapResultsDelegate").executed();
        verifyJavaDelegateMock("summaryGeneratorDelegate").executed();
    }

    @Test
    public void testAutomatedScanWithMarkingFalsePositive() {

        Map<String, Object> variables = new HashMap<>(defaultVariables);
        changeVariable(variables, "markFalsePositive", true);
        Scenario scenario = Scenario.run(nmapProcess).startByKey(PROCESS_ID, variables).execute();

        assertThat(scenario.instance(nmapProcess)).isEnded();
        assertThat(scenario.instance(nmapProcess)).hasVariables(
                "context", "nmap_target", "targetName", "markFalsePositive", "nmap_configuration_type");
        assertThat(scenario.instance(nmapProcess)).hasPassedInOrder(DO_PORTSCAN_TASK_ID, TRANSFORM_RESULTS_TASK_ID,
                MARK_FALSE_POSITIVES_TASK_ID, CREATE_REPORT_TASK_ID);
        verifyExecutionListenerMock("finishPortScanListener").executed();
        verifyJavaDelegateMock("transformNmapResultsDelegate").executed();
        verifyJavaDelegateMock("summaryGeneratorDelegate").executed();
    }

    @Test
    public void testManualRunWithApprovedTestResults() {

        Map<String, Object> variables = new HashMap<>(defaultVariables);
        changeVariable(variables, DefaultFields.PROCESS_AUTOMATED.name(), false);

        when(nmapProcess.waitsAtUserTask(APPROVE_RESULTS_TASK_ID)).thenReturn(task -> {
            variables.put("resultApproved", "approved");
            task.complete(variables);
        });

        Mocks.register("setFormUrlListener", new TaskListener() {

            private Expression scanner_type = new Expression() {
                @Override
                public Object getValue(VariableScope variableScope) {
                    return "nmap";
                }

                @Override
                public void setValue(Object o, VariableScope variableScope) {

                }

                @Override
                public String getExpressionText() {
                    return null;
                }

                @Override
                public boolean isLiteralText() {
                    return false;
                }
            };

            @Override
            public void notify(DelegateTask delegateTask) {
            }
        });

        Scenario scenario = Scenario.run(nmapProcess).startByKey(PROCESS_ID, variables).execute();

        assertThat(scenario.instance(nmapProcess)).isEnded();
        assertThat(scenario.instance(nmapProcess)).hasPassed(APPROVE_RESULTS_TASK_ID);
        assertThat(scenario.instance(nmapProcess)).variables().containsEntry("resultApproved", "approved");
    }

    @Test
    public void testManualRunWithRejectedTestResultsShouldGoBackToConfigureAdvancedScan() {

        Map<String, Object> variables = new HashMap<>(defaultVariables);
        changeVariable(variables, DefaultFields.PROCESS_AUTOMATED.name(), false);

        when(nmapProcess.waitsAtUserTask(APPROVE_RESULTS_TASK_ID)).thenReturn(task -> {
            variables.put("resultApproved", "dissapproved");
            task.complete(variables);
        });

        Mocks.register("setFormUrlListener", new TaskListener() {

            private Expression scanner_type = new Expression() {
                @Override
                public Object getValue(VariableScope variableScope) {
                    return "nmap";
                }

                @Override
                public void setValue(Object o, VariableScope variableScope) {

                }

                @Override
                public String getExpressionText() {
                    return null;
                }

                @Override
                public boolean isLiteralText() {
                    return false;
                }
            };

            @Override
            public void notify(DelegateTask delegateTask) {
            }
        });

        Scenario scenario = Scenario.run(nmapProcess).startByKey(PROCESS_ID, variables).execute();

        assertThat(scenario.instance(nmapProcess)).hasPassed(APPROVE_RESULTS_TASK_ID);
        assertThat(scenario.instance(nmapProcess)).variables().containsEntry("resultApproved", "dissapproved");
        assertThat(scenario.instance(nmapProcess)).isWaitingAt(CONFIGURE_ADVANCED_PORTSCAN_TASK_ID);
    }


    /**
     * Executes an external process without doing anything in the task
     * In the first step the job is executed on the camunda engine, therefore the token for the
     * provided topic gets pushed. Then an external Service is called to pull the token and execute the task
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

    /**
     * Define here what should happen in your Usertasks
     * You might want to create a switch statement and then define the actions for each task
     *
     * @param usertaskId the id of the usertask
     */
    private void executeUserTask(String usertaskId) {
        switch (usertaskId) {
            case APPROVE_RESULTS_TASK_ID:
                Map<String, Object> variables = new HashMap<>();
                variables.put("resultApproved", true);
                complete(task(), variables);
                break;
            case CONFIGURE_ADVANCED_PORTSCAN_TASK_ID:
                complete(task());
                break;
        }
    }

    private void changeVariable(Map<String, Object> variables, String key, Object value) {

        if (variables.containsKey(key)) {
            variables.remove(key);
        }
        variables.put(key, value);
    }


    /**
     * This will just stay here for future reference
     */
//    @Test
//    public void testAutomatedScanWithoutMarkingFalsePositve() {
//
//        autoMock("bpmn/nmap_scan.bpmn");
//        ProcessInstance processInstance = processEngine().getRuntimeService().startProcessInstanceByKey(PROCESS_ID, defaultVariables);
//
//
//        assertThat(processInstance).isStarted();
//        //Make sure that the next task to execute is "Run Portscan"
//        assertEquals(Collections.singletonList(DO_PORTSCAN_TASK_ID), runtimeService().getActiveActivityIds(processInstance.getId()));
//
//        startExternalMockProcess("nmap_portscan");
//
//        verifyExecutionListenerMock("finishPortScanListener").executed();
//        assertThat(processInstance).isWaitingAt(TRANSFORM_RESULTS_TASK_ID);
//
//        execute(job());
//
//        verifyJavaDelegateMock("transformNmapResultsDelegate").executed();
//        assertThat(processInstance).isWaitingAt(CREATE_REPORT_TASK_ID);
//
//        execute(job());
//
//        verifyJavaDelegateMock("summaryGeneratorDelegate").executed();
//        assertThat(processInstance).isEnded();
//
//        //todo: expand this list with finding variables
//        assertThat(processInstance).hasVariables("context", "nmap_target", "targetName", "markFalsePositive",
//                "nmap_configuration_type");
//
//        assertThat(processInstance).hasPassedInOrder(DO_PORTSCAN_TASK_ID, TRANSFORM_RESULTS_TASK_ID, CREATE_REPORT_TASK_ID);
//    }

}

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

package io.securecodebox.scanprocess.nmap;

import io.securecodebox.constants.DefaultFields;
import io.securecodebox.constants.NmapFields;
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
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.bpm.scenario.delegate.ExternalTaskDelegate;
import org.camunda.bpm.scenario.delegate.TaskDelegate;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.processEngine;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.camunda.bpm.extension.mockito.CamundaMockito.*;
import static org.mockito.Mockito.when;

/**
 * This class tests the process execution of the Nmap-Process BPMN Model
 * It verifies that each process task is called when it's supposed to be and
 * delegation code is executed at the right time
 *
 * The tests run in an own Camunda engine which is defined by the camunda.cfg.xml in the resources directory
 *
 * The test cases use Camunda BPM's standard framework as well as the
 * Camunda BPM Assert extension (<a href="https://github.com/camunda/camunda-bpm-assert")/>),
 * camunda-bpm-mockito (<a href="https://github.com/camunda/camunda-bpm-mockito"/>)
 * and the Camunda BPM Assert Scenario extension (<a href="https://github.com/camunda/camunda-bpm-assert-scenario"/>)
 *
 * Furthermore this class also uses the Camunda BPM Process Test Coverage extension
 * (<a href="https://github.com/camunda/camunda-bpm-process-test-coverage"/>).
 * After the test is run we can examine the test coverage in the directory target/process-test-coverage
 */

@RunWith(SpringJUnit4ClassRunner.class)
@Deployment(resources = "bpmn/nmap_scan.bpmn")
public class NmapProcessTest {

    //Define the Process Activity IDs
    private static final String PROCESS_ID = "SecureBoxExternalPortscan";
    private static final String DO_PORTSCAN_TASK_ID = "ServiceTask_DoPortscan";
    private static final String TRANSFORM_RESULTS_TASK_ID = "ServiceTask_TransformResult";
    private static final String CREATE_REPORT_TASK_ID = "ServiceTask_CreateSummary";
    private static final String MARK_FALSE_POSITIVES_TASK_ID = "ServiceTask_MarkFalsePositive";
    private static final String APPROVE_RESULTS_TASK_ID = "UserTask_ApproveResults";
    private static final String CONFIGURE_ADVANCED_PORTSCAN_TASK_ID = "UserTask_ConfigureAdvancedPortscan";

    private final Map<String, Object> defaultVariables = new HashMap<>();

    @Rule
    @ClassRule
    public static ProcessEngineRule processEngineRule = TestCoverageProcessEngineRuleBuilder.create().build();

    @Mock
    private ProcessScenario nmapProcess;

    /**
     * Executed before every test-case
     * In this method default variables for the process and a default behaviour for the mocks
     * in the process are defined
     */
    @Before
    public void init() {

        //Creating a map of default variables for the process
        defaultVariables.put(DefaultFields.PROCESS_AUTOMATED.name(), true);
        defaultVariables.put(DefaultFields.PROCESS_CONTEXT.name(), "BodgeIT");
        defaultVariables.put(NmapFields.NMAP_TARGET.name(), "localhost");
        defaultVariables.put(NmapFields.NMAP_TARGET_URL.name(), "BodgeIT Public Host");
        defaultVariables.put("markFalsePositive", false);
        defaultVariables.put(NmapFields.NMAP_CONFIGURATION_TYPE.name(), "default");

        /*
        Mocking everything in the BPMN Model
        This includes ExecutionListeners, TaskListeners, JavaDelegates, etc.
        Simply stated: Everything, that's executable code

        If you need to define custom behaviour for the Mocks you can do so by
        registering Mocks with Camunda's method "Mocks.register(String key, Object value)".
        Here the key describes a delegateExpression (as defined in BPMN model) and the value
        describes the implementation of the code which should be executed
        (Hint: You can put the real implementation as well as a fake one in there)

        Note: Most of the mocking methods seem to work only in combination with delegateExpressions
        but not with class definitions as delegate implementation.

        If you have the path to your executable code (the class for example) as delegate implementation
        then this guide is helpful:
        https://blog.akquinet.de/2016/11/04/camunda-bpm-test-your-processes-based-on-plain-old-java-delegates/
         */
        autoMock("bpmn/nmap_scan.bpmn");

        /*
        Here we define a default behaviour for all the tasks in the BPMN model.
        This behaviour can easily be overridden in test cases.

        The code inside the "thenReturn(...)" method specifies what should happen when process execution
        waits at the given task
        As a default behaviour we just complete the task and move on to the next one without changing anything

        Note that we have our own mock implementation in the last two when(...) statements.
        This is because these tasks are external tasks which cannot be as easily completed as
        ServiceTasks. They need an external worker to do so.
         */
        when(nmapProcess.waitsAtUserTask(Mockito.anyString())).thenReturn(TaskDelegate::complete);
        when(nmapProcess.waitsAtServiceTask(Mockito.anyString())).thenReturn(
                ExternalTaskDelegate::complete);
        when(nmapProcess.waitsAtServiceTask(DO_PORTSCAN_TASK_ID)).thenReturn(
                task -> startExternalMockProcess("nmap_portscan"));
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
        changeVariable(variables, NmapFields.NMAP_CONFIGURATION_TYPE.name(), "advanced");
        changeVariable(variables, DefaultFields.PROCESS_AUTOMATED.name(), false);

        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_ID, variables);

        assertThat(processInstance).isWaitingAt(CONFIGURE_ADVANCED_PORTSCAN_TASK_ID);
    }

    @Test
    public void testAfterAdvancedConfigurationPortscanShouldBeStarted() {
        Map<String, Object> variables = new HashMap<>(defaultVariables);
        changeVariable(variables, NmapFields.NMAP_CONFIGURATION_TYPE.name(), "advanced");
        changeVariable(variables, DefaultFields.PROCESS_AUTOMATED.name(), false);

        /*
        Here some of the default behaviour defined in the "init()" method gets overridden
         */
        when(nmapProcess.waitsAtUserTask(Mockito.anyString())).thenReturn(TaskDelegate::complete);
        when(nmapProcess.waitsAtServiceTask(Mockito.anyString())).thenReturn(task -> {
        });

        /*
        Execute a process scenario:
        The process specified by the id gets started and the model behaves as defined in the
        when(...).thenReturn(...) statements.
         */
        Scenario scenario = Scenario.run(nmapProcess).startByKey(PROCESS_ID, variables).execute();

        //After running the scenario we verify what we want to test
        assertThat(scenario.instance(nmapProcess)).isWaitingAt(DO_PORTSCAN_TASK_ID);
        assertThat(scenario.instance(nmapProcess)).hasPassedInOrder(CONFIGURE_ADVANCED_PORTSCAN_TASK_ID);
        assertThat(scenario.instance(nmapProcess)).variables().containsEntry(DefaultFields.PROCESS_AUTOMATED.name(), false)
                .containsEntry(NmapFields.NMAP_CONFIGURATION_TYPE.name(), "advanced");
    }

    /**
     * The commands used in this test case are documented in
     * @see #testAfterAdvancedConfigurationPortscanShouldBeStarted() 
     */
    @Test
    public void testAutomatedScanWithoutMarkingFalsePositive() {

        Scenario scenario = Scenario.run(nmapProcess).startByKey(PROCESS_ID, defaultVariables).execute();

        assertThat(scenario.instance(nmapProcess)).isEnded();
        assertThat(scenario.instance(nmapProcess)).hasVariables(
                DefaultFields.PROCESS_CONTEXT.name(), NmapFields.NMAP_TARGET.name(), NmapFields.NMAP_TARGET_URL.name(), "markFalsePositive", NmapFields.NMAP_CONFIGURATION_TYPE.name());
        assertThat(scenario.instance(nmapProcess)).hasPassedInOrder(DO_PORTSCAN_TASK_ID, TRANSFORM_RESULTS_TASK_ID, CREATE_REPORT_TASK_ID);
        verifyExecutionListenerMock("finishedPortScanTaskListener").executed();
        verifyJavaDelegateMock("transformNmapResultsDelegate").executed();
        verifyJavaDelegateMock("summaryGeneratorDelegate").executed();
    }

    /**
     * The commands used in this test case are documented in
     * @see #testAfterAdvancedConfigurationPortscanShouldBeStarted()
     */
    @Test
    public void testAutomatedScanWithMarkingFalsePositive() {

        Map<String, Object> variables = new HashMap<>(defaultVariables);
        changeVariable(variables, "markFalsePositive", true);

        Scenario scenario = Scenario.run(nmapProcess).startByKey(PROCESS_ID, variables).execute();

        assertThat(scenario.instance(nmapProcess)).isEnded();
        assertThat(scenario.instance(nmapProcess)).hasVariables(
                DefaultFields.PROCESS_CONTEXT.name(), NmapFields.NMAP_TARGET.name(), NmapFields.NMAP_TARGET_URL.name(), "markFalsePositive", NmapFields.NMAP_CONFIGURATION_TYPE.name());
        assertThat(scenario.instance(nmapProcess)).hasPassedInOrder(DO_PORTSCAN_TASK_ID, TRANSFORM_RESULTS_TASK_ID,
                MARK_FALSE_POSITIVES_TASK_ID, CREATE_REPORT_TASK_ID);
        verifyExecutionListenerMock("finishedPortScanTaskListener").executed();
        verifyJavaDelegateMock("transformNmapResultsDelegate").executed();
        verifyJavaDelegateMock("summaryGeneratorDelegate").executed();
    }

    /**
     * The commands used in this test case are documented in
     * @see #testAfterAdvancedConfigurationPortscanShouldBeStarted()
     */
    @Test
    public void testManualRunWithApprovedTestResults() {

        Map<String, Object> variables = new HashMap<>(defaultVariables);
        changeVariable(variables, DefaultFields.PROCESS_AUTOMATED.name(), false);

        when(nmapProcess.waitsAtUserTask(APPROVE_RESULTS_TASK_ID)).thenReturn(task -> {
            variables.put(NmapFields.NMAP_RESULT_APPROVED.name(), "approved");
            task.complete(variables);
        });

        /*
        Here we register a custom mock.
        The BPMN model TaskListener takes an injected field variable which cannot be mocked.
        Therefore we create our own TaskListener with a dummy implementation and which also
        holds the variable, that should be injected.
        Then we register our TaskListener with "Mocks.register(...)" and it gets executed when the delegateExpression
        is called.
         */
        Mocks.register("setFormUrlListener", new TaskListener() {

            @Autowired
            private Expression scanner_type;

            @Override
            public void notify(DelegateTask delegateTask) {
            }
        });

        Scenario scenario = Scenario.run(nmapProcess).startByKey(PROCESS_ID, variables).execute();

        assertThat(scenario.instance(nmapProcess)).isEnded();
        assertThat(scenario.instance(nmapProcess)).hasPassed(APPROVE_RESULTS_TASK_ID);
        assertThat(scenario.instance(nmapProcess)).variables().containsEntry(NmapFields.NMAP_RESULT_APPROVED.name(), "approved");
    }

    /**
     * The commands used in this test case are documented in
     * @see #testAfterAdvancedConfigurationPortscanShouldBeStarted()
     * and in
     * @see #testManualRunWithApprovedTestResults()
     */
    @Test
    public void testManualRunWithRejectedTestResultsShouldGoBackToConfigureAdvancedScan() {

        Map<String, Object> variables = new HashMap<>(defaultVariables);
        changeVariable(variables, DefaultFields.PROCESS_AUTOMATED.name(), false);

        when(nmapProcess.waitsAtUserTask(APPROVE_RESULTS_TASK_ID)).thenReturn(task -> {
            variables.put(NmapFields.NMAP_RESULT_APPROVED.name(), "disapproved");
            task.complete(variables);
        });
        when(nmapProcess.waitsAtUserTask(CONFIGURE_ADVANCED_PORTSCAN_TASK_ID)).thenReturn(task ->  {});

        Mocks.register("setFormUrlListener", new TaskListener() {

            @Autowired
            private Expression scanner_type;

            @Override
            public void notify(DelegateTask delegateTask) {
            }
        });

        Scenario scenario = Scenario.run(nmapProcess).startByKey(PROCESS_ID, variables).execute();

        assertThat(scenario.instance(nmapProcess)).hasPassed(APPROVE_RESULTS_TASK_ID);
        assertThat(scenario.instance(nmapProcess)).variables().containsEntry(NmapFields.NMAP_RESULT_APPROVED.name(), "disapproved");
        assertThat(scenario.instance(nmapProcess)).isWaitingAt(CONFIGURE_ADVANCED_PORTSCAN_TASK_ID);
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


    /**
     * This will just stay here for future reference
     * This test uses another way to test process models which might be useful in some cases
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
//        verifyExecutionListenerMock("finishedPortScanListener").executed();
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
//        assertThat(processInstance).hasVariables(DefaultFields.PROCESS_CONTEXT.name(), NmapFields.NMAP_TARGET.name(), NmapFields.NMAP_TARGET_URL.name(), "markFalsePositive",
//                NmapFields.NMAP_CONFIGURATION_TYPE.name());
//
//        assertThat(processInstance).hasPassedInOrder(DO_PORTSCAN_TASK_ID, TRANSFORM_RESULTS_TASK_ID, CREATE_REPORT_TASK_ID);
//    }

}

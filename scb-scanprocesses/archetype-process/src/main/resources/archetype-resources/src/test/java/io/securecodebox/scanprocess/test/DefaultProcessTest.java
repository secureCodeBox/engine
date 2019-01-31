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

package io.securecodebox.scanprocess.test;

import io.securecodebox.constants.DefaultFields;
import ${package}.SummaryGeneratorDelegate;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.TaskListener;
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
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.camunda.bpm.extension.mockito.CamundaMockito.autoMock;
import static org.mockito.Mockito.when;

/**
 * This class tests the process execution of the Default-Process BPMN Model
 * It verifies that each process task is called when it's supposed to be and
 * delegation code is executed at the right time
 * <p>
 * The tests run in an own Camunda engine which is defined by the camunda.cfg.xml in the resources directory
 * <p>
 * The test cases use Camunda BPM's standard framework as well as the
 * Camunda BPM Assert extension (<a href="https://github.com/camunda/camunda-bpm-assert")/>),
 * camunda-bpm-mockito (<a href="https://github.com/camunda/camunda-bpm-mockito"/>)
 * and the Camunda BPM Assert Scenario extension (<a href="https://github.com/camunda/camunda-bpm-assert-scenario"/>)
 * <p>
 * Furthermore this class also uses the Camunda BPM Process Test Coverage extension
 * (<a href="https://github.com/camunda/camunda-bpm-process-test-coverage"/>).
 * After the test is run we can examine the test coverage in the directory target/process-test-coverage
 */

@RunWith(SpringJUnit4ClassRunner.class)
@Deployment(resources = "bpmn/default_process.bpmn")
public class DefaultProcessTest {

    //Define the Process Activity IDs
    private static final String PROCESS_ID = "$processTopic";
    private static final String DO_SCAN_TASK_ID = "ServiceTask_DoScan";
    private static final String CREATE_REPORT_TASK_ID = "ServiceTask_CreateSummary";
    private static final String APPROVE_RESULTS_TASK_ID = "UserTask_ApproveResults";

    private final Map<String, Object> defaultVariables = new HashMap<>();

    @Rule
    @ClassRule
    public static ProcessEngineRule processEngineRule = TestCoverageProcessEngineRuleBuilder.create().build();

    @Mock
    private ProcessScenario process;

    @Mock
    SummaryGeneratorDelegate delegate;

    /**
     * Executed before every test-case
     * In this method default variables for the process and a default behaviour for the mocks
     * in the process are defined+
     */
    @Before
    public void init() {

        MockitoAnnotations.initMocks(this);

        //Creating a map of default variables for the process
        defaultVariables.put(DefaultFields.PROCESS_AUTOMATED.name(), true);
        defaultVariables.put(DefaultFields.PROCESS_CONTEXT.name(), "BodgeIT");

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

        If you have the path to your executable code (the class for delegate) as delegate implementation
        then this guide is helpful:
        https://blog.akquinet.de/2016/11/04/camunda-bpm-test-your-processes-based-on-plain-old-java-delegates/
         */
        autoMock("bpmn/default_process.bpmn");

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
        when(process.waitsAtUserTask(Mockito.anyString())).thenReturn(TaskDelegate::complete);
        when(process.waitsAtServiceTask(Mockito.anyString())).thenReturn(ExternalTaskDelegate::complete);
        when(process.waitsAtServiceTask(DO_SCAN_TASK_ID)).thenReturn(task -> startExternalMockProcess("$processTopic"));
    }

    @Test
    public void testAutomatedStart_shouldPass() {

        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_ID, defaultVariables);

        assertThat(processInstance).isStarted();
    }

    @Test
    public void testManualStartWithDefaultConfiguration_shouldPass() {
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_ID, defaultVariables);

        assertThat(processInstance).isStarted();
        assertThat(processInstance).isWaitingAt(DO_SCAN_TASK_ID);
    }

    @Test
    public void testManualRunWithApprovedTestResults() {

        Map<String, Object> variables = new HashMap<>(defaultVariables);
        changeVariable(variables, DefaultFields.PROCESS_AUTOMATED.name(), false);

        when(process.waitsAtUserTask(APPROVE_RESULTS_TASK_ID)).thenReturn(task -> {
            variables.put(DefaultFields.PROCESS_RESULT_APPROVED.name(), "approved");
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

        Scenario scenario = Scenario.run(process).startByKey(PROCESS_ID, variables).execute();

        assertThat(scenario.instance(process)).isEnded();
        assertThat(scenario.instance(process)).hasPassed(APPROVE_RESULTS_TASK_ID);
        assertThat(scenario.instance(process)).variables()
                .containsEntry(DefaultFields.PROCESS_RESULT_APPROVED.name(), "approved");
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
                .topic(topic, 5000L)
                .execute();

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

}

import io.securecodebox.constants.DefaultFields;
import io.securecodebox.scanprocess.NmapScanProcessExecution;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.mockito.mock.FluentExecutionListenerMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.processEngine;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.execute;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.job;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.extension.mockito.CamundaMockito.autoMock;
import static org.camunda.bpm.extension.mockito.CamundaMockito.registerExecutionListenerMock;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@Deployment(resources = "bpmn/nmap_scan.bpmn")
public class NmapProcessTest {

    private static final String PROCESS_ID = "SecureBoxExternalPortscan";
    private static final String DO_PORTSCAN_TASK_ID = "ServiceTask_DoPortscan";
    private static final String TRANSFORM_RESULTS_TASK_ID = "ServiceTask_TransformResult";

    private final Map<String, Object> defaultVariables = new HashMap<>();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Before
    public void init(){
        defaultVariables.put(DefaultFields.PROCESS_AUTOMATED.name(), true);
    }

    @Test
    public void testDeployment(){}

    @Test
    public void testAutomatedStart_shouldPass(){
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_ID, defaultVariables);
        assertThat(processInstance).isStarted();
    }

    @Test(expected = ProcessEngineException.class)
    public void testStartWithoutVariables_shouldFail(){
        runtimeService().startProcessInstanceByKey(PROCESS_ID);
    }

    @Test(expected = ProcessEngineException.class)
    public void testManualStartWithoutConfiguration_shouldFail(){
        Map<String, Object> variables = new HashMap<>();
        variables.put(DefaultFields.PROCESS_AUTOMATED.name(), false);
        runtimeService().startProcessInstanceByKey(PROCESS_ID, variables);
    }

    @Test
    public void testManualStartWithConfiguration_shouldPass(){
        Map<String, Object> variables = new HashMap<>();
        variables.put(DefaultFields.PROCESS_AUTOMATED.name(), false);
        //todo: comment this in, when NmapFields exist
//        variables.put(NmapFields.CONFIGURATION_TYPE.name(), "default");
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_ID, variables);
        assertThat(processInstance).isStarted(); }



    @Test
    public void testScanProcess(){

        autoMock("bpmn/nmap_scan.bpmn");
        Map<String, Object> variables = new HashMap<>();
        variables.put(DefaultFields.PROCESS_AUTOMATED.name(), false);
        variables.put("context", "BodgeIT");
        variables.put("nmap_target", "localhost");
        variables.put("targetName", "BodgeIT Public Host");
        variables.put("markFalsePositive", false);
        variables.put("nmap_configuration_type", "default");
        ProcessInstance processInstance = processEngine().getRuntimeService().startProcessInstanceByKey(PROCESS_ID, variables);

        assertThat(processInstance).isStarted();
        //Make sure that the next task to execute is "Run Portscan"
        assertEquals(Collections.singletonList(DO_PORTSCAN_TASK_ID), runtimeService().getActiveActivityIds(processInstance.getId()));

        //Execute the next job
        execute(job());

        startMockPortscan();
        assertThat(processInstance).isWaitingAt(TRANSFORM_RESULTS_TASK_ID);

//
//        //Make sure the next task is the "Transform Generic Results"
//        assertEquals(Collections.singletonList(TRANSFORM_RESULTS_TASK_ID), runtimeService().getActiveActivityIds(processInstance.getId()));

    }


    private void startMockPortscan(){

        ExternalTaskService externalTaskService = processEngine().getExternalTaskService();
        List<LockedExternalTask> lockedExternalTasks = externalTaskService.fetchAndLock(1, "worker")
                .topic("nmap_portscan", 5000L).execute();

        assertThat(lockedExternalTasks.size()).isEqualTo(1);

        LockedExternalTask task = lockedExternalTasks.get(0);
        externalTaskService.complete(task.getId(), "worker");

    }

}

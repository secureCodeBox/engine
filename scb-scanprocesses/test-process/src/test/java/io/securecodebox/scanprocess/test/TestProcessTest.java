package io.securecodebox.scanprocess.test;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.camunda.bpm.extension.mockito.CamundaMockito.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

@RunWith(SpringJUnit4ClassRunner.class)
@Deployment(resources = "bpmn/sample.bpmn")
public class TestProcessTest {

    private static final String SAMPLE_SERVICE_ID = "Sample";
    private static final String TASK_DO_SOMETHING_ID = "UserTask_1";
    private static final String TASK_SAY_HELLO_ID = "ServiceTask_1";

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    //Best practice to test if the deployment of the BPMN model is successful
    @Test
    public void testDeployment(){}

    @Test
    public void testSuccessfulExecutionOfProcess(){

        //Mock everything: ExecutionListeners, TaskListeners, Eventlisteners, JavaDelegates, etc.
        autoMock("bpmn/sample.bpmn");

        //Start the Service
        ProcessInstance processInstance = processEngine().getRuntimeService().startProcessInstanceByKey(SAMPLE_SERVICE_ID);

        //check that the Service has started and waits at the UserTask
        assertThat(processInstance).isStarted()
                .isWaitingAt(TASK_DO_SOMETHING_ID);

        //Execute the next task, which should be the UserTask
        complete(task(), Variables.createVariables());

        //Make sure the next task is the "Say Hello" ServiceTask
        assertEquals(Collections.singletonList(TASK_SAY_HELLO_ID), runtimeService().getActiveActivityIds(processInstance.getId()));

        //execute the Service Task
        execute(job());

        //Check if the SayHelloDelegate was called
        verifyJavaDelegateMock("sayHelloDelegate").executed(times(1));

        //Check if every task has been executed and the process finished
        assertThat(processInstance).isEnded().hasPassed(TASK_DO_SOMETHING_ID, TASK_SAY_HELLO_ID);
    }
}

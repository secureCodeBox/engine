import io.securecodebox.scanprocess.example.SayHelloDelegate;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.junit.Assert.assertEquals;

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

        //Start the Service
        ProcessInstance processInstance = processEngine().getRuntimeService().startProcessInstanceByKey(SAMPLE_SERVICE_ID);

        //Register the SayHelloDelegate in the Test Configuration
        SayHelloDelegate sayHelloDelegate = new SayHelloDelegate();
        Mocks.register("sayHelloDelegate", sayHelloDelegate);

        //check that the Service has started and waits at the UserTask
        assertThat(processInstance).isStarted()
                .isWaitingAt(TASK_DO_SOMETHING_ID);

        //Execute the next task, which should be the UserTask
        complete(task(), Variables.createVariables());

        //Make sure the next task is the "Say Hello" ServiceTask
        assertEquals(Collections.singletonList(TASK_SAY_HELLO_ID), runtimeService().getActiveActivityIds(processInstance.getId()));

        //execute the Service Task
        execute(job());

        try {

            //Check if the SayHelloDelegate was called
            Mockito.verify(sayHelloDelegate, Mockito.times(1)).execute(Mockito.any(DelegateExecution.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Check if every task has been executed and the process finished
        assertThat(processInstance).isEnded().hasPassed(TASK_DO_SOMETHING_ID, TASK_SAY_HELLO_ID);
    }
}

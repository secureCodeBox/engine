import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@Deployment(resources = "bpmn/nmap_scan.bpmn")
public class NmapProcessTest {

    private static final String PROCESS_ID = "SecureBoxExternalPortscan";
    private static final String DO_PORTSCAN_TASK_ID = "ServiceTask_DoPortscan";
    private static final String TRANSFORM_RESULTS_TASK_ID = "ServiceTask_TransformResult";

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Test
    public void testDeployment(){}

    @Test
    public void testScanProcess(){

    }
}

package org.camunda.bpm.spring.boot.example.webapp;

import io.securecodebox.engine.SecureCodeBoxEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SecureCodeBoxEngine.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationIT {

  @Test
  public void startUpTest() {
    // context init test
  }

}

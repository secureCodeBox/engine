package io.securecodebox.scanner.example;

import io.securecodebox.sdk.ScannerEntryPoint;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ScannerEntryPoint
public class SayHelloDelegate implements JavaDelegate {

  private static final Logger LOGGER = LoggerFactory.getLogger(SayHelloDelegate.class);

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    LOGGER.info("hello {}", execution);
  }

}

package io.securecodebox.scanprocess.zap;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class FinishedSpiderTaskListener implements ExecutionListener {


    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {

    }
}

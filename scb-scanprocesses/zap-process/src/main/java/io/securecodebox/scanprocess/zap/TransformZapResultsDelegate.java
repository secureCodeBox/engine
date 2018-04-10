package io.securecodebox.scanprocess.zap;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This delegate transforms the raw ZAP Results into the generic findings
 */
public class TransformZapResultsDelegate  implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(TransformZapResultsDelegate.class);

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

    }
}

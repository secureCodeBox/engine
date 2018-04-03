/**
 *
 */
package io.securecodebox.scanprocess.nmap;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;
/**
 * @author Jannik Hollenbach - iteratec GmbH
 * Sets the URL to the form used to display the results.
 * When false positive detection is enabled in the system, the generic form will always be used.
 */
@Component
public class SetFormUrlListener implements TaskListener {

    /**
     * The Type of the Scanner should be injected into the listener using Field Injection
     * See https://docs.camunda.org/manual/latest/user-guide/process-engine/delegation-code/#field-injection
     */
    private Expression scanner_type;

    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.setVariable("result_form_url","embedded:app:forms/portscan/approve-port-scanner-results.html");    }
}

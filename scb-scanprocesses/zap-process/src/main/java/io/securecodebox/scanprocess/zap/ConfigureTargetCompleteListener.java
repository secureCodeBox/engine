package io.securecodebox.scanprocess.zap;

import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import io.securecodebox.scanprocess.ZapScanProcessExecution;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigureTargetCompleteListener implements ExecutionListener {

    @Autowired
    private ScanProcessExecutionFactory scanProcessExecutionFactory;

    /* (non-Javadoc)
     * @see org.camunda.bpm.engine.delegate.ExecutionListener#notify(org.camunda.bpm.engine.delegate.DelegateExecution)
     */
    @Override
    public void notify(DelegateExecution execution) throws Exception {

        ZapScanProcessExecution process = scanProcessExecutionFactory.get(execution, ZapScanProcessExecution.class);

        // Define default values if the current process is not
        // started by another automated process (Jenkins, Bamboo) with
        // already defined process variables.

            if(process.getTargetUrl() != null && !process.getTargetUrl().isEmpty()) {

                // define some default values as authentication example
                if (process.getAuthentication()) {
                    process.setLoginSite("http://bodgeit:8080/bodgeit/login.jsp");
                    process.setLoginUserName("user@mail.com");
                    process.setUsernameFieldId("username");
                    process.setLoginPassword("testing");
                    process.setPasswordFieldId("password");
                    process.setLoggedInIndicator("User: <a href=\"password.jsp\">user@mail.com</a>");
                }

                // add default spider values:
                process.setSpiderTargetUrl(process.getTargetUrl());
                process.setSpiderIncludeRegexes("\\Q" + process.getTargetUrl() + "\\E.*");
                process.setSpiderExcludeRegexes("");
                process.setSpiderExcludeDuplicates("[]");
                process.setSpiderMaxDepth("1");

                // add default scanner values:
                process.setScannerTargetUrl(process.getTargetUrl());
                process.setScannerIncludeRegexes("\\Q" + process.getTargetUrl() + "\\E.*");
                process.setScannerExcludeRegexes("");

                String[] reportingTypes = {"kibana"};
                process.setReportingTypes(reportingTypes);
                process.setReportingFalsePositives("[]");

        }
    }
}

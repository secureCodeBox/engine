package io.securecodebox.engine.history;

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.stereotype.Component;

@Component
public class HistoryCleanupConfig  extends AbstractProcessEnginePlugin {
    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("20:00");
        processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("24:00");
    }
}
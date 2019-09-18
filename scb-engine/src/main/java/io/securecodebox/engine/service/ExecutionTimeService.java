package io.securecodebox.engine.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.history.HistoricProcessInstance;

import java.util.Date;
import java.util.Optional;

public class ExecutionTimeService {

    private DelegateExecution execution;

    public ExecutionTimeService(DelegateExecution execution){
        this.execution = execution;
    }

    private Optional<HistoricProcessInstance> getHistoricProcessInstance(){
        return execution.getProcessEngineServices()
                .getHistoryService()
                .createHistoricProcessInstanceQuery()
                .processInstanceId(execution.getProcessInstanceId())
                .list()
                .stream()
                .findFirst();
    }

    public Date getStartDate(){
        return getHistoricProcessInstance()
                .orElseThrow(() -> new RuntimeException("Failed to finding process"))
                .getStartTime();
    }

    public Optional<Date> getEndDate(){
        return Optional.ofNullable(
                getHistoricProcessInstance()
                        .orElseThrow(() -> new RuntimeException("Failed to finding process"))
                        .getEndTime()
        );
    }
}

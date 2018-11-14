package io.securecodebox.engine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.rest.Report;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class ReportService {
    @Autowired
    ProcessEngine engine;
    @Autowired
    ObjectMapper objectMapper;

    private static final Logger LOG = LoggerFactory.getLogger(ReportService.class);

    public static class ReportNotFoundException extends Exception{}

    public Report getReport(UUID securityTestId) throws ReportNotFoundException, IOException {
        List<HistoricVariableInstance> variables = engine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .variableName(DefaultFields.PROCESS_REPORT.name())
                .processInstanceId(securityTestId.toString())
                .list();

        if (variables.size() != 1) {
            throw new ReportNotFoundException();
        }

        String reportDoubilySerialized = (String) variables.get(0).getValue();

        String reportString = objectMapper.readValue(reportDoubilySerialized, String.class);
        return objectMapper.readValue(reportString, Report.class);
    }
}
package io.securecodebox.engine.rest;

import io.securecodebox.engine.service.ReportService;
import io.securecodebox.model.rest.Report;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class ReportResourceTest {
    @InjectMocks
    ReportResource classUnderTest;

    @Mock
    ReportService reportServiceServiceDummy;

    @Test
    public void shouldReturnAReportIfFound() throws Exception {
        Report report = new Report();
        report.setContext("Foobar");
        UUID id = UUID.fromString("82896999-1bf8-4824-aeb2-f87b991cd773");
        report.setSecurityTestId(id);

        given(reportServiceServiceDummy.getReport(any())).willReturn(report);
        ResponseEntity<Report> response = classUnderTest.getReport(id);

        assertEquals("Foobar", response.getBody().getContext());
        assertEquals(id, response.getBody().getSecurityTestId());
    }

    @Test
    public void shouldReturnA404IfReportWasntFound() throws Exception {
        given(reportServiceServiceDummy.getReport(any())).willThrow(new ReportService.ReportNotFoundException());
        ResponseEntity<Report> response = classUnderTest.getReport(UUID.randomUUID());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void shouldReturnA500IfReportCouldNotGetDeserialized() throws Exception {
        given(reportServiceServiceDummy.getReport(any())).willThrow(new IOException());
        ResponseEntity<Report> response = classUnderTest.getReport(UUID.randomUUID());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
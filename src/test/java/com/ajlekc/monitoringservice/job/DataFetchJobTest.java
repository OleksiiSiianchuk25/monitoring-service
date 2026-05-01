package com.ajlekc.monitoringservice.job;

import com.ajlekc.monitoringservice.client.UserClient;
import com.ajlekc.monitoringservice.model.ChangeType;
import com.ajlekc.monitoringservice.model.JobRun;
import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.service.JobAuditService;
import com.ajlekc.monitoringservice.service.UserProcessingService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataFetchJobTest {

    @Mock
    private UserClient userClient;

    @Mock
    private UserProcessingService processingService;

    @Mock
    private JobAuditService jobAuditService;

    private DataFetchJob dataFetchJob;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        dataFetchJob = new DataFetchJob(userClient, processingService, meterRegistry, jobAuditService);
    }

    @Test
    void shouldFetchAndProcessUser_andRecordSuccessfulRun_whenChangeIsNew() {
        JobRun startedRun = new JobRun();
        when(jobAuditService.start()).thenReturn(startedRun);

        User mockUser = new User();
        when(userClient.fetchUserById(anyInt())).thenReturn(mockUser);
        when(processingService.processAndSave(mockUser)).thenReturn(ChangeType.NEW);

        dataFetchJob.execute();

        verify(userClient, times(1)).fetchUserById(anyInt());
        verify(processingService, times(1)).processAndSave(mockUser);

        Counter successCounter = meterRegistry.find("monitoring.fetch.operations").tag("result", "success").counter();
        assertEquals(1.0, successCounter.count());

        ArgumentCaptor<JobAuditService.JobRunCounters> captor =
                ArgumentCaptor.forClass(JobAuditService.JobRunCounters.class);
        verify(jobAuditService).finishSuccess(eq(startedRun), captor.capture());

        JobAuditService.JobRunCounters counters = captor.getValue();
        assertThat(counters.fetched()).isEqualTo(1);
        assertThat(counters.created()).isEqualTo(1);
        assertThat(counters.updated()).isZero();
        assertThat(counters.unchanged()).isZero();
        assertThat(counters.skipped()).isZero();

        verify(jobAuditService, never()).finishFailure(any(), anyString());
    }

    @Test
    void shouldRecordSkipped_whenProcessingReturnsNull() {
        JobRun startedRun = new JobRun();
        when(jobAuditService.start()).thenReturn(startedRun);

        when(userClient.fetchUserById(anyInt())).thenReturn(null);
        when(processingService.processAndSave(null)).thenReturn(null);

        dataFetchJob.execute();

        ArgumentCaptor<JobAuditService.JobRunCounters> captor =
                ArgumentCaptor.forClass(JobAuditService.JobRunCounters.class);
        verify(jobAuditService).finishSuccess(eq(startedRun), captor.capture());

        JobAuditService.JobRunCounters counters = captor.getValue();
        assertThat(counters.fetched()).isEqualTo(1);
        assertThat(counters.skipped()).isEqualTo(1);
        assertThat(counters.created()).isZero();
    }

    @Test
    void shouldHandleException_andRecordFailedRun() {
        JobRun startedRun = new JobRun();
        when(jobAuditService.start()).thenReturn(startedRun);
        when(userClient.fetchUserById(anyInt())).thenThrow(new RuntimeException("API Error"));

        dataFetchJob.execute();

        verify(processingService, never()).processAndSave(any());

        Counter failureCounter = meterRegistry.find("monitoring.fetch.operations").tag("result", "failure").counter();
        assertEquals(1.0, failureCounter.count());

        verify(jobAuditService).finishFailure(eq(startedRun), eq("API Error"));
        verify(jobAuditService, never()).finishSuccess(any(), any());
    }
}
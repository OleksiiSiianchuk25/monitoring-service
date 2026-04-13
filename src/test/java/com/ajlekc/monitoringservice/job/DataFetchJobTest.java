package com.ajlekc.monitoringservice.job;

import com.ajlekc.monitoringservice.client.UserClient;
import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.service.UserProcessingService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataFetchJobTest {

    @Mock
    private UserClient userClient;

    @Mock
    private UserProcessingService processingService;

    private DataFetchJob dataFetchJob;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();

        dataFetchJob = new DataFetchJob(userClient, processingService, meterRegistry);
    }

    @Test
    void testShouldFetchAndProcessUser() {
        User mockUser = new User();
        when(userClient.fetchUserById(anyInt())).thenReturn(mockUser);

        dataFetchJob.execute();

        verify(userClient, times(1)).fetchUserById(anyInt());
        verify(processingService, times(1)).processAndSave(mockUser);

        Counter successCounter = meterRegistry.find("monitoring.fetch.operations").tag("result", "success").counter();
        assertEquals(1.0, successCounter.count());
    }

    @Test
    void testShouldHandleException() {
        when(userClient.fetchUserById(anyInt())).thenThrow(new RuntimeException("API Error"));

        dataFetchJob.execute();

        verify(processingService, never()).processAndSave(any());

        Counter failureCounter = meterRegistry.find("monitoring.fetch.operations").tag("result", "failure").counter();
        assertEquals(1.0, failureCounter.count());
    }
}
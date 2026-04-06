package com.ajlekc.monitoringservice.job;

import com.ajlekc.monitoringservice.client.UserClient;
import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.service.UserProcessingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataFetchJobTest {

    @Mock
    private UserClient userClient;

    @Mock
    private UserProcessingService processingService;

    @InjectMocks
    private DataFetchJob dataFetchJob;

    @Test
    void testShouldFetchAndProcessUser() {
        User mockUser = new User();
        when(userClient.fetchUserById(anyInt())).thenReturn(mockUser);

        dataFetchJob.execute();

        verify(userClient, times(1)).fetchUserById(anyInt());
        verify(processingService, times(1)).processAndSave(mockUser);
    }

    @Test
    void testShouldHandleException() {
        when(userClient.fetchUserById(anyInt())).thenThrow(new RuntimeException("API Error"));

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> dataFetchJob.execute()
        );

        verify(processingService, never()).processAndSave(any());
    }
}
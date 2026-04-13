package com.ajlekc.monitoringservice.service;

import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.repository.UserRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProcessingServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserProcessingService userProcessingService;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        userProcessingService = new UserProcessingService(userRepository, meterRegistry);

        userProcessingService.initMetrics();
    }

    @Test
    void testShouldSaveUserWhenUserIsNotNull() {
        User testUser = new User();
        testUser.setName("Test User");
        testUser.setExternalId(1);

        userProcessingService.processAndSave(testUser);

        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testShouldNotSaveWhenUserIsNull() {
        userProcessingService.processAndSave(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void testMetricsInitialization() {
        when(userRepository.count()).thenReturn(42L);

        double gaugeValue = meterRegistry.get("monitoring.records.stored").gauge().value();
        assertEquals(42.0, gaugeValue);
    }
}
package com.ajlekc.monitoringservice.service;

import com.ajlekc.monitoringservice.model.ChangeType;
import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.repository.UserRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProcessingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserChangeDetector changeDetector;

    @Mock
    private UserEventProducer eventProducer;

    private UserProcessingService userProcessingService;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();

        userProcessingService = new UserProcessingService(userRepository, changeDetector, meterRegistry, eventProducer);

        userProcessingService.initMetrics();
    }

    @Test
    void shouldSaveAndReturnNew_whenChangeIsNew() {
        User testUser = new User();
        testUser.setName("Test User");
        testUser.setExternalId(1);

        when(changeDetector.classifyFetchedUser(testUser)).thenReturn(ChangeType.NEW);

        ChangeType result = userProcessingService.processAndSave(testUser);

        assertThat(result).isEqualTo(ChangeType.NEW);
        verify(userRepository, times(1)).save(testUser);

        verify(eventProducer, times(1)).publishUserChangedEvent(testUser, ChangeType.NEW);
        assertThat(meterRegistry.counter("monitoring.records.changes", "type", "new")
                .count()).isEqualTo(1.0);
    }

    @Test
    void shouldSaveAndReturnUpdated_whenChangeIsUpdated() {
        User testUser = new User();
        testUser.setExternalId(1);

        when(changeDetector.classifyFetchedUser(testUser)).thenReturn(ChangeType.UPDATED);

        ChangeType result = userProcessingService.processAndSave(testUser);

        assertThat(result).isEqualTo(ChangeType.UPDATED);
        verify(userRepository, times(1)).save(testUser);

        verify(eventProducer, times(1)).publishUserChangedEvent(testUser, ChangeType.UPDATED);
        assertThat(meterRegistry.counter("monitoring.records.changes", "type", "updated")
                .count()).isEqualTo(1.0);
    }

    @Test
    void shouldNotSaveAndReturnUnchanged_whenChangeIsUnchanged() {
        User testUser = new User();
        testUser.setExternalId(1);

        when(changeDetector.classifyFetchedUser(testUser)).thenReturn(ChangeType.UNCHANGED);

        ChangeType result = userProcessingService.processAndSave(testUser);

        assertThat(result).isEqualTo(ChangeType.UNCHANGED);
        verify(userRepository, never()).save(any());

        verify(eventProducer, never()).publishUserChangedEvent(any(), any());
        assertThat(meterRegistry.counter("monitoring.records.changes", "type", "unchanged")
                .count()).isEqualTo(1.0);
    }

    @Test
    void shouldNotSaveAndReturnNull_whenUserIsNull() {
        ChangeType result = userProcessingService.processAndSave(null);

        assertNull(result);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(changeDetector);
        verifyNoInteractions(eventProducer);
    }

    @Test
    void testMetricsInitialization() {
        when(userRepository.count()).thenReturn(42L);

        double gaugeValue = meterRegistry.get("monitoring.records.stored").gauge().value();
        assertEquals(42.0, gaugeValue);
    }
}
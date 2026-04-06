package com.ajlekc.monitoringservice.service;

import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProcessingServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserProcessingService userProcessingService;

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
}
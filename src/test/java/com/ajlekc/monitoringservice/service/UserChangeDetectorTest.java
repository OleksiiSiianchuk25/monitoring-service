package com.ajlekc.monitoringservice.service;

import com.ajlekc.monitoringservice.model.ChangeType;
import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserChangeDetectorTest {

    @Mock
    private UserRepository userRepository;

    private UserChangeDetector detector;

    @BeforeEach
    void setUp() {
        detector = new UserChangeDetector(userRepository);
    }

    @Test
    void classify_returnsNew_whenIncomingIsNull() {
        assertThat(detector.classifyFetchedUser(null)).isEqualTo(ChangeType.NEW);
    }

    @Test
    void classify_returnsNew_whenExternalIdIsNull() {
        User incoming = new User();
        incoming.setName("Test User");

        assertThat(detector.classifyFetchedUser(incoming)).isEqualTo(ChangeType.NEW);
    }

    @Test
    void classify_returnsNew_whenNoExistingUserFound() {
        User incoming = new User();
        incoming.setExternalId(1);
        incoming.setName("Test User");

        when(userRepository.findFirstByExternalIdOrderByInternalId(1)).thenReturn(Optional.empty());

        assertThat(detector.classifyFetchedUser(incoming)).isEqualTo(ChangeType.NEW);
    }

    @Test
    void classify_returnsUnchanged_whenAllFieldsMatch() {
        User existing = sampleUser("internal-1");
        User incoming = sampleUser(null);

        when(userRepository.findFirstByExternalIdOrderByInternalId(1)).thenReturn(Optional.of(existing));

        ChangeType result = detector.classifyFetchedUser(incoming);

        assertThat(result).isEqualTo(ChangeType.UNCHANGED);
        assertThat(incoming.getInternalId()).isEqualTo("internal-1");
    }

    @Test
    void classify_returnsUpdated_whenNameDiffers() {
        User existing = sampleUser("internal-1");
        User incoming = sampleUser(null);
        incoming.setName("New Name");

        when(userRepository.findFirstByExternalIdOrderByInternalId(1)).thenReturn(Optional.of(existing));

        ChangeType result = detector.classifyFetchedUser(incoming);

        assertThat(result).isEqualTo(ChangeType.UPDATED);
        assertThat(incoming.getInternalId()).isEqualTo("internal-1");
    }

    @Test
    void classify_returnsUpdated_whenEmailDiffers() {
        User existing = sampleUser("internal-1");
        User incoming = sampleUser(null);
        incoming.setEmail("changed@example.com");

        when(userRepository.findFirstByExternalIdOrderByInternalId(1)).thenReturn(Optional.of(existing));

        assertThat(detector.classifyFetchedUser(incoming)).isEqualTo(ChangeType.UPDATED);
    }

    private static User sampleUser(String internalId) {
        User user = new User();
        user.setInternalId(internalId);
        user.setExternalId(1);
        user.setName("Test User");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPhone("123-456");
        user.setWebsite("test.example");
        return user;
    }
}
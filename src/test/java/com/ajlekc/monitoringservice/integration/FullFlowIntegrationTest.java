package com.ajlekc.monitoringservice.integration;

import com.ajlekc.monitoringservice.model.Address;
import com.ajlekc.monitoringservice.model.ChangeType;
import com.ajlekc.monitoringservice.model.Company;
import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.repository.JobRunRepository;
import com.ajlekc.monitoringservice.repository.UserRepository;
import com.ajlekc.monitoringservice.service.UserChangeDetector;
import com.ajlekc.monitoringservice.service.UserProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "app.scheduling.enabled=false",
        "external.mock-api.base-url=http://localhost:9999/mock-users"
})
@WithMockUser(username = "admin", roles = {"ADMIN"})
class FullFlowIntegrationTest extends TestcontainersConfig {

    @Autowired
    private UserProcessingService processingService;

    @Autowired
    private UserChangeDetector changeDetector;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRunRepository jobRunRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        jobRunRepository.deleteAll();
    }

    @Test
    void shouldClassifyAndSaveNewUser() {
        User user = createTestUser(1, "New User", "new@test.com");

        ChangeType result = processingService.processAndSave(user);

        assertThat(result).isEqualTo(ChangeType.NEW);
        assertThat(userRepository.count()).isEqualTo(1);

        Optional<User> saved = userRepository.findFirstByExternalIdOrderByInternalId(1);
        assertThat(saved).isPresent();
        assertThat(saved.get().getName()).isEqualTo("New User");
    }

    @Test
    void shouldClassifyAsUnchangedWhenSameDataFetchedAgain() {
        User user = createTestUser(1, "Same User", "same@test.com");

        // First save - NEW
        ChangeType firstResult = processingService.processAndSave(user);
        assertThat(firstResult).isEqualTo(ChangeType.NEW);

        // Fetch the same data again - UNCHANGED
        User sameUser = createTestUser(1, "Same User", "same@test.com");
        ChangeType secondResult = processingService.processAndSave(sameUser);
        assertThat(secondResult).isEqualTo(ChangeType.UNCHANGED);

        // Still only one record in DB
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldClassifyAsUpdatedWhenDataChanges() {
        User user = createTestUser(1, "Original Name", "original@test.com");
        processingService.processAndSave(user);

        // Same external ID, different data
        User updatedUser = createTestUser(1, "Updated Name", "updated@test.com");
        ChangeType result = processingService.processAndSave(updatedUser);

        assertThat(result).isEqualTo(ChangeType.UPDATED);
        // Still one record (updated in place)
        assertThat(userRepository.count()).isEqualTo(1);

        Optional<User> found = userRepository.findFirstByExternalIdOrderByInternalId(1);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Name");
        assertThat(found.get().getEmail()).isEqualTo("updated@test.com");
    }

    @Test
    void shouldHandleMultipleUsersIndependently() {
        User user1 = createTestUser(1, "Alice", "alice@test.com");
        User user2 = createTestUser(2, "Bob", "bob@test.com");

        assertThat(processingService.processAndSave(user1)).isEqualTo(ChangeType.NEW);
        assertThat(processingService.processAndSave(user2)).isEqualTo(ChangeType.NEW);
        assertThat(userRepository.count()).isEqualTo(2);

        // Update user1, user2 unchanged
        User updatedUser1 = createTestUser(1, "Alice Updated", "alice.new@test.com");
        User sameUser2 = createTestUser(2, "Bob", "bob@test.com");

        assertThat(processingService.processAndSave(updatedUser1)).isEqualTo(ChangeType.UPDATED);
        assertThat(processingService.processAndSave(sameUser2)).isEqualTo(ChangeType.UNCHANGED);

        // Still 2 users
        assertThat(userRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldReturnNullForNullUser() {
        ChangeType result = processingService.processAndSave(null);

        assertThat(result).isNull();
        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void shouldDetectChangeInAddressField() {
        User user = createTestUser(1, "Test User", "test@test.com");
        processingService.processAndSave(user);

        // Change only the address
        User userWithNewAddress = createTestUser(1, "Test User", "test@test.com");
        Address newAddress = new Address();
        newAddress.setStreet("New Street");
        newAddress.setCity("New City");
        newAddress.setZipcode("99999");
        userWithNewAddress.setAddress(newAddress);

        ChangeType result = processingService.processAndSave(userWithNewAddress);

        assertThat(result).isEqualTo(ChangeType.UPDATED);
    }

    private User createTestUser(int externalId, String name, String email) {
        User user = new User();
        user.setExternalId(externalId);
        user.setName(name);
        user.setUsername(name.toLowerCase().replace(" ", "_"));
        user.setEmail(email);
        user.setPhone("555-0000");
        user.setWebsite("test.com");

        Address address = new Address();
        address.setStreet("Test Street");
        address.setCity("Test City");
        address.setZipcode("00000");
        user.setAddress(address);

        Company company = new Company();
        company.setName("Test Corp");
        user.setCompany(company);

        return user;
    }
}
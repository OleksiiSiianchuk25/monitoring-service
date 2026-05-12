package com.ajlekc.monitoringservice.integration;

import com.ajlekc.monitoringservice.model.Address;
import com.ajlekc.monitoringservice.model.Company;
import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "app.scheduling.enabled=false",
        "external.mock-api.base-url=http://localhost:9999/mock-users"
})
class UserRepositoryIntegrationTest extends TestcontainersConfig {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveUser() {
        User user = createTestUser(1, "Alice", "alice@test.com");

        User saved = userRepository.save(user);

        assertThat(saved.getInternalId()).isNotNull();
        assertThat(saved.getExternalId()).isEqualTo(1);
        assertThat(saved.getName()).isEqualTo("Alice");
    }

    @Test
    void shouldFindUserByExternalId() {
        User user = createTestUser(5, "Bob", "bob@test.com");
        userRepository.save(user);

        Optional<User> found = userRepository.findFirstByExternalIdOrderByInternalId(5);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Bob");
        assertThat(found.get().getEmail()).isEqualTo("bob@test.com");
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        Optional<User> found = userRepository.findFirstByExternalIdOrderByInternalId(999);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldUpdateExistingUser() {
        User user = createTestUser(3, "Charlie", "charlie@test.com");
        User saved = userRepository.save(user);

        saved.setName("Charlie Updated");
        saved.setEmail("charlie.new@test.com");
        userRepository.save(saved);

        Optional<User> found = userRepository.findFirstByExternalIdOrderByInternalId(3);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Charlie Updated");
        assertThat(found.get().getEmail()).isEqualTo("charlie.new@test.com");
    }

    @Test
    void shouldCountUsers() {
        userRepository.save(createTestUser(1, "User1", "u1@test.com"));
        userRepository.save(createTestUser(2, "User2", "u2@test.com"));
        userRepository.save(createTestUser(3, "User3", "u3@test.com"));

        assertThat(userRepository.count()).isEqualTo(3);
    }

    private User createTestUser(int externalId, String name, String email) {
        User user = new User();
        user.setExternalId(externalId);
        user.setName(name);
        user.setUsername(name.toLowerCase());
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

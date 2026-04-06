package com.ajlekc.monitoringservice.client;

import com.ajlekc.monitoringservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserClient userClient;

    private final String baseUrl = "https://mock-api.com/users";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userClient, "baseUrl", baseUrl);
    }

    @Test
    void testFetchUserByIdShouldReturnUser() {
        int userId = 1;
        String expectedUrl = baseUrl + "/" + userId;
        User expectedUser = new User();
        expectedUser.setExternalId(userId);

        when(restTemplate.getForObject(expectedUrl, User.class)).thenReturn(expectedUser);

        User actualUser = userClient.fetchUserById(userId);

        assertNotNull(actualUser);
        assertEquals(userId, actualUser.getExternalId());
        verify(restTemplate, times(1)).getForObject(expectedUrl, User.class);
    }
}
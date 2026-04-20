package com.ajlekc.monitoringservice.client;

import com.ajlekc.monitoringservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(properties = "external.mock-api.base-url=http://test-server.com/users")
class UserClientTest {

    @Autowired
    private UserClient userClient;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void shouldFetchAndDeserializeUserSuccessfully() {
        int userId = 1;
        String expectedUrl = "http://test-server.com/users/1";

        String jsonResponse = """
                {
                    "id": 1,
                    "name": "Leanne Graham",
                    "username": "Bret",
                    "email": "Sincere@april.biz"
                }
                """;

        this.mockServer
                .expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        User user = userClient.fetchUserById(userId);

        assertNotNull(user, "User should not be null");
        assertEquals("Leanne Graham", user.getName());
        assertEquals("Bret", user.getUsername());
        assertEquals("Sincere@april.biz", user.getEmail());

        this.mockServer.verify();
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        int userId = 999;
        String expectedUrl = "http://test-server.com/users/999";

        this.mockServer
                .expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(HttpClientErrorException.NotFound.class, () -> userClient.fetchUserById(userId));

        this.mockServer.verify();
    }
}
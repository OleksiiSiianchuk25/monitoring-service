package com.ajlekc.monitoringservice.client;

import com.ajlekc.monitoringservice.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${external.mock-api.base-url}")
    private String baseUrl;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public User fetchUserById(int id) {
        String url = baseUrl + "/" + id;
        return restTemplate.getForObject(url, User.class);
    }
}
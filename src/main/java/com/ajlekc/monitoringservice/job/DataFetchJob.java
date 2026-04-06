package com.ajlekc.monitoringservice.job;

import com.ajlekc.monitoringservice.client.UserClient;
import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.service.UserProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataFetchJob {

    @Autowired
    private UserClient userClient;

    @Autowired
    private UserProcessingService processingService;

    public void execute() {
        try {
            int randomId = (int) (Math.random() * 10) + 1;
            User user = userClient.fetchUserById(randomId);
            processingService.processAndSave(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
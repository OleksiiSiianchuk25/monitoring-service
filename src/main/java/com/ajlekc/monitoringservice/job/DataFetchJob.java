package com.ajlekc.monitoringservice.job;

import com.ajlekc.monitoringservice.client.UserClient;
import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.service.UserProcessingService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataFetchJob {

    private final UserClient userClient;
    private final UserProcessingService processingService;
    private final MeterRegistry meterRegistry;

    @Scheduled(fixedRate = 10000)
    public void execute() {
        try {
            int randomId = (int) (Math.random() * 10) + 1;
            User user = userClient.fetchUserById(randomId);
            processingService.processAndSave(user);

            meterRegistry.counter("monitoring.fetch.operations", "result", "success").increment();

        } catch (Exception e) {
            log.error("Failed to fetch data from mock service: {}", e.getMessage());

            meterRegistry.counter("monitoring.fetch.operations", "result", "failure").increment();
        }
    }
}
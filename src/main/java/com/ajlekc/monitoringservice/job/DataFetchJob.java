package com.ajlekc.monitoringservice.job;

import com.ajlekc.monitoringservice.client.UserClient;
import com.ajlekc.monitoringservice.model.ChangeType;
import com.ajlekc.monitoringservice.model.JobRun;
import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.service.JobAuditService;
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
    private final JobAuditService jobAuditService;

    @Scheduled(fixedRate = 10000)
    public void execute() {
        JobRun run = jobAuditService.start();
        JobAuditService.JobRunCounters counters = new JobAuditService.JobRunCounters();

        try {
            int randomId = (int) (Math.random() * 10) + 1;

            log.info("Requesting data from mock service for user ID: {}", randomId);

            User user = userClient.fetchUserById(randomId);
            counters.incFetched();

            ChangeType change = processingService.processAndSave(user);

            if (change == null) {
                counters.incSkipped();
            } else {
                switch (change) {
                    case NEW -> counters.incCreated();
                    case UPDATED -> counters.incUpdated();
                    case UNCHANGED -> counters.incUnchanged();
                }
            }

            meterRegistry.counter("monitoring.fetch.operations", "result", "success").increment();
            jobAuditService.finishSuccess(run, counters);

        } catch (Exception e) {
            log.error("Failed to fetch data from mock service: {}", e.getMessage());
            meterRegistry.counter("monitoring.fetch.operations", "result", "failure").increment();
            jobAuditService.finishFailure(run, e.getMessage());
        }
    }
}
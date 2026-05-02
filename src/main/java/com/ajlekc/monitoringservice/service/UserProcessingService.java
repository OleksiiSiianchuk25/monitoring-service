package com.ajlekc.monitoringservice.service;

import com.ajlekc.monitoringservice.model.ChangeType;
import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.repository.UserRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProcessingService {

    private final UserRepository userRepository;
    private final UserChangeDetector userChangeDetector;
    private final MeterRegistry meterRegistry;

    private final UserEventProducer eventProducer;

    @PostConstruct
    public void initMetrics() {
        Gauge.builder("monitoring.records.stored", userRepository, UserRepository::count)
                .description("Total number of users stored in MongoDB")
                .register(meterRegistry);
    }

    public ChangeType processAndSave(User user) {
        if (user == null) {
            log.warn("The empty user was found. Saving has been canceled.");
            return null;
        }

        ChangeType change = userChangeDetector.classifyFetchedUser(user);

        switch (change) {
            case NEW -> {
                userRepository.save(user);
                meterRegistry.counter("monitoring.records.changes", "type", "new").increment();
                log.info("User {} (External ID: {}) saved as NEW", user.getName(), user.getExternalId());

                eventProducer.publishUserChangedEvent(user, ChangeType.NEW);
            }
            case UPDATED -> {
                userRepository.save(user);
                meterRegistry.counter("monitoring.records.changes", "type", "updated").increment();
                log.info("User {} (External ID: {}) saved as UPDATED", user.getName(), user.getExternalId());

                eventProducer.publishUserChangedEvent(user, ChangeType.UPDATED);
            }
            case UNCHANGED -> {
                meterRegistry.counter("monitoring.records.changes", "type", "unchanged").increment();
                log.debug("User {} (External ID: {}) is UNCHANGED, skipping save", user.getName(), user.getExternalId());
            }
        }

        return change;
    }
}
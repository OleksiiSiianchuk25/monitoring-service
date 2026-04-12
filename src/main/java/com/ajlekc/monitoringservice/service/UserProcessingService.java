package com.ajlekc.monitoringservice.service;

import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.repository.UserRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct; // У Spring Boot 3 використовується jakarta, а не javax
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProcessingService {

    private final UserRepository userRepository;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    public void initMetrics() {
        Gauge.builder("monitoring.records.stored", userRepository, UserRepository::count)
                .description("Total number of users stored in MongoDB")
                .register(meterRegistry);
    }

    public void processAndSave(User user) {
        if (user == null) {
            log.warn("The empty user was found. Saving has been canceled.");
            return;
        }

        userRepository.save(user);
        log.info("User {} (External ID: {}) successfully saved to the database", user.getName(), user.getExternalId());
    }
}
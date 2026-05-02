package com.ajlekc.monitoringservice.service;

import com.ajlekc.monitoringservice.event.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserEventConsumer {

    @KafkaListener(topics = "user-audit-events", groupId = "monitoring-service-group")
    public void consumeUserEvent(UserEvent event) {
        log.info("[KAFKA CONSUMER] Got event: User '{}' (ID: {}) change status type to {}",
                event.name(),
                event.externalId(),
                event.changeType());
    }
}
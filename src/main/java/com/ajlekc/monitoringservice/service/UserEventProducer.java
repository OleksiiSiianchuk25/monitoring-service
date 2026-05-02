package com.ajlekc.monitoringservice.service;

import com.ajlekc.monitoringservice.model.ChangeType;
import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private static final String TOPIC = "user-audit-events";

    public void publishUserChangedEvent(User user, ChangeType changeType) {
        UserEvent event = new UserEvent(
                user.getExternalId(),
                user.getName(),
                changeType,
                Instant.now().toString()
        );

        kafkaTemplate.send(TOPIC, String.valueOf(user.getExternalId()), event);
        log.info("Message sent to Kafka topic '{}': {}", TOPIC, event);
    }
}
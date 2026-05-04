package com.ajlekc.monitoringservice.integration;

import com.ajlekc.monitoringservice.event.UserEvent;
import com.ajlekc.monitoringservice.model.ChangeType;
import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.service.UserEventProducer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "app.scheduling.enabled=false",
        "external.mock-api.base-url=http://localhost:9999/mock-users"
})
@Testcontainers
class KafkaIntegrationTest extends TestcontainersConfig {

    @Autowired
    private UserEventProducer eventProducer;

    @Test
    void shouldPublishEventToKafkaTopic() {
        User user = new User();
        user.setExternalId(1);
        user.setName("Test User");

        eventProducer.publishUserChangedEvent(user, ChangeType.NEW);

        try (KafkaConsumer<String, UserEvent> consumer = createTestConsumer()) {
            consumer.subscribe(List.of("user-audit-events"));

            ConsumerRecords<String, UserEvent> records = consumer.poll(Duration.ofSeconds(10));

            assertThat(records.count()).isEqualTo(1);

            UserEvent event = records.iterator().next().value();
            assertThat(event.externalId()).isEqualTo(1);
            assertThat(event.name()).isEqualTo("Test User");
            assertThat(event.changeType()).isEqualTo(ChangeType.NEW);
            assertThat(event.timestamp()).isNotNull();
        }
    }

    @Test
    void shouldPublishMultipleEventsWithCorrectKeys() {
        User user1 = new User();
        user1.setExternalId(1);
        user1.setName("User One");

        User user2 = new User();
        user2.setExternalId(2);
        user2.setName("User Two");

        eventProducer.publishUserChangedEvent(user1, ChangeType.NEW);
        eventProducer.publishUserChangedEvent(user2, ChangeType.UPDATED);

        try (KafkaConsumer<String, UserEvent> consumer = createTestConsumer()) {
            consumer.subscribe(List.of("user-audit-events"));

            ConsumerRecords<String, UserEvent> records = consumer.poll(Duration.ofSeconds(10));

            assertThat(records.count()).isEqualTo(2);

            var iterator = records.iterator();
            var record1 = iterator.next();
            var record2 = iterator.next();

            assertThat(record1.key()).isEqualTo("1");
            assertThat(record1.value().changeType()).isEqualTo(ChangeType.NEW);

            assertThat(record2.key()).isEqualTo("2");
            assertThat(record2.value().changeType()).isEqualTo(ChangeType.UPDATED);
        }
    }

    private KafkaConsumer<String, UserEvent> createTestConsumer() {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + System.currentTimeMillis(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class,
                JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.ajlekc.monitoringservice.event"
        );
        return new KafkaConsumer<>(props);
    }
}

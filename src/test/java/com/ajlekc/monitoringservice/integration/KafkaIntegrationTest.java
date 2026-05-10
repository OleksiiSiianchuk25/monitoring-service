package com.ajlekc.monitoringservice.integration;

import com.ajlekc.monitoringservice.event.UserEvent;
import com.ajlekc.monitoringservice.model.ChangeType;
import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.service.UserEventProducer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "app.scheduling.enabled=false",
        "external.mock-api.base-url=http://localhost:9999/mock-users"
})
class KafkaIntegrationTest extends TestcontainersConfig {

    @Autowired
    private UserEventProducer eventProducer;

    @Test
    void shouldPublishEventToKafkaTopic() {
        User user = new User();
        user.setExternalId(10101);
        user.setName("Kafka Test User");

        eventProducer.publishUserChangedEvent(user, ChangeType.NEW);

        try (KafkaConsumer<String, UserEvent> consumer = createTestConsumer()) {
            consumer.subscribe(List.of("user-audit-events"));

            List<UserEvent> relevantEvents = new ArrayList<>();
            long endTime = System.currentTimeMillis() + 10000;

            while (System.currentTimeMillis() < endTime && relevantEvents.isEmpty()) {
                ConsumerRecords<String, UserEvent> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, UserEvent> record : records) {
                    if (record.value().externalId() == 10101) {
                        relevantEvents.add(record.value());
                    }
                }
            }

            assertThat(relevantEvents).hasSize(1);

            UserEvent event = relevantEvents.get(0);
            assertThat(event.name()).isEqualTo("Kafka Test User");
            assertThat(event.changeType()).isEqualTo(ChangeType.NEW);
            assertThat(event.timestamp()).isNotNull();
        }
    }

    @Test
    void shouldPublishMultipleEventsWithCorrectKeys() {
        User user1 = new User();
        user1.setExternalId(20201);
        user1.setName("User One");

        User user2 = new User();
        user2.setExternalId(20202);
        user2.setName("User Two");

        eventProducer.publishUserChangedEvent(user1, ChangeType.NEW);
        eventProducer.publishUserChangedEvent(user2, ChangeType.UPDATED);

        try (KafkaConsumer<String, UserEvent> consumer = createTestConsumer()) {
            consumer.subscribe(List.of("user-audit-events"));

            List<ConsumerRecord<String, UserEvent>> relevantRecords = new ArrayList<>();
            long endTime = System.currentTimeMillis() + 10000;

            while (System.currentTimeMillis() < endTime && relevantRecords.size() < 2) {
                ConsumerRecords<String, UserEvent> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, UserEvent> record : records) {
                    if (record.value().externalId() == 20201 || record.value().externalId() == 20202) {
                        relevantRecords.add(record);
                    }
                }
            }

            relevantRecords.sort((r1, r2) -> Integer.compare(r1.value().externalId(), r2.value().externalId()));

            assertThat(relevantRecords).hasSize(2);

            var record1 = relevantRecords.get(0);
            var record2 = relevantRecords.get(1);

            assertThat(record1.key()).isEqualTo("20201");
            assertThat(record1.value().changeType()).isEqualTo(ChangeType.NEW);

            assertThat(record2.key()).isEqualTo("20202");
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
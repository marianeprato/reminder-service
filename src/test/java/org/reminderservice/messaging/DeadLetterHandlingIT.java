package org.reminderservice.messaging;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reminderservice.dto.ReminderRequest;
import org.reminderservice.event.TaskCreatedEvent;
import org.reminderservice.messaging.DeadLetterTopicListener.DeadLetterEntry;
import org.reminderservice.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Proves the full failure path: a record that always fails processing gets
 * retried (DefaultErrorHandler, 3 retries / 1s backoff -- see
 * KafkaConsumerConfig), then, once retries are exhausted, published to the
 * "task-created.DLT" topic by DeadLetterPublishingRecoverer and picked up by
 * DeadLetterTopicListener. ReminderService is mocked here specifically to
 * force a deterministic failure; everything else (Kafka, Postgres, the real
 * listener/error-handler/recoverer wiring) is real.
 */
@SpringBootTest
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"task-created", "task-created.DLT"})
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class DeadLetterHandlingIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @MockitoBean
    private ReminderService reminderService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private DeadLetterTopicListener deadLetterTopicListener;

    @Value("${app.kafka.topic.task-created}")
    private String topic;

    private KafkaTemplate<String, TaskCreatedEvent> producer;

    @BeforeEach
    void setUp() {
        when(reminderService.createReminder(any(ReminderRequest.class)))
                .thenThrow(new RuntimeException("simulated failure for DLT test"));

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerProps.put("spring.json.add.type.headers", false);
        producer = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));
    }

    @Test
    void recordThatAlwaysFailsEndsUpOnTheDeadLetterTopicAfterRetriesAreExhausted() {
        UUID taskId = UUID.randomUUID();
        TaskCreatedEvent event = new TaskCreatedEvent(
                taskId, "Poison event", "Always fails to process",
                LocalDate.now().plusDays(1), "LOW", Instant.now());

        producer.send(topic, taskId.toString(), event);

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            List<DeadLetterEntry> entries = deadLetterTopicListener.recentEntries();
            assertThat(entries)
                    .anyMatch(e -> taskId.toString().equals(e.key())
                            && "task-created".equals(e.originalTopic())
                            && e.exceptionMessage() != null
                            && e.exceptionMessage().contains("simulated failure for DLT test"));
        });
    }
}

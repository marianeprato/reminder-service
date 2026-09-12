package org.reminderservice.messaging;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reminderservice.event.TaskCreatedEvent;
import org.reminderservice.model.Reminder;
import org.reminderservice.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * End-to-end check of the async task-created -> reminder-created flow: a
 * real event is published onto an embedded broker and consumed by the
 * actual {@link TaskCreatedEventListener} bean, which must land a persisted
 * {@link Reminder} row.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"task-created"})
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class TaskCreatedEventConsumerIT {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ReminderRepository reminderRepository;

    @Value("${app.kafka.topic.task-created}")
    private String topic;

    private KafkaTemplate<String, TaskCreatedEvent> producer;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerProps.put("spring.json.add.type.headers", false);
        producer = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));
    }

    @Test
    void consumingTaskCreatedEventPersistsAReminder() {
        UUID taskId = UUID.randomUUID();
        TaskCreatedEvent event = new TaskCreatedEvent(
                taskId, "Write report", "Quarterly report",
                LocalDate.now().plusDays(1), "HIGH", Instant.now()
        );

        producer.send(topic, taskId.toString(), event);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<Reminder> reminders = reminderRepository.findByTaskId(taskId);
            assertThat(reminders).hasSize(1);
            assertThat(reminders.get(0).getMessage()).contains("Write report");
        });
    }
}

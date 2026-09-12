package org.reminderservice.messaging;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Listens on the dead-letter topic that {@link org.reminderservice.config.KafkaConsumerConfig}'s
 * DeadLetterPublishingRecoverer publishes to once a record exhausts its
 * retries, logs it, and keeps the last {@value #MAX_ENTRIES} in memory so
 * they can be inspected via GET /reminders/dead-letters. This is a
 * lightweight inspection aid, not a durable store or a replay mechanism --
 * entries are lost on restart, same as everything else in this service's
 * in-process state.
 */
@Slf4j
@Component
public class DeadLetterTopicListener {

    private static final int MAX_ENTRIES = 50;

    private final Deque<DeadLetterEntry> recentEntries = new ConcurrentLinkedDeque<>();

    @KafkaListener(
            topics = "${app.kafka.topic.task-created}-dlt",
            groupId = "reminder-service-dlt",
            properties = {
                    "value.deserializer=org.apache.kafka.common.serialization.StringDeserializer"
            }
    )
    public void onDeadLetter(ConsumerRecord<String, String> record) {
        String exceptionClass = header(record, "kafka_dlt-exception-fqcn");
        String exceptionMessage = header(record, "kafka_dlt-exception-message");
        String originalTopic = header(record, "kafka_dlt-original-topic");

        log.error("Dead-lettered record: originalTopic={}, key={}, exception={}: {}, value={}",
                originalTopic, record.key(), exceptionClass, exceptionMessage, record.value());

        recentEntries.addFirst(new DeadLetterEntry(
                Instant.now(), record.key(), record.value(), originalTopic, exceptionClass, exceptionMessage));
        while (recentEntries.size() > MAX_ENTRIES) {
            recentEntries.removeLast();
        }
    }

    public List<DeadLetterEntry> recentEntries() {
        return List.copyOf(recentEntries);
    }

    private static String header(ConsumerRecord<String, String> record, String key) {
        Header header = record.headers().lastHeader(key);
        return header != null ? new String(header.value(), StandardCharsets.UTF_8) : null;
    }

    public record DeadLetterEntry(
            Instant receivedAt,
            String key,
            String value,
            String originalTopic,
            String exceptionClass,
            String exceptionMessage
    ) {}
}

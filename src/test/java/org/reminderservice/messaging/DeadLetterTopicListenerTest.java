package org.reminderservice.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.Test;
import org.reminderservice.messaging.DeadLetterTopicListener.DeadLetterEntry;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeadLetterTopicListenerTest {

    private final DeadLetterTopicListener listener = new DeadLetterTopicListener();

    private ConsumerRecord<String, String> recordWithHeaders(String key, String value) {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("task-created-dlt", 0, 0L, key, value);
        record.headers().add(new RecordHeader("kafka_dlt-exception-fqcn",
                "java.lang.RuntimeException".getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("kafka_dlt-exception-message",
                "boom".getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("kafka_dlt-original-topic",
                "task-created".getBytes(StandardCharsets.UTF_8)));
        return record;
    }

    @Test
    void capturesDeadLetteredRecordDetails() {
        listener.onDeadLetter(recordWithHeaders("task-1", "{\"taskId\":\"task-1\"}"));

        List<DeadLetterEntry> entries = listener.recentEntries();

        assertThat(entries).hasSize(1);
        DeadLetterEntry entry = entries.get(0);
        assertThat(entry.key()).isEqualTo("task-1");
        assertThat(entry.value()).isEqualTo("{\"taskId\":\"task-1\"}");
        assertThat(entry.originalTopic()).isEqualTo("task-created");
        assertThat(entry.exceptionClass()).isEqualTo("java.lang.RuntimeException");
        assertThat(entry.exceptionMessage()).isEqualTo("boom");
        assertThat(entry.receivedAt()).isNotNull();
    }

    @Test
    void mostRecentEntryComesFirst() {
        listener.onDeadLetter(recordWithHeaders("first", "{}"));
        listener.onDeadLetter(recordWithHeaders("second", "{}"));

        List<DeadLetterEntry> entries = listener.recentEntries();

        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).key()).isEqualTo("second");
        assertThat(entries.get(1).key()).isEqualTo("first");
    }

    @Test
    void capsAtFiftyEntries() {
        for (int i = 0; i < 55; i++) {
            listener.onDeadLetter(recordWithHeaders("key-" + i, "{}"));
        }

        List<DeadLetterEntry> entries = listener.recentEntries();

        assertThat(entries).hasSize(50);
        assertThat(entries.get(0).key()).isEqualTo("key-54");
    }

    @Test
    void toleratesMissingHeaders() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("task-created-dlt", 0, 0L, "key", "{}");

        listener.onDeadLetter(record);

        DeadLetterEntry entry = listener.recentEntries().get(0);
        assertThat(entry.exceptionClass()).isNull();
        assertThat(entry.exceptionMessage()).isNull();
        assertThat(entry.originalTopic()).isNull();
    }
}

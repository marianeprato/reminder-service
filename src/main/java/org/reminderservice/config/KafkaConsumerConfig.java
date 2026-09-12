package org.reminderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    /**
     * After a record fails 3 times (1s apart), it's published to
     * "<original-topic>.DLT" (DeadLetterPublishingRecoverer's default naming)
     * instead of just being logged and skipped, so it isn't silently lost --
     * see DeadLetterTopicListener for how it's inspected afterwards.
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaOperations<Object, Object> kafkaOperations) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaOperations);
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
    }
}

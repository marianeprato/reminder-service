package org.reminderservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reminderservice.correlation.CorrelationIdContext;
import org.reminderservice.dto.ReminderRequest;
import org.reminderservice.event.TaskCreatedEvent;
import org.reminderservice.service.ReminderService;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskCreatedEventListener {

    private final ReminderService reminderService;

    @KafkaListener(
            topics = "${app.kafka.topic.task-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onTaskCreated(
            TaskCreatedEvent event,
            @Header(value = CorrelationIdContext.HEADER_NAME, required = false) byte[] correlationIdHeader) {
        MDC.put(CorrelationIdContext.MDC_KEY, resolveCorrelationId(correlationIdHeader));
        try {
            log.info("Received TaskCreated event for taskId: {}", event.taskId());
            ReminderRequest reminderRequest = new ReminderRequest(
                    event.taskId(),
                    "Reminder for task: " + event.taskTitle()
            );
            reminderService.createReminder(reminderRequest);
            log.info("Reminder created from TaskCreated event for taskId: {}", event.taskId());
        } finally {
            MDC.remove(CorrelationIdContext.MDC_KEY);
        }
    }

    static String resolveCorrelationId(byte[] correlationIdHeader) {
        if (correlationIdHeader == null) {
            return UUID.randomUUID().toString();
        }
        return new String(correlationIdHeader, StandardCharsets.UTF_8);
    }
}

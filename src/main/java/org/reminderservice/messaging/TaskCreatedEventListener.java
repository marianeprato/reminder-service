package org.reminderservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reminderservice.dto.ReminderRequest;
import org.reminderservice.event.TaskCreatedEvent;
import org.reminderservice.service.ReminderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskCreatedEventListener {

    private final ReminderService reminderService;

    @KafkaListener(
            topics = "${app.kafka.topic.task-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onTaskCreated(TaskCreatedEvent event) {
        log.info("Received TaskCreated event for taskId: {}", event.taskId());
        ReminderRequest reminderRequest = new ReminderRequest(
                event.taskId(),
                "Reminder for task: " + event.taskTitle()
        );
        reminderService.createReminder(reminderRequest);
        log.info("Reminder created from TaskCreated event for taskId: {}", event.taskId());
    }
}

package org.reminderservice.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Wire contract consumed from the "task-created" Kafka topic. This is a
 * deliberate duplicate of task-service's event of the same name: the two
 * services live in separate repos and only agree on the JSON shape, never
 * on a shared Java type.
 */
public record TaskCreatedEvent(
        UUID taskId,
        String taskTitle,
        String taskDescription,
        LocalDate taskDueDate,
        String priority,
        Instant occurredAt
) {}

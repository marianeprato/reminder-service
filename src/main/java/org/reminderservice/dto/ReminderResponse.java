package org.reminderservice.dto;

import java.util.UUID;

public record ReminderResponse(Long reminderId, UUID taskId, String message) {}

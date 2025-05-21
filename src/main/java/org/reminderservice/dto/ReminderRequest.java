package org.reminderservice.dto;

import java.util.UUID;

public record ReminderRequest(UUID taskId, String message) {}


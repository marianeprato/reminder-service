package org.reminderservice.service;

import lombok.RequiredArgsConstructor;
import org.reminderservice.dto.ReminderRequest;
import org.reminderservice.dto.ReminderResponse;
import org.reminderservice.model.Reminder;
import org.reminderservice.repository.ReminderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;

    public ReminderResponse createReminder(ReminderRequest request) {
        Reminder reminder = Reminder.builder()
                .taskId(request.taskId())
                .message(request.message())
                .build();

        Reminder savedReminder = reminderRepository.save(reminder);

        return new ReminderResponse(
                savedReminder.getReminderId(),
                savedReminder.getTaskId(),
                savedReminder.getMessage()
        );
    }

    public List<ReminderResponse> getRemindersForTask(UUID taskId) {
        return reminderRepository.findByTaskId(taskId).stream()
                .map(r -> new ReminderResponse(r.getReminderId(), r.getTaskId(), r.getMessage()))
                .collect(Collectors.toList());
    }

    public ReminderResponse getReminderById(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found for id: " + reminderId));

        return new ReminderResponse(reminder.getReminderId(), reminder.getTaskId(), reminder.getMessage());
    }

    public List<Reminder> getAllReminders() {
        return reminderRepository.findAll();
    }
}

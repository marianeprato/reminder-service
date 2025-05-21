package org.reminderservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reminderservice.dto.ReminderRequest;
import org.reminderservice.dto.ReminderResponse;
import org.reminderservice.model.Reminder;
import org.reminderservice.service.ReminderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping
    public ResponseEntity<ReminderResponse> createReminder(@RequestBody ReminderRequest request) {
        log.info("Received reminder request for taskId: {}, message: {}", request.taskId(), request.message());
        ReminderResponse response = reminderService.createReminder(request);
        log.info("Reminder created successfully for taskId: {}", request.taskId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<List<ReminderResponse>> getReminderByTaskId(@PathVariable("taskId") UUID taskId) {
        List<ReminderResponse> reminders = reminderService.getRemindersForTask(taskId);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/id/{reminderId}")
    public ResponseEntity<ReminderResponse> getReminderById(@PathVariable("reminderId") long reminderId) {
        ReminderResponse reminder = reminderService.getReminderById(reminderId);
        return ResponseEntity.ok(reminder);
    }


    @GetMapping
    public ResponseEntity<List<Reminder>> getAllReminders() {
        List<Reminder> reminders = reminderService.getAllReminders();
        return ResponseEntity.ok(reminders);
    }

}

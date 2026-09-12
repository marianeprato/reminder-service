package org.reminderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reminderservice.dto.ReminderRequest;
import org.reminderservice.dto.ReminderResponse;
import org.reminderservice.model.Reminder;
import org.reminderservice.service.ReminderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/reminders")
@RequiredArgsConstructor
@Tag(name = "Reminders", description = "Create and query reminders. Most reminders are created " +
        "asynchronously from task-service's TaskCreated Kafka event; this REST API also works standalone.")
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping
    @Operation(summary = "Create a reminder directly via REST")
    public ResponseEntity<ReminderResponse> createReminder(@RequestBody ReminderRequest request) {
        log.info("Received reminder request for taskId: {}, message: {}", request.taskId(), request.message());
        ReminderResponse response = reminderService.createReminder(request);
        log.info("Reminder created successfully for taskId: {}", request.taskId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get all reminders for a task")
    public ResponseEntity<List<ReminderResponse>> getReminderByTaskId(@PathVariable("taskId") UUID taskId) {
        List<ReminderResponse> reminders = reminderService.getRemindersForTask(taskId);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/id/{reminderId}")
    @Operation(summary = "Get a single reminder by its id")
    public ResponseEntity<ReminderResponse> getReminderById(@PathVariable("reminderId") long reminderId) {
        ReminderResponse reminder = reminderService.getReminderById(reminderId);
        return ResponseEntity.ok(reminder);
    }


    @GetMapping
    @Operation(summary = "List all reminders")
    public ResponseEntity<List<Reminder>> getAllReminders() {
        List<Reminder> reminders = reminderService.getAllReminders();
        return ResponseEntity.ok(reminders);
    }
}

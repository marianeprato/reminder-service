package org.reminderservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reminderservice.dto.ReminderRequest;
import org.reminderservice.dto.ReminderResponse;
import org.reminderservice.model.Reminder;
import org.reminderservice.service.ReminderService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReminderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReminderService reminderService;

    @InjectMocks
    private ReminderController reminderController;

    private UUID taskId;
    private long reminderId;
    private ReminderRequest reminderRequest;
    private Reminder reminder;
    private ReminderResponse reminderResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reminderController).build();

        taskId = UUID.randomUUID();
        reminderId = 1L;

        reminderRequest = new ReminderRequest(taskId, "Test reminder");
        reminder = Reminder.builder()
                .reminderId(reminderId)
                .taskId(taskId)
                .message("Test reminder")
                .build();

        reminderResponse = new ReminderResponse(reminderId, taskId, "Test reminder");
    }

    @Test
    void createReminder() throws Exception {
        when(reminderService.createReminder(any(ReminderRequest.class)))
                .thenReturn(reminderResponse);

        String json = String.format(
                "{\"taskId\":\"%s\",\"message\":\"%s\"}",
                taskId, "Test reminder"
        );

        mockMvc.perform(post("/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reminderId").value(reminderId))
                .andExpect(jsonPath("$.taskId").value(taskId.toString()))
                .andExpect(jsonPath("$.message").value("Test reminder"));

        verify(reminderService, times(1)).createReminder(any(ReminderRequest.class));
    }

    @Test
    void getRemindersForTask() throws Exception {
        List<ReminderResponse> list = List.of(reminderResponse);
        when(reminderService.getRemindersForTask(taskId)).thenReturn(list);

        mockMvc.perform(get("/reminders/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reminderId").value(reminderId))
                .andExpect(jsonPath("$[0].taskId").value(taskId.toString()))
                .andExpect(jsonPath("$[0].message").value("Test reminder"));

        verify(reminderService, times(1)).getRemindersForTask(taskId);
    }

    @Test
    void getReminderById() throws Exception {
        when(reminderService.getReminderById(reminderId)).thenReturn(reminderResponse);

        mockMvc.perform(get("/reminders/id/{reminderId}", reminderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reminderId").value(reminderId))
                .andExpect(jsonPath("$.taskId").value(taskId.toString()))
                .andExpect(jsonPath("$.message").value("Test reminder"));

        verify(reminderService, times(1)).getReminderById(reminderId);
    }

    @Test
    void getAllReminders() throws Exception {
        List<Reminder> all = List.of(reminder);
        when(reminderService.getAllReminders()).thenReturn(all);

        mockMvc.perform(get("/reminders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reminderId").value(reminderId))
                .andExpect(jsonPath("$[0].taskId").value(taskId.toString()))
                .andExpect(jsonPath("$[0].message").value("Test reminder"));

        verify(reminderService, times(1)).getAllReminders();
    }
}

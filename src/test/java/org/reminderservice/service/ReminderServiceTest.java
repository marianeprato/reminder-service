package org.reminderservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reminderservice.dto.ReminderRequest;
import org.reminderservice.dto.ReminderResponse;
import org.reminderservice.model.Reminder;
import org.reminderservice.repository.ReminderRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @InjectMocks
    private ReminderService reminderService;

    private UUID taskId;
    private ReminderRequest reminderRequest;
    private Reminder reminder;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        reminderRequest = new ReminderRequest(taskId, "Test reminder");
        reminder = Reminder.builder()
                .reminderId(1L)
                .taskId(taskId)
                .message(reminderRequest.message())
                .build();
    }

    @Test
    void shouldCreateReminder() {
        when(reminderRepository.save(any(Reminder.class))).thenReturn(reminder);

        ReminderResponse response = reminderService.createReminder(reminderRequest);

        assertThat(response).isNotNull();
        assertThat(response.reminderId()).isEqualTo(reminder.getReminderId());
        assertThat(response.taskId()).isEqualTo(reminder.getTaskId());
        assertThat(response.message()).isEqualTo(reminder.getMessage());

        verify(reminderRepository, times(1)).save(any(Reminder.class));
    }

    @Test
    void shouldGetRemindersForTask() {
        when(reminderRepository.findByTaskId(taskId)).thenReturn(List.of(reminder));

        List<ReminderResponse> responses = reminderService.getRemindersForTask(taskId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).taskId()).isEqualTo(taskId);
        assertThat(responses.get(0).message()).isEqualTo(reminder.getMessage());

        verify(reminderRepository, times(1)).findByTaskId(taskId);
    }

    @Test
    void shouldGetReminderById_whenExists() {
        when(reminderRepository.findById(1L)).thenReturn(Optional.of(reminder));

        ReminderResponse response = reminderService.getReminderById(1L);

        assertThat(response).isNotNull();
        assertThat(response.reminderId()).isEqualTo(reminder.getReminderId());
        assertThat(response.taskId()).isEqualTo(reminder.getTaskId());
        assertThat(response.message()).isEqualTo(reminder.getMessage());

        verify(reminderRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrow_whenReminderNotFound() {
        when(reminderRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reminderService.getReminderById(2L));

        assertThat(ex.getMessage()).contains("Reminder not found for id: 2");
        verify(reminderRepository, times(1)).findById(2L);
    }

    @Test
    void shouldGetAllReminders() {
        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        List<Reminder> all = reminderService.getAllReminders();

        assertThat(all).hasSize(1);
        assertThat(all.get(0).getReminderId()).isEqualTo(1L);

        verify(reminderRepository, times(1)).findAll();
    }
}

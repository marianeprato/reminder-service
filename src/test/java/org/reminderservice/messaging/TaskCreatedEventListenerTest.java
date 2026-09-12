package org.reminderservice.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reminderservice.dto.ReminderRequest;
import org.reminderservice.event.TaskCreatedEvent;
import org.reminderservice.service.ReminderService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskCreatedEventListenerTest {

    @Mock
    private ReminderService reminderService;

    @InjectMocks
    private TaskCreatedEventListener listener;

    @Test
    void createsReminderFromTaskCreatedEvent() {
        UUID taskId = UUID.randomUUID();
        TaskCreatedEvent event = new TaskCreatedEvent(
                taskId, "Write report", "Quarterly report",
                LocalDate.now().plusDays(1), "HIGH", Instant.now()
        );

        listener.onTaskCreated(event);

        ArgumentCaptor<ReminderRequest> captor = ArgumentCaptor.forClass(ReminderRequest.class);
        verify(reminderService, times(1)).createReminder(captor.capture());

        assertThat(captor.getValue().taskId()).isEqualTo(taskId);
        assertThat(captor.getValue().message()).contains("Write report");
    }
}

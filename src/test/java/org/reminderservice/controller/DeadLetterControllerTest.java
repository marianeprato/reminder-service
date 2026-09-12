package org.reminderservice.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reminderservice.messaging.DeadLetterTopicListener;
import org.reminderservice.messaging.DeadLetterTopicListener.DeadLetterEntry;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeadLetterControllerTest {

    @Mock
    private DeadLetterTopicListener deadLetterTopicListener;

    @InjectMocks
    private DeadLetterController controller;

    @Test
    void returnsRecentDeadLettersFromTheListener() {
        List<DeadLetterEntry> entries = List.of(
                new DeadLetterEntry(Instant.now(), "key-1", "{}", "task-created",
                        "java.lang.RuntimeException", "boom")
        );
        when(deadLetterTopicListener.recentEntries()).thenReturn(entries);

        ResponseEntity<List<DeadLetterEntry>> response = controller.recentDeadLetters();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(entries);
        verify(deadLetterTopicListener, times(1)).recentEntries();
    }
}

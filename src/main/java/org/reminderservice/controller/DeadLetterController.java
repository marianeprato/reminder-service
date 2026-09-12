package org.reminderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.reminderservice.messaging.DeadLetterTopicListener;
import org.reminderservice.messaging.DeadLetterTopicListener.DeadLetterEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dead-letters")
@RequiredArgsConstructor
@Tag(name = "Dead letters", description = "Inspect Kafka records that exhausted their retries")
public class DeadLetterController {

    private final DeadLetterTopicListener deadLetterTopicListener;

    @GetMapping
    @Operation(summary = "List the most recent dead-lettered records (in-memory, last 50, lost on restart)")
    public ResponseEntity<List<DeadLetterEntry>> recentDeadLetters() {
        return ResponseEntity.ok(deadLetterTopicListener.recentEntries());
    }
}

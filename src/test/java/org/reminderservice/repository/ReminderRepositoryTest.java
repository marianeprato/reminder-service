package org.reminderservice.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.reminderservice.model.Reminder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DataJpaTest
class ReminderRepositoryTest {

    @Autowired
    private ReminderRepository reminderRepository;

    private UUID taskId1;
    private UUID taskId2;

    @BeforeEach
    void setUp() {
        reminderRepository.deleteAll();

        taskId1 = UUID.randomUUID();
        taskId2 = UUID.randomUUID();

        Reminder r1 = Reminder.builder()
                .taskId(taskId1)
                .message("Alpha reminder")
                .build();
        Reminder r2 = Reminder.builder()
                .taskId(taskId1)
                .message("Beta reminder")
                .build();
        Reminder r3 = Reminder.builder()
                .taskId(taskId2)
                .message("Gamma reminder")
                .build();

        reminderRepository.save(r1);
        reminderRepository.save(r2);
        reminderRepository.save(r3);
    }

    @Test
    void findByTaskId_returnsMatchingReminders() {
        List<Reminder> found = reminderRepository.findByTaskId(taskId1);
        assertEquals(2, found.size(), "Should return two reminders for taskId1");
        assertTrue(
                found.stream().anyMatch(r -> "Alpha reminder".equals(r.getMessage())),
                "Should contain 'Alpha reminder'"
        );
        assertTrue(
                found.stream().anyMatch(r -> "Beta reminder".equals(r.getMessage())),
                "Should contain 'Beta reminder'"
        );
    }

    @Test
    void findByTaskId_returnsEmptyList_whenNoReminders() {
        List<Reminder> found = reminderRepository.findByTaskId(UUID.randomUUID());
        assertNotNull(found, "Result should not be null");
        assertTrue(found.isEmpty(), "Should return empty list when no reminders for given taskId");
    }

    @Test
    void save_assignsId_and_findByIdWorks() {
        Reminder newReminder = Reminder.builder()
                .taskId(taskId2)
                .message("Delta reminder")
                .build();
        Reminder saved = reminderRepository.save(newReminder);

        assertNotNull(saved.getReminderId(), "Saved reminder should have an ID");

        reminderRepository.findById(saved.getReminderId())
                .ifPresentOrElse(
                        r -> assertEquals("Delta reminder", r.getMessage(), "Fetched reminder should match saved one"),
                        () -> fail("Reminder should be found by ID")
                );
    }
}

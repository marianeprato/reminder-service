package org.reminderservice.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.reminderservice.model.Reminder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @DataJpaTest normally swaps in an embedded database automatically; since
 * this project no longer has one on the classpath (Postgres only), that
 * auto-replacement is disabled and a real Postgres from Testcontainers is
 * wired in instead. Flyway (included in the @DataJpaTest slice) migrates it
 * before the tests run, same as it would in production.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ReminderRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

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

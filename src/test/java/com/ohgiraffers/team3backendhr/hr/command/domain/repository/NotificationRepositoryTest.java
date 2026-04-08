package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notification.Notification;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notification.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private Notification notification;
    private Long notificationId;

    @BeforeEach
    void setUp() {
        notificationId = idGenerator.generate();
        notification = Notification.builder()
                .notificationId(notificationId)
                .notificationType(NotificationType.RESULTS)
                .notificationTitle("평가 결과 알림")
                .notificationContent("2026년 1차 평가 결과가 공개되었습니다.")
                .notificationSentAt(LocalDateTime.of(2026, 4, 1, 9, 0))
                .build();
    }

    @Test
    @DisplayName("Save notification success: notification is persisted")
    void save_success() {
        Notification saved = notificationRepository.save(notification);

        assertNotNull(saved);
        assertEquals(notificationId, saved.getNotificationId());
        assertEquals(NotificationType.RESULTS, saved.getNotificationType());
    }

    @Test
    @DisplayName("Find notification by id success: return persisted notification")
    void findById_success() {
        notificationRepository.save(notification);

        Optional<Notification> result = notificationRepository.findById(notificationId);

        assertTrue(result.isPresent());
        assertEquals("평가 결과 알림", result.get().getNotificationTitle());
    }

    @Test
    @DisplayName("Find notification by id failure: return empty when notification does not exist")
    void findById_whenNotFound_thenEmpty() {
        Optional<Notification> result = notificationRepository.findById(idGenerator.generate());

        assertFalse(result.isPresent());
    }
}

package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Notification;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NotificationRecipient;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationRecipientRepositoryTest {

    @Autowired
    private NotificationRecipientRepository notificationRecipientRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private NotificationRecipient recipient;
    private Long recipientId;
    private Long notificationId;
    private final Long employeeId = 1001L;

    @BeforeEach
    void setUp() {
        notificationId = idGenerator.generate();
        notificationRepository.save(Notification.builder()
                .notificationId(notificationId)
                .notificationType(NotificationType.RESULTS)
                .notificationTitle("평가 결과 알림")
                .notificationContent("평가 결과가 공개되었습니다.")
                .notificationSentAt(LocalDateTime.of(2026, 4, 1, 9, 0))
                .build());

        recipientId = idGenerator.generate();
        recipient = NotificationRecipient.builder()
                .notificationRecipientId(recipientId)
                .notificationId(notificationId)
                .employeeId(employeeId)
                .build();
    }

    @Test
    @DisplayName("Save notification recipient success: recipient is persisted")
    void save_success() {
        NotificationRecipient saved = notificationRecipientRepository.save(recipient);

        assertNotNull(saved);
        assertEquals(recipientId, saved.getNotificationRecipientId());
        assertFalse(saved.getNotificationIsRead());
        assertFalse(saved.getNotificationIsHide());
    }

    @Test
    @DisplayName("Find notification recipient by id success: return persisted recipient")
    void findById_success() {
        notificationRecipientRepository.save(recipient);

        Optional<NotificationRecipient> result = notificationRecipientRepository.findById(recipientId);

        assertTrue(result.isPresent());
        assertEquals(employeeId, result.get().getEmployeeId());
    }

    @Test
    @DisplayName("Find notification recipient by id failure: return empty when recipient does not exist")
    void findById_whenNotFound_thenEmpty() {
        Optional<NotificationRecipient> result = notificationRecipientRepository.findById(idGenerator.generate());

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Find recipients by employee id: return all notifications of the employee")
    void findByEmployeeId_success() {
        notificationRecipientRepository.save(recipient);

        List<NotificationRecipient> result = notificationRecipientRepository.findByEmployeeId(employeeId);

        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(r -> r.getEmployeeId().equals(employeeId)));
    }

    @Test
    @DisplayName("Find non-hidden recipients by employee id: return only visible notifications")
    void findByEmployeeIdAndNotificationIsHideFalse_success() {
        notificationRecipientRepository.save(recipient);

        Long hiddenId = idGenerator.generate();
        Long hiddenNotifId = idGenerator.generate();
        notificationRepository.save(Notification.builder()
                .notificationId(hiddenNotifId)
                .notificationType(NotificationType.PROMOTION)
                .notificationTitle("숨김 알림")
                .notificationContent("숨김 처리된 알림입니다.")
                .notificationSentAt(LocalDateTime.of(2026, 4, 2, 10, 0))
                .build());
        NotificationRecipient hidden = NotificationRecipient.builder()
                .notificationRecipientId(hiddenId)
                .notificationId(hiddenNotifId)
                .employeeId(employeeId)
                .build();
        hidden.hide();
        notificationRecipientRepository.save(hidden);

        List<NotificationRecipient> result = notificationRecipientRepository
                .findByEmployeeIdAndNotificationIsHideFalse(employeeId);

        assertTrue(result.stream().noneMatch(NotificationRecipient::getNotificationIsHide));
    }

    @Test
    @DisplayName("Count unread and non-hidden notifications: return correct unread count")
    void countUnreadAndNotHidden_success() {
        notificationRecipientRepository.save(recipient);

        long count = notificationRecipientRepository
                .countByEmployeeIdAndNotificationIsReadFalseAndNotificationIsHideFalse(employeeId);

        assertTrue(count >= 1);
    }
}

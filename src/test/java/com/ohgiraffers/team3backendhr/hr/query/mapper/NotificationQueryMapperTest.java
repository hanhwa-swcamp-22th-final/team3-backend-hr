package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Notification;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NotificationRecipient;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NotificationType;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NotificationRecipientRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NotificationRepository;
import com.ohgiraffers.team3backendhr.hr.query.dto.NotificationResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NotificationSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Sql(statements = "SET FOREIGN_KEY_CHECKS = 0", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class NotificationQueryMapperTest {

    @Autowired
    private NotificationQueryMapper mapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationRecipientRepository notificationRecipientRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();
    private final Long employeeId = 200L;

    private Long notificationId1;
    private Long notificationId2;
    private Long recipientId1;
    private Long recipientId2;
    private Long recipientId3;

    @BeforeEach
    void setUp() {
        notificationId1 = idGenerator.generate();
        notificationRepository.saveAndFlush(Notification.builder()
                .notificationId(notificationId1)
                .notificationType(NotificationType.RESULTS)
                .notificationTitle("평가 결과 알림")
                .notificationContent("평가 결과가 등록되었습니다.")
                .notificationSentAt(LocalDateTime.of(2026, 4, 1, 9, 0))
                .build());

        notificationId2 = idGenerator.generate();
        notificationRepository.saveAndFlush(Notification.builder()
                .notificationId(notificationId2)
                .notificationType(NotificationType.PROMOTION)
                .notificationTitle("승급 알림")
                .notificationContent("승급 심사 대상입니다.")
                .notificationSentAt(LocalDateTime.of(2026, 4, 2, 9, 0))
                .build());

        // 읽지 않은 수신 레코드
        recipientId1 = idGenerator.generate();
        notificationRecipientRepository.saveAndFlush(NotificationRecipient.builder()
                .notificationRecipientId(recipientId1)
                .notificationId(notificationId1)
                .employeeId(employeeId)
                .build());

        // 읽은 수신 레코드
        recipientId2 = idGenerator.generate();
        NotificationRecipient read = NotificationRecipient.builder()
                .notificationRecipientId(recipientId2)
                .notificationId(notificationId2)
                .employeeId(employeeId)
                .build();
        read.acknowledge();
        notificationRecipientRepository.saveAndFlush(read);

        // 숨김 처리된 수신 레코드 (다른 notification)
        Long notificationId3 = idGenerator.generate();
        notificationRepository.saveAndFlush(Notification.builder()
                .notificationId(notificationId3)
                .notificationType(NotificationType.ARRANGEMENT)
                .notificationTitle("숨긴 알림")
                .notificationContent("숨긴 내용")
                .notificationSentAt(LocalDateTime.of(2026, 4, 3, 9, 0))
                .build());

        recipientId3 = idGenerator.generate();
        NotificationRecipient hidden = NotificationRecipient.builder()
                .notificationRecipientId(recipientId3)
                .notificationId(notificationId3)
                .employeeId(employeeId)
                .build();
        hidden.hide();
        notificationRecipientRepository.saveAndFlush(hidden);
    }

    @Test
    @DisplayName("숨김 처리되지 않은 알림 목록을 최신순으로 조회한다")
    void findVisibleByEmployeeId_success() {
        List<NotificationResponse> result = mapper.findVisibleByEmployeeId(employeeId);

        assertThat(result).hasSize(2);
        // 최신순 정렬 — notificationId2(4월2일)가 먼저
        assertThat(result.get(0).getNotificationId()).isEqualTo(notificationId2);
        assertThat(result.get(1).getNotificationId()).isEqualTo(notificationId1);
    }

    @Test
    @DisplayName("숨김 처리된 알림은 목록에 포함되지 않는다")
    void findVisibleByEmployeeId_excludesHidden() {
        List<NotificationResponse> result = mapper.findVisibleByEmployeeId(employeeId);

        assertThat(result).noneMatch(r -> r.getNotificationRecipientId().equals(recipientId3));
    }

    @Test
    @DisplayName("알림 요약 — 전체 건수와 미읽음 건수를 반환한다")
    void findSummaryByEmployeeId_success() {
        NotificationSummaryResponse summary = mapper.findSummaryByEmployeeId(employeeId);

        assertThat(summary.getTotalCount()).isEqualTo(2);   // 숨김 제외
        assertThat(summary.getUnreadCount()).isEqualTo(1);  // recipientId1만 미읽음
    }
}

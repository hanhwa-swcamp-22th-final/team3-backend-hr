package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Notification;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NotificationRecipient;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NotificationType;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NotificationRecipientRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(statements = "SET FOREIGN_KEY_CHECKS = 0", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class NotificationCommandIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationRecipientRepository notificationRecipientRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();
    private static final Long WORKER_EMPLOYEE_ID = 200L;

    private Long notificationId;
    private Long recipientId;

    private EmployeeUserDetails workerUser() {
        return new EmployeeUserDetails(WORKER_EMPLOYEE_ID, "EMP-WORKER", "password",
                List.of(new SimpleGrantedAuthority("ROLE_WORKER")));
    }

    @BeforeEach
    void setUp() {
        EmployeeUserDetails user = workerUser();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );

        notificationId = idGenerator.generate();
        notificationRepository.save(Notification.builder()
                .notificationId(notificationId)
                .notificationType(NotificationType.PROMOTION)
                .notificationTitle("미션 달성 알림")
                .notificationContent("미션을 달성하셨습니다.")
                .notificationSentAt(LocalDateTime.now())
                .build());

        recipientId = idGenerator.generate();
        notificationRecipientRepository.save(NotificationRecipient.builder()
                .notificationRecipientId(recipientId)
                .notificationId(notificationId)
                .employeeId(WORKER_EMPLOYEE_ID)
                .build());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("알림 읽음 처리 전체 흐름 — 200 반환 및 DB에 읽음 상태가 반영된다")
    void acknowledge_fullFlow() throws Exception {
        mockMvc.perform(post("/api/v1/hr/notifications/" + notificationId + "/ack")
                        .with(csrf())
                        .with(user(workerUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        NotificationRecipient recipient = notificationRecipientRepository.findById(recipientId).orElseThrow();
        assertThat(recipient.getNotificationIsRead()).isTrue();
        assertThat(recipient.getNotificationReadAt()).isNotNull();
    }

    @Test
    @DisplayName("알림 숨김 처리 전체 흐름 — 200 반환 및 DB에 숨김 상태가 반영된다")
    void hide_fullFlow() throws Exception {
        mockMvc.perform(patch("/api/v1/hr/notifications/" + notificationId + "/hide")
                        .with(csrf())
                        .with(user(workerUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        NotificationRecipient recipient = notificationRecipientRepository.findById(recipientId).orElseThrow();
        assertThat(recipient.getNotificationIsHide()).isTrue();
    }
}

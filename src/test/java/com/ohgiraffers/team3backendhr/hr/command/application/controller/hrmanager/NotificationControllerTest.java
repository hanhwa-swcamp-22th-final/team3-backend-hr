package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.hr.command.application.service.NotificationCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationCommandService notificationCommandService;

    private EmployeeUserDetails workerUser() {
        return new EmployeeUserDetails(200L, "EMP-WORKER", "password",
                List.of(new SimpleGrantedAuthority("WORKER")));
    }

    @Test
    @DisplayName("알림 숨김 성공 — 200 OK (PATCH /{notificationId}/hide)")
    void hide_success() throws Exception {
        mockMvc.perform(patch("/api/v1/hr/notifications/1/hide")
                        .with(csrf())
                        .with(user(workerUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationCommandService).hide(1L, 200L);
    }

    @Test
    @DisplayName("알림 숨김 — 존재하지 않으면 404")
    void hide_notFound() throws Exception {
        doThrow(new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND))
                .when(notificationCommandService).hide(any(), any());

        mockMvc.perform(patch("/api/v1/hr/notifications/9999/hide")
                        .with(csrf())
                        .with(user(workerUser())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("알림 읽음 처리 성공 — 200 OK (POST /{notificationId}/ack)")
    void acknowledge_success() throws Exception {
        mockMvc.perform(post("/api/v1/hr/notifications/1/ack")
                        .with(csrf())
                        .with(user(workerUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationCommandService).acknowledge(1L, 200L);
    }

    @Test
    @DisplayName("알림 읽음 처리 — 존재하지 않으면 404")
    void acknowledge_notFound() throws Exception {
        doThrow(new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND))
                .when(notificationCommandService).acknowledge(any(), any());

        mockMvc.perform(post("/api/v1/hr/notifications/9999/ack")
                        .with(csrf())
                        .with(user(workerUser())))
                .andExpect(status().isNotFound());
    }
}

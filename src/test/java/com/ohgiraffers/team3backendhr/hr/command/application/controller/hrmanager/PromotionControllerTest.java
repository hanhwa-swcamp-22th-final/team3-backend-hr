package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.hr.command.application.service.PromotionCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PromotionController.class)
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PromotionCommandService promotionCommandService;

    private EmployeeUserDetails hrmUser() {
        return new EmployeeUserDetails(99L, "EMP-HRM", "password",
                List.of(new SimpleGrantedAuthority("HRM")));
    }

    @Test
    @DisplayName("승급 확정 성공 — 200 OK (POST /{candidateId}/confirm)")
    void confirmPromotion_success() throws Exception {
        mockMvc.perform(post("/api/v1/hr/promotions/1/confirm")
                        .with(csrf())
                        .with(user(hrmUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(promotionCommandService).confirmPromotion(1L);
    }

    @Test
    @DisplayName("승급 확정 — 존재하지 않으면 404")
    void confirmPromotion_notFound() throws Exception {
        doThrow(new IllegalArgumentException("승급 이력을 찾을 수 없습니다."))
                .when(promotionCommandService).confirmPromotion(any());

        mockMvc.perform(post("/api/v1/hr/promotions/9999/confirm")
                        .with(csrf())
                        .with(user(hrmUser())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("승급 확정 — 이미 처리된 상태면 400")
    void confirmPromotion_alreadyProcessed() throws Exception {
        doThrow(new IllegalStateException("심사 중인 승급 후보만 확정할 수 있습니다."))
                .when(promotionCommandService).confirmPromotion(any());

        mockMvc.perform(post("/api/v1/hr/promotions/1/confirm")
                        .with(csrf())
                        .with(user(hrmUser())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("승급 보류 성공 — 200 OK (POST /{candidateId}/hold)")
    void suspendPromotion_success() throws Exception {
        mockMvc.perform(post("/api/v1/hr/promotions/1/hold")
                        .with(csrf())
                        .with(user(hrmUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(promotionCommandService).suspendPromotion(1L);
    }

    @Test
    @DisplayName("승급 보류 — 이미 처리된 상태면 400")
    void suspendPromotion_alreadyProcessed() throws Exception {
        doThrow(new IllegalStateException("심사 중인 승급 후보만 보류할 수 있습니다."))
                .when(promotionCommandService).suspendPromotion(any());

        mockMvc.perform(post("/api/v1/hr/promotions/1/hold")
                        .with(csrf())
                        .with(user(hrmUser())))
                .andExpect(status().isBadRequest());
    }
}

package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.promotion.PromotionStatusUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.PromotionCommandService;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionStatus;
import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PromotionController.class)
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PromotionCommandService promotionCommandService;

    private EmployeeUserDetails hrmUser() {
        return new EmployeeUserDetails(99L, "EMP-HRM", "password",
                List.of(new SimpleGrantedAuthority("HRM")));
    }

    @Test
    @DisplayName("승급 확정 성공 — 200 OK (PATCH /{candidateId})")
    void confirmPromotion_success() throws Exception {
        PromotionStatusUpdateRequest request = new PromotionStatusUpdateRequest(
                PromotionStatus.CONFIRMATION_OF_PROMOTION);

        mockMvc.perform(patch("/api/v1/hr/promotions/1")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(promotionCommandService).confirmPromotion(1L, 99L);
    }

    @Test
    @DisplayName("승급 확정 — 존재하지 않으면 404")
    void confirmPromotion_notFound() throws Exception {
        doThrow(new BusinessException(ErrorCode.PROMOTION_NOT_FOUND))
                .when(promotionCommandService).confirmPromotion(any(), any());

        PromotionStatusUpdateRequest request = new PromotionStatusUpdateRequest(
                PromotionStatus.CONFIRMATION_OF_PROMOTION);

        mockMvc.perform(patch("/api/v1/hr/promotions/9999")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("승급 확정 — 이미 처리된 상태면 400")
    void confirmPromotion_alreadyProcessed() throws Exception {
        doThrow(new BusinessException(ErrorCode.PROMOTION_NOT_UNDER_REVIEW))
                .when(promotionCommandService).confirmPromotion(any(), any());

        PromotionStatusUpdateRequest request = new PromotionStatusUpdateRequest(
                PromotionStatus.CONFIRMATION_OF_PROMOTION);

        mockMvc.perform(patch("/api/v1/hr/promotions/1")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("승급 보류 성공 — 200 OK (PATCH /{candidateId})")
    void suspendPromotion_success() throws Exception {
        PromotionStatusUpdateRequest request = new PromotionStatusUpdateRequest(
                PromotionStatus.SUSPENSION);

        mockMvc.perform(patch("/api/v1/hr/promotions/1")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(promotionCommandService).suspendPromotion(1L, 99L);
    }

    @Test
    @DisplayName("승급 보류 — 이미 처리된 상태면 400")
    void suspendPromotion_alreadyProcessed() throws Exception {
        doThrow(new BusinessException(ErrorCode.PROMOTION_NOT_UNDER_REVIEW))
                .when(promotionCommandService).suspendPromotion(any(), any());

        PromotionStatusUpdateRequest request = new PromotionStatusUpdateRequest(
                PromotionStatus.SUSPENSION);

        mockMvc.perform(patch("/api/v1/hr/promotions/1")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionHistory;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.TierConfig;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.PromotionHistoryRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.TierConfigRepository;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.promotion.PromotionStatusUpdateRequest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(statements = "SET FOREIGN_KEY_CHECKS = 0", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PromotionCommandIntegrationTest {

    @MockitoBean
    private AdminClient adminClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TierConfigRepository tierConfigRepository;

    @Autowired
    private PromotionHistoryRepository promotionHistoryRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();
    private static final Long HRM_EMPLOYEE_ID = 99L;

    private Long tierPromotionId;
    private Long currentTierConfigId;
    private Long targetTierConfigId;

    private EmployeeUserDetails hrmUser() {
        return new EmployeeUserDetails(HRM_EMPLOYEE_ID, "EMP-HRM", "password",
                List.of(new SimpleGrantedAuthority("HRM")));
    }

    @BeforeEach
    void setUp() {
        EmployeeUserDetails user = hrmUser();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );

        currentTierConfigId = idGenerator.generate();
        targetTierConfigId = idGenerator.generate();

        tierConfigRepository.save(TierConfig.builder()
                .tierConfigId(currentTierConfigId)
                .tierConfigTier(Grade.B)
                .tierConfigPromotionPoint(500)
                .build());

        tierConfigRepository.save(TierConfig.builder()
                .tierConfigId(targetTierConfigId)
                .tierConfigTier(Grade.A)
                .tierConfigPromotionPoint(1000)
                .build());

        tierPromotionId = idGenerator.generate();
        promotionHistoryRepository.save(PromotionHistory.builder()
                .tierPromotionId(tierPromotionId)
                .employeeId(300L)
                .currentTierConfigId(currentTierConfigId)
                .targetTierConfigId(targetTierConfigId)
                .build());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("승급 확정 전체 흐름 — 200 반환 및 DB에 확정 상태가 반영된다")
    void confirmPromotion_fullFlow() throws Exception {
        PromotionStatusUpdateRequest request = new PromotionStatusUpdateRequest(PromotionStatus.CONFIRMATION_OF_PROMOTION);
        mockMvc.perform(patch("/api/v1/hr/promotions/" + tierPromotionId)
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        PromotionHistory history = promotionHistoryRepository.findById(tierPromotionId).orElseThrow();
        assertThat(history.getTierPromoStatus()).isEqualTo(PromotionStatus.CONFIRMATION_OF_PROMOTION);
        assertThat(history.getTierReviewedAt()).isNotNull();
        assertThat(history.getReviewerId()).isEqualTo(HRM_EMPLOYEE_ID);
    }

    @Test
    @DisplayName("승급 보류 전체 흐름 — 200 반환 및 DB에 보류 상태가 반영된다")
    void suspendPromotion_fullFlow() throws Exception {
        PromotionStatusUpdateRequest request = new PromotionStatusUpdateRequest(PromotionStatus.SUSPENSION);
        mockMvc.perform(patch("/api/v1/hr/promotions/" + tierPromotionId)
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        PromotionHistory history = promotionHistoryRepository.findById(tierPromotionId).orElseThrow();
        assertThat(history.getTierPromoStatus()).isEqualTo(PromotionStatus.SUSPENSION);
        assertThat(history.getTierReviewedAt()).isNotNull();
        assertThat(history.getReviewerId()).isEqualTo(HRM_EMPLOYEE_ID);
    }
}

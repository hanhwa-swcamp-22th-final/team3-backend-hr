package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionCandidateDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionCandidateItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionCandidateListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.PromotionQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromotionQueryController.class)
class PromotionQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PromotionQueryService service;

    @Test
    @DisplayName("승급 요약 조회 — 200 반환")
    @WithMockUser(authorities = "HRM")
    void getSummary_success() throws Exception {
        given(service.getSummary()).willReturn(new PromotionSummaryResponse(10L, 4L, 40.0));

        mockMvc.perform(get("/api/v1/hr/promotions/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCandidates").value(10))
                .andExpect(jsonPath("$.data.confirmedCount").value(4))
                .andExpect(jsonPath("$.data.promotionRate").value(40.0));
    }

    @Test
    @DisplayName("후보 목록 조회 — 200 반환")
    @WithMockUser(authorities = "HRM")
    void getCandidates_success() throws Exception {
        PromotionCandidateItem item = new PromotionCandidateItem();
        item.setTierPromotionId(1L);
        given(service.getCandidates(null, 1, 10))
                .willReturn(new PromotionCandidateListResponse(List.of(item), 1L, 1L));

        mockMvc.perform(get("/api/v1/hr/promotions/candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(1));
    }

    @Test
    @DisplayName("후보 상세 조회 — 200 반환")
    @WithMockUser(authorities = "HRM")
    void getCandidateDetail_success() throws Exception {
        PromotionCandidateDetailResponse detail = new PromotionCandidateDetailResponse();
        detail.setTierPromotionId(1L);
        given(service.getCandidateDetail(1L)).willReturn(detail);

        mockMvc.perform(get("/api/v1/hr/promotions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tierPromotionId").value(1));
    }
}

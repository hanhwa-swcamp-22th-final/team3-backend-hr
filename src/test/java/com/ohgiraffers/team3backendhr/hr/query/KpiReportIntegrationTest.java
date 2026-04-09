package com.ohgiraffers.team3backendhr.hr.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class KpiReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "HRM")
    @DisplayName("HRM 권한으로 KPI 엑셀 보고서를 다운로드할 수 있다")
    void downloadKpiReport_success() throws Exception {
        mockMvc.perform(get("/api/v1/hr/kpi/report/download")
                        .param("year", "2026")
                        .param("quarter", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("filename*=UTF-8''")))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @WithMockUser(authorities = "WORKER")
    @DisplayName("HRM 권한이 없으면 KPI 엑셀 보고서를 다운로드할 수 없다 (403)")
    void downloadKpiReport_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/hr/kpi/report/download")
                        .param("year", "2026")
                        .param("quarter", "1"))
                .andExpect(status().isForbidden());
    }
}

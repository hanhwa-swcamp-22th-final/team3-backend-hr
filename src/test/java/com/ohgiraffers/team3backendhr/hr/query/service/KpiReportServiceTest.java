package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiDetailItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KpiReportServiceTest {

    @Mock
    private DashboardQueryService dashboardQueryService;

    @InjectMocks
    private KpiReportService kpiReportService;

    @Test
    @DisplayName("전사 KPI 상세 데이터를 엑셀 바이트 배열로 생성한다")
    void generateHrmKpiExcel_success() {
        // given
        int year = 2026;
        int quarter = 1;
        List<HrmKpiDetailItem> mockItems = List.of(
                new HrmKpiDetailItem(1L, "홍길동", "S", 95.5, "S", "CONFIRMED"),
                new HrmKpiDetailItem(2L, "김철수", "A", 88.0, "A", "CONFIRMED")
        );
        // DashboardQueryService에서 데이터를 가져온다고 가정 (페이징 없이 전체 조회용 메서드 필요할 수 있음)
        given(dashboardQueryService.getHrmKpiDetailsAll(year, quarter)).willReturn(mockItems);

        // when
        byte[] excelBytes = kpiReportService.generateHrmKpiExcel(year, quarter);

        // then
        assertThat(excelBytes).isNotEmpty();
        assertThat(excelBytes.length).isGreaterThan(100); // 최소한의 엑셀 헤더 크기
    }
}

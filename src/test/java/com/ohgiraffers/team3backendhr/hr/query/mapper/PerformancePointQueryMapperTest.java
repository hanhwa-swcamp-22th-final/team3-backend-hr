package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.PerformancePoint;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.PointType;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.PerformancePointRepository;
import com.ohgiraffers.team3backendhr.hr.query.dto.PointHistoryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.PointSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Sql(statements = "SET FOREIGN_KEY_CHECKS = 0", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PerformancePointQueryMapperTest {

    @Autowired
    private PerformancePointQueryMapper mapper;

    @Autowired
    private PerformancePointRepository performancePointRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();
    private final Long employeeId = 300L;

    @BeforeEach
    void setUp() {
        performancePointRepository.saveAndFlush(PerformancePoint.builder()
                .performancePointId(idGenerator.generate())
                .performanceEmployeeId(employeeId)
                .pointType(PointType.QUALITATIVE)
                .pointAmount(BigDecimal.valueOf(80))
                .pointEarnedDate(LocalDate.of(2026, 3, 31))
                .pointDescription("정성 평가 포인트")
                .build());

        performancePointRepository.saveAndFlush(PerformancePoint.builder()
                .performancePointId(idGenerator.generate())
                .performanceEmployeeId(employeeId)
                .pointType(PointType.KNOWLEDGE_SHARING)
                .pointAmount(BigDecimal.valueOf(20))
                .pointEarnedDate(LocalDate.of(2026, 2, 15))
                .pointDescription("지식 공유 포인트")
                .build());
    }

    @Test
    @DisplayName("포인트 요약 — 총 포인트 합계를 반환한다")
    void findSummaryByEmployeeId_success() {
        PointSummaryResponse summary = mapper.findSummaryByEmployeeId(employeeId);

        assertThat(summary.getTotalPoints()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("포인트 이력 — 날짜 내림차순으로 목록을 반환한다")
    void findHistoryByEmployeeId_success() {
        List<PointHistoryResponse> history = mapper.findHistoryByEmployeeId(employeeId);

        assertThat(history).hasSize(2);
        // 최신 날짜(3월31일)가 먼저
        assertThat(history.get(0).getPointEarnedDate()).isEqualTo(LocalDate.of(2026, 3, 31));
        assertThat(history.get(1).getPointEarnedDate()).isEqualTo(LocalDate.of(2026, 2, 15));
    }

    @Test
    @DisplayName("포인트가 없으면 총합 0을 반환한다")
    void findSummaryByEmployeeId_whenNone_returnsZero() {
        PointSummaryResponse summary = mapper.findSummaryByEmployeeId(9999L);

        assertThat(summary.getTotalPoints()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("포인트가 없으면 빈 목록을 반환한다")
    void findHistoryByEmployeeId_whenNone_returnsEmpty() {
        List<PointHistoryResponse> history = mapper.findHistoryByEmployeeId(9999L);

        assertThat(history).isEmpty();
    }
}

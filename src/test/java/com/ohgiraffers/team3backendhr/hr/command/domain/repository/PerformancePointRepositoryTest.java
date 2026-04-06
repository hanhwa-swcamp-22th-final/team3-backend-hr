package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.PerformancePoint;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.PointType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PerformancePointRepositoryTest {

    @Autowired
    private PerformancePointRepository performancePointRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private PerformancePoint performancePoint;
    private Long performancePointId;
    private final Long employeeId = 1001L;

    @BeforeEach
    void setUp() {
        performancePointId = idGenerator.generate();
        performancePoint = PerformancePoint.builder()
                .performancePointId(performancePointId)
                .performanceEmployeeId(employeeId)
                .pointType(PointType.QUANTITY)
                .pointAmount(BigDecimal.valueOf(50))
                .pointEarnedDate(LocalDate.of(2026, 4, 1))
                .pointSourceType("matching_record")
                .pointDescription("고난도 작업 완료")
                .build();
    }

    @Test
    @DisplayName("Save performance point success: performance point is persisted")
    void save_success() {
        PerformancePoint saved = performancePointRepository.save(performancePoint);

        assertNotNull(saved);
        assertEquals(performancePointId, saved.getPerformancePointId());
        assertEquals(PointType.QUANTITY, saved.getPointType());
    }

    @Test
    @DisplayName("Find performance point by id success: return persisted performance point")
    void findById_success() {
        performancePointRepository.save(performancePoint);

        Optional<PerformancePoint> result = performancePointRepository.findById(performancePointId);

        assertTrue(result.isPresent());
        assertEquals(employeeId, result.get().getPerformanceEmployeeId());
    }

    @Test
    @DisplayName("Find performance point by id failure: return empty when performance point does not exist")
    void findById_whenNotFound_thenEmpty() {
        Optional<PerformancePoint> result = performancePointRepository.findById(idGenerator.generate());

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Find performance points by employee id: return all points of the employee")
    void findByPerformanceEmployeeId_success() {
        performancePointRepository.save(performancePoint);
        performancePointRepository.save(PerformancePoint.builder()
                .performancePointId(idGenerator.generate())
                .performanceEmployeeId(employeeId)
                .pointType(PointType.KNOWLEDGE_SHARING)
                .pointAmount(BigDecimal.valueOf(30))
                .pointEarnedDate(LocalDate.of(2026, 4, 2))
                .pointSourceType("knowledge_article")
                .build());

        List<PerformancePoint> result = performancePointRepository.findByPerformanceEmployeeId(employeeId);

        assertTrue(result.size() >= 2);
        assertTrue(result.stream().allMatch(p -> p.getPerformanceEmployeeId().equals(employeeId)));
    }
}

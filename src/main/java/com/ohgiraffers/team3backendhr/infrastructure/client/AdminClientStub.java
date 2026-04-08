package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeSkillResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierChartPointResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierMilestoneResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * AdminClient 임시 Mock 구현체.
 * Admin 서비스 API 확정 전까지 하드코딩 데이터를 반환한다.
 * feign.admin.url 프로퍼티 설정 시 AdminRestClient 로 자동 교체됨.
 */
@Slf4j
@Component
@ConditionalOnMissingBean(name = "adminRestClient")
public class AdminClientStub implements AdminClient {

    @Override
    public List<WorkerResponse> getWorkers() {
        return List.of(
                worker(1L, "EMP-001", "홍길동"),
                worker(2L, "EMP-002", "김철수"),
                worker(3L, "EMP-003", "이영희")
        );
    }

    @Override
    public EmployeeProfileResponse getWorkerProfile(Long employeeId) {
        return EmployeeProfileResponse.builder()
                .employeeId(employeeId)
                .employeeCode("EMP-" + String.format("%03d", employeeId))
                .employeeName("홍길동")
                .departmentName("생산1팀")
                .teamName("A팀")
                .currentTier(Grade.B)
                .totalScore(new BigDecimal("720.50"))
                .build();
    }

    @Override
    public List<EmployeeSkillResponse> getWorkerSkills(Long employeeId) {
        return List.of(
                EmployeeSkillResponse.builder().skillId(1L).skillName("용접").skillLevel("ADVANCED").build(),
                EmployeeSkillResponse.builder().skillId(2L).skillName("절삭가공").skillLevel("INTERMEDIATE").build(),
                EmployeeSkillResponse.builder().skillId(3L).skillName("품질검사").skillLevel("BEGINNER").build()
        );
    }

    @Override
    public List<TierMilestoneResponse> getTierMilestones(Long employeeId) {
        return List.of(
                TierMilestoneResponse.builder().milestoneId(1L).description("정량 평가 80점 이상").requiredPoint(80).isAchieved(true).build(),
                TierMilestoneResponse.builder().milestoneId(2L).description("정성 평가 75점 이상").requiredPoint(75).isAchieved(true).build(),
                TierMilestoneResponse.builder().milestoneId(3L).description("종합 점수 700점 이상").requiredPoint(700).isAchieved(true).build(),
                TierMilestoneResponse.builder().milestoneId(4L).description("이의신청 미제기").requiredPoint(0).isAchieved(false).build()
        );
    }

    @Override
    public List<TierChartPointResponse> getTierChart(Long employeeId) {
        return List.of(
                TierChartPointResponse.builder().year(2024).quarter(1).tier("C").totalScore(new BigDecimal("580.00")).build(),
                TierChartPointResponse.builder().year(2024).quarter(2).tier("C").totalScore(new BigDecimal("620.00")).build(),
                TierChartPointResponse.builder().year(2024).quarter(3).tier("B").totalScore(new BigDecimal("680.00")).build(),
                TierChartPointResponse.builder().year(2024).quarter(4).tier("B").totalScore(new BigDecimal("720.50")).build()
        );
    }

    @Override
    public List<Long> getTeamMemberIds(Long leaderId) {
        return List.of(1L, 2L, 3L);
    }

    @Override
    public void updateEmployeeTier(Long employeeId, Grade newTier) {
        log.warn("[AdminClientStub] updateEmployeeTier 호출 무시 — Admin 서비스 미연결 (employeeId={}, newTier={})",
                employeeId, newTier);
    }

    private WorkerResponse worker(Long id, String code, String name) {
        WorkerResponse w = new WorkerResponse();
        w.setEmployeeId(id);
        w.setEmployeeCode(code);
        w.setEmployeeName(name);
        return w;
    }
}

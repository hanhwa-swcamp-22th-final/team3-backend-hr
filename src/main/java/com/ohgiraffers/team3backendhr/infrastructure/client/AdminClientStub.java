package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DomainKeywordRuleResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeSkillResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierChartPointResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierMilestoneResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnMissingBean(name = "adminRestClient")
public class AdminClientStub implements AdminClient {

    @Override
    public List<WorkerResponse> getWorkers() {
        return List.of(
            worker(1L, "EMP-001", "Stub Worker 1"),
            worker(2L, "EMP-002", "Stub Worker 2"),
            worker(3L, "EMP-003", "Stub Worker 3")
        );
    }

    @Override
    public EmployeeProfileResponse getWorkerProfile(Long employeeId) {
        return EmployeeProfileResponse.builder()
            .employeeId(employeeId)
            .employeeCode("EMP-" + String.format("%03d", employeeId))
            .employeeName("Stub Worker")
            .departmentName("Production")
            .teamName("Team A")
            .currentTier(Grade.B)
            .totalScore(new BigDecimal("720.50"))
            .build();
    }

    @Override
    public List<EmployeeSkillResponse> getWorkerSkills(Long employeeId) {
        return List.of(
            EmployeeSkillResponse.builder().skillId(1L).skillName("Welding").skillLevel("ADVANCED").build(),
            EmployeeSkillResponse.builder().skillId(2L).skillName("Line Analysis").skillLevel("INTERMEDIATE").build(),
            EmployeeSkillResponse.builder().skillId(3L).skillName("Inspection").skillLevel("BEGINNER").build()
        );
    }

    @Override
    public List<TierMilestoneResponse> getTierMilestones(Long employeeId) {
        return List.of(
            TierMilestoneResponse.builder().milestoneId(1L).description("Quantitative score >= 80").requiredPoint(80).isAchieved(true).build(),
            TierMilestoneResponse.builder().milestoneId(2L).description("Qualitative score >= 75").requiredPoint(75).isAchieved(true).build(),
            TierMilestoneResponse.builder().milestoneId(3L).description("Total score >= 700").requiredPoint(700).isAchieved(true).build(),
            TierMilestoneResponse.builder().milestoneId(4L).description("No pending objections").requiredPoint(0).isAchieved(false).build()
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
        log.warn("[AdminClientStub] Skipping tier update because admin service is not connected. employeeId={}, newTier={}",
            employeeId, newTier);
    }

    @Override
    public List<DomainKeywordRuleResponse> getActiveDomainKeywordRules() {
        return List.of(
            keywordRule("safety compliance", new BigDecimal("8.0"), new BigDecimal("1.5")),
            keywordRule("teamwork", new BigDecimal("7.0"), new BigDecimal("1.4")),
            keywordRule("problem solving", new BigDecimal("8.0"), new BigDecimal("1.4"))
        );
    }

    private WorkerResponse worker(Long id, String code, String name) {
        WorkerResponse worker = new WorkerResponse();
        worker.setEmployeeId(id);
        worker.setEmployeeCode(code);
        worker.setEmployeeName(name);
        return worker;
    }

    private DomainKeywordRuleResponse keywordRule(String keyword, BigDecimal baseScore, BigDecimal weight) {
        DomainKeywordRuleResponse response = new DomainKeywordRuleResponse();
        response.setDomainKeyword(keyword);
        response.setDomainBaseScore(baseScore);
        response.setDomainWeight(weight);
        response.setDomainIsActive(Boolean.TRUE);
        return response;
    }
}
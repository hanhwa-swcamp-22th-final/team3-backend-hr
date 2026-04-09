package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.AlgorithmVersionSnapshotResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DepartmentCreateRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DepartmentDetailResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DomainKeywordRuleResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeSkillResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgEmployeeResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgTeamMembersResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgUnitTreeResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TeamCreateRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TeamMemberAddRequest;
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

    @Override
    public AlgorithmVersionSnapshotResponse getAlgorithmVersionSnapshot(Long algorithmVersionId) {
        AlgorithmVersionSnapshotResponse response = new AlgorithmVersionSnapshotResponse();
        response.setAlgorithmVersionId(algorithmVersionId);
        response.setVersionNo("stub-v1");
        response.setImplementationKey("quantitative-stub");
        response.setDescription("Stub algorithm snapshot for local HR tests.");
        response.setIsActive(Boolean.TRUE);
        response.setParameters(
            "{\"environment\":{\"tempWeight\":0.4,\"humidityWeight\":0.3,\"particleWeight\":0.3},\"lotDefectThreshold\":0.6,\"monthly\":{\"environmentCorrection\":0.0,\"materialCorrection\":0.0}}"
        );
        response.setReferenceValues(
            "{\"targetUph\":100.0,\"targetYieldRate\":95.0,\"targetLeadTimeSec\":60.0}"
        );
        return response;
    }

    @Override
    public OrgUnitTreeResponse getOrgTree() {
        return OrgUnitTreeResponse.builder()
                .unitId(1L).unitName("전사").type("ROOT")
                .children(List.of(
                        OrgUnitTreeResponse.builder().unitId(10L).unitName("생산1부").type("DEPARTMENT")
                                .children(List.of(
                                        OrgUnitTreeResponse.builder().unitId(100L).unitName("A팀").type("TEAM").children(List.of()).build(),
                                        OrgUnitTreeResponse.builder().unitId(101L).unitName("B팀").type("TEAM").children(List.of()).build()
                                )).build(),
                        OrgUnitTreeResponse.builder().unitId(11L).unitName("생산2부").type("DEPARTMENT")
                                .children(List.of(
                                        OrgUnitTreeResponse.builder().unitId(110L).unitName("C팀").type("TEAM").children(List.of()).build()
                                )).build()
                )).build();
    }

    @Override
    public List<OrgEmployeeResponse> getEmployees(Long departmentId, Long teamId, String keyword, int page, int size) {
        return List.of(
                OrgEmployeeResponse.builder().employeeId(1L).name("홍길동").departmentName("생산1부").teamName("A팀").role("WORKER").build(),
                OrgEmployeeResponse.builder().employeeId(2L).name("김철수").departmentName("생산1부").teamName("A팀").role("TL").build(),
                OrgEmployeeResponse.builder().employeeId(3L).name("이영희").departmentName("생산2부").teamName("C팀").role("DL").build()
        );
    }

    @Override
    public OrgTeamMembersResponse getTeamMembers(Long teamId) {
        OrgEmployeeResponse leader = OrgEmployeeResponse.builder()
                .employeeId(2L).name("김철수").departmentName("생산1부").teamName("A팀").role("TL").build();
        return OrgTeamMembersResponse.builder()
                .leaderInfo(leader)
                .members(List.of(
                        OrgEmployeeResponse.builder().employeeId(1L).name("홍길동").departmentName("생산1부").teamName("A팀").role("WORKER").build(),
                        OrgEmployeeResponse.builder().employeeId(4L).name("박민수").departmentName("생산1부").teamName("A팀").role("WORKER").build()
                )).build();
    }

    @Override
    public Long createDepartment(DepartmentCreateRequest request) {
        log.warn("[AdminClientStub] createDepartment mock — name={}", request.getDepartmentName());
        return 999L;
    }

    @Override
    public Long updateDepartment(Long departmentId, DepartmentCreateRequest request) {
        log.warn("[AdminClientStub] updateDepartment mock — id={}", departmentId);
        return departmentId;
    }

    @Override
    public void deleteDepartment(Long departmentId) {
        log.warn("[AdminClientStub] deleteDepartment mock — id={}", departmentId);
    }

    @Override
    public Long createTeam(Long departmentId, TeamCreateRequest request) {
        log.warn("[AdminClientStub] createTeam mock — departmentId={}, name={}", departmentId, request.getTeamName());
        return 999L;
    }

    @Override
    public Long updateTeam(Long teamId, TeamCreateRequest request) {
        log.warn("[AdminClientStub] updateTeam mock — id={}", teamId);
        return teamId;
    }

    @Override
    public void deleteTeam(Long teamId) {
        log.warn("[AdminClientStub] deleteTeam mock — id={}", teamId);
    }

    @Override
    public DepartmentDetailResponse getDepartmentDetail(Long departmentId) {
        return DepartmentDetailResponse.builder()
                .departmentId(departmentId).departmentName("생산1부")
                .teamCount(2).totalMembers(6)
                .teams(List.of(
                        DepartmentDetailResponse.TeamSummaryResponse.builder().teamId(100L).teamName("A팀").memberCount(3).build(),
                        DepartmentDetailResponse.TeamSummaryResponse.builder().teamId(101L).teamName("B팀").memberCount(3).build()
                )).build();
    }

    @Override
    public void addTeamMembers(Long teamId, TeamMemberAddRequest request) {
        log.warn("[AdminClientStub] addTeamMembers mock — teamId={}, count={}", teamId, request.getEmployeeIds().size());
    }

    @Override
    public void removeTeamMember(Long teamId, Long employeeId) {
        log.warn("[AdminClientStub] removeTeamMember mock — teamId={}, employeeId={}", teamId, employeeId);
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

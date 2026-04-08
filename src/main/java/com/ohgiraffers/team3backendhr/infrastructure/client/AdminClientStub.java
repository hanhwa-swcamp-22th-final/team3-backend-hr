package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DepartmentCreateRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DepartmentDetailResponse;
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

/**
 * AdminClient 임시 Mock 구현체.
 * Admin 서비스 API 확정 전까지 하드코딩 데이터를 반환한다.
 * feign.admin.url 프로퍼티 설정 시 AdminRestClient 로 자동 교체된다.
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
                EmployeeSkillResponse.builder().skillId(2L).skillName("원인분석").skillLevel("INTERMEDIATE").build(),
                EmployeeSkillResponse.builder().skillId(3L).skillName("품질검사").skillLevel("BEGINNER").build()
        );
    }

    @Override
    public List<TierMilestoneResponse> getTierMilestones(Long employeeId) {
        return List.of(
                TierMilestoneResponse.builder().milestoneId(1L).description("정량 평가 80점 이상").requiredPoint(80).isAchieved(true).build(),
                TierMilestoneResponse.builder().milestoneId(2L).description("정성 평가 75점 이상").requiredPoint(75).isAchieved(true).build(),
                TierMilestoneResponse.builder().milestoneId(3L).description("종합 점수 700점 이상").requiredPoint(700).isAchieved(true).build(),
                TierMilestoneResponse.builder().milestoneId(4L).description("이의요청 미제기").requiredPoint(0).isAchieved(false).build()
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
        log.warn("[AdminClientStub] updateEmployeeTier 호출 무시 - Admin 서비스 미연결(employeeId={}, newTier={})",
                employeeId, newTier);
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
        WorkerResponse w = new WorkerResponse();
        w.setEmployeeId(id);
        w.setEmployeeCode(code);
        w.setEmployeeName(name);
        return w;
    }
}